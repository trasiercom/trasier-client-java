package com.trasier.opentracing.spring.interceptor.rest;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.SafeSpanResolverInterceptorInvoker;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

public class TrasierClientRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierClientRequestInterceptor.class);

    private final Tracer tracer;
    private final SafeSpanResolverInterceptorInvoker interceptorInvoker;
    private final TrasierClientConfiguration configuration;

    public TrasierClientRequestInterceptor(Tracer tracer, TrasierClientConfiguration configuration, List<TrasierSpanResolverInterceptor> samplingInterceptors) {
        this.tracer = tracer;
        this.configuration = configuration;
        this.interceptorInvoker = new SafeSpanResolverInterceptorInvoker(samplingInterceptors);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
        TrasierSpan span = (TrasierSpan) tracer.activeSpan();
        Span unwrap = span.unwrap();
        unwrap.setName(extractOperationName(request.getURI(), unwrap.getName()));
        interceptorInvoker.invokeOnRequestUriResolved(unwrap, request.getURI().getPath());
        interceptorInvoker.invokeOnMetadataResolved(unwrap);
        handleRequest(request, data, span);

        ClientHttpResponse response = execution.execute(request, data);

        handleResponse(response, span);
        return response;
    }

    private void handleResponse(ClientHttpResponse response, TrasierSpan span) {
        if (span != null && !span.unwrap().isCancel()) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            if (response != null) {
                trasierSpan.setOutgoingContentType(ContentType.JSON);
                trasierSpan.getOutgoingHeader().putAll(response.getHeaders().toSingleValueMap());
                if (!configuration.isPayloadTracingDisabled() && !trasierSpan.isPayloadDisabled()) {
                    try {
                        InputStream body = null;
                        try {
                            body = response.getBody(); // throws exception on empty input stream
                        } catch (Exception e) {
                            LOGGER.debug(e.getMessage(), e);
                        }
                        if (body instanceof ByteArrayInputStream) {
                            String responseBody = StreamUtils.copyToString(body, Charset.defaultCharset());
                            trasierSpan.setOutgoingData(responseBody);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while logging response", e);
                    }
                }
            }
        }
    }

    private void handleRequest(HttpRequest request, byte[] data, TrasierSpan span) {
        if (span != null && !span.unwrap().isCancel()) {
            Span trasierSpan = span.unwrap();
            trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setIncomingContentType(ContentType.JSON);
            try {
                trasierSpan.getIncomingHeader().putAll(request.getHeaders().toSingleValueMap());
                if (!configuration.isPayloadTracingDisabled() && !span.unwrap().isPayloadDisabled()) {
                    trasierSpan.setIncomingData(new String(data));
                }
            } catch (Exception e) {
                LOGGER.error("Error while logging request", e);
            }
        }
    }

    private String extractOperationName(URI requestUri, String defaultName) {
        String path = requestUri.getPath();
        if (!path.isEmpty() && !path.equals("/")) {
            return path;
        }
        return defaultName;
    }

}