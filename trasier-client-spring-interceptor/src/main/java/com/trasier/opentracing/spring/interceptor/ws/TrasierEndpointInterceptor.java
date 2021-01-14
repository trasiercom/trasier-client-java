package com.trasier.opentracing.spring.interceptor.ws;

import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.SafeSpanResolverInterceptorInvoker;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import com.trasier.client.opentracing.TrasierSpan;
import io.opentracing.Tracer;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.SmartEndpointInterceptor;

import java.util.List;

/**
 * Enrich data that we could not access much higher on the filter level.
 * The rest will be handeled by the TrasierServletFilterSpanDecorator
 */
public class TrasierEndpointInterceptor implements SmartEndpointInterceptor {

    private final Tracer tracer;
    private final TrasierClientConfiguration configuration;
    private final SafeSpanResolverInterceptorInvoker interceptorInvoker;

    public TrasierEndpointInterceptor(Tracer tracer, TrasierClientConfiguration configuration, List<TrasierSpanResolverInterceptor> samplingInterceptors) {
        this.tracer = tracer;
        this.configuration = configuration;
        this.interceptorInvoker = new SafeSpanResolverInterceptorInvoker(samplingInterceptors);
    }

    @Override
    public boolean shouldIntercept(MessageContext messageContext, Object endpoint) {
        return configuration.isActivated();
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object o) {
        TrasierSpan activeSpan = (TrasierSpan) tracer.activeSpan();
        Span trasierSpan = activeSpan.unwrap();
        if (!trasierSpan.isCancel()) {
            String operationName = WSUtil.extractOperationName(messageContext, o);
            if (operationName != null && !TrasierConstants.UNKNOWN_WS_CALL.equals(operationName)) {
                trasierSpan.setName(operationName);
                interceptorInvoker.invokeOnMetadataResolved(trasierSpan);
            }
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageConttext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
    }

}
