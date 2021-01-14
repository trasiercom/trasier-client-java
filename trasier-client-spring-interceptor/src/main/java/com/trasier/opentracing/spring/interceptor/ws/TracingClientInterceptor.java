package com.trasier.opentracing.spring.interceptor.ws;

import com.trasier.client.api.TrasierConstants;
import com.trasier.client.interceptor.SafeSpanResolverInterceptorInvoker;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TracingClientInterceptor extends ClientInterceptorAdapter {
    private final Tracer tracer;
    private final SafeSpanResolverInterceptorInvoker interceptorInvoker;

    public TracingClientInterceptor(Tracer tracer, List<TrasierSpanResolverInterceptor> samplingInterceptors) {
        this.tracer = tracer;
        this.interceptorInvoker = new SafeSpanResolverInterceptorInvoker(samplingInterceptors);
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        Span span = tracer.buildSpan(TrasierConstants.UNKNOWN_WS_CALL)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();

        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            interceptorInvoker.invokeOnRequestUriResolved(trasierSpan, extractUrlPath(messageContext));
            if (!trasierSpan.isCancel()) {
                trasierSpan.setName(WSUtil.extractOperationName(messageContext, null));
                interceptorInvoker.invokeOnMetadataResolved(trasierSpan);
            }
        }

        TransportContext context = TransportContextHolder.getTransportContext();
        if (context.getConnection() instanceof HeadersAwareSenderWebServiceConnection) {
            final HeadersAwareSenderWebServiceConnection httpConnection = (HeadersAwareSenderWebServiceConnection) context.getConnection();

            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                @Override
                public Iterator<Map.Entry<String, String>> iterator() {
                    return null;
                }

                @Override
                public void put(String key, String value) {
                    try {
                        httpConnection.addRequestHeader(key, value);
                        if (span instanceof TrasierSpan && value != null) {
                            ((TrasierSpan) span).unwrap().getIncomingHeader().put(key, value);
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
        }

        Scope scope = tracer.activateSpan(span);
        messageContext.setProperty("TRASIER_ACTIVE_SPAN", span);
        scope.close(); //close immediately as the response may be handled asynchronously
        return super.handleRequest(messageContext);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return super.handleResponse(messageContext);
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return super.handleFault(messageContext);
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        TrasierSpan span = (TrasierSpan) messageContext.getProperty("TRASIER_ACTIVE_SPAN");
        if (span != null) {
            if(ex != null) {
                span.handleException(ex);
            }
            span.finish();
        }
        super.afterCompletion(messageContext, ex);
    }

    private String extractUrlPath(MessageContext messageContext) {
        if (messageContext.getRequest() instanceof SoapMessage) {
            String pathByOperationName = WSUtil.extractOperationName(messageContext, null);
            if (!StringUtils.isEmpty(pathByOperationName)) {
                return "/" + pathByOperationName;
            }
        }
        return "";
    }

}
