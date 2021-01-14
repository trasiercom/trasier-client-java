package com.trasier.client.ws;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.SafeSpanResolverInterceptorInvoker;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import com.trasier.client.opentracing.TrasierScopeManager;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.util.LocalEndpointHolder;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TracingSoapHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracingSoapHandler.class);

    private final Tracer tracer;
    private final TrasierClientConfiguration clientConfig;
    private final SafeSpanResolverInterceptorInvoker interceptorInvoker;

    public TracingSoapHandler(Tracer tracer, TrasierClientConfiguration clientConfig) {
        this(tracer, clientConfig, Collections.emptyList());
    }

    public TracingSoapHandler(Tracer tracer, TrasierClientConfiguration clientConfig, List<TrasierSpanResolverInterceptor> samplingInterceptors) {
        this.tracer = tracer;
        this.clientConfig = clientConfig;
        this.interceptorInvoker = new SafeSpanResolverInterceptorInvoker(samplingInterceptors);
    }

    @Override
    public void close(MessageContext messageContext) {
    }

    @Override
    public Set<QName> getHeaders() {
        return new HashSet<>();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        if (tracer instanceof TrasierTracer && clientConfig.isActivated()) {
            handle(soapMessageContext, false);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        if (tracer instanceof TrasierTracer && clientConfig.isActivated()) {
            handle(soapMessageContext, true);
        }
        return true;
    }

    private void handle(SOAPMessageContext soapMessageContext, boolean isFault) {
        TrasierTracer tracer = (TrasierTracer) this.tracer;
        TrasierSpan trasierSpan;

        boolean isRequest = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (isRequest) {
            String methodName = resolveMethodName(soapMessageContext);
            trasierSpan = (TrasierSpan) tracer.buildSpan(methodName).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
            interceptorInvoker.invokeOnMetadataResolved(trasierSpan.unwrap());
            tracer.activateSpan(trasierSpan);
        } else {
            trasierSpan = (TrasierSpan) tracer.activeSpan();
        }
        Span span = trasierSpan.unwrap();

        try {
            tracer.inject(trasierSpan.context(), Format.Builtin.HTTP_HEADERS, new SOAPMessageInjectAdapter(soapMessageContext, clientConfig, span));

            if (isRequest) {
                span.setIncomingContentType(ContentType.XML);
                span.setIncomingEndpoint(LocalEndpointHolder.getLocalEndpoint(clientConfig.getSystemName()));
                URL url = getOutgoingEndpointUrl(soapMessageContext);
                if (url != null) {
                    int indexOfDot = url.getHost().indexOf(".");
                    String endpointName = indexOfDot > 0 ? url.getHost().substring(0, indexOfDot) : url.getHost();
                    span.setOutgoingEndpoint(createEndpoint(endpointName, url.getHost(), url.getPort()));
                    interceptorInvoker.invokeOnRequestUriResolved(span, url.getPath());
                }
                span.setStartTimestamp(new Date().getTime());
                span.setIncomingHeader(createIncommingHeaders(url));
                if (!clientConfig.isPayloadTracingDisabled() && !span.isPayloadDisabled()) {
                    span.setIncomingData(getMessagePayload(soapMessageContext));
                }
            } else {
                span.setOutgoingContentType(ContentType.XML);
                span.setEndTimestamp(new Date().getTime());
                if (!clientConfig.isPayloadTracingDisabled() && !span.isPayloadDisabled()) {
                    span.setOutgoingData(getMessagePayload(soapMessageContext));
                }
                span.setStatus(isFault ? "ERROR" : "OK");
                TrasierScopeManager scopeManager = (TrasierScopeManager)tracer.scopeManager();
                scopeManager.activeScope().close();
                trasierSpan.finish();
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    private URL getOutgoingEndpointUrl(SOAPMessageContext soapMessageContext) {
        HttpURLConnection httpConnection = (HttpURLConnection) soapMessageContext.get("http.connection");
        if (httpConnection != null && httpConnection.getURL() != null) {
            return httpConnection.getURL();
        }
        Object address = soapMessageContext.get("javax.xml.ws.service.endpoint.address");
        if (address instanceof String) {
            try {
                return new URL(String.valueOf(address));
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    private String getMessagePayload(SOAPMessageContext soapMessageContext) throws SOAPException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMessageContext.getMessage().writeTo(out);
        return out.toString();
    }

    private Map<String, String> createIncommingHeaders(URL url) {
        Map<String, String> traceableHeaders = new HashMap<>();
        if (url != null) {
            traceableHeaders.put("url", url.toString());
        }
        return traceableHeaders;
    }

    private static Endpoint createEndpoint(String name, String host, Integer port) {
        Endpoint endpoint = new Endpoint(name);
        endpoint.setHostname(host);
        endpoint.setPort(String.valueOf(port));
        return endpoint;
    }

    private String resolveMethodName(SOAPMessageContext soapMessageContext) {
        Method method = (Method) soapMessageContext.get("java.lang.reflect.Method");
        String methodName = "soap";
        if (method != null) {
            methodName = method.getName();
        } else {
            Object operation = soapMessageContext.get(MessageContext.WSDL_OPERATION);
            if (operation instanceof QName) {
                methodName = ((QName) operation).getLocalPart();
            }
        }
        return methodName;
    }

}
