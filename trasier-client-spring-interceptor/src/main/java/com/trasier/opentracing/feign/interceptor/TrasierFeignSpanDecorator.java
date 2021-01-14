package com.trasier.opentracing.feign.interceptor;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierSpan;
import com.trasier.client.util.ExceptionUtils;
import com.trasier.opentracing.spring.interceptor.rest.TrasierClientRequestInterceptor;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO LP 20.05.2020: Filter interceptors
public class TrasierFeignSpanDecorator implements FeignSpanDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierClientRequestInterceptor.class);

    private final TrasierClientConfiguration configuration;
    private final Tracer tracer;

    public TrasierFeignSpanDecorator(TrasierClientConfiguration configuration, Tracer tracer) {
        this.configuration = configuration;
        this.tracer = tracer;
    }

    @Override
    public void onRequest(feign.Request request, feign.Request.Options options, Span span) {
        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setBeginProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setIncomingContentType(ContentType.JSON);
            try {
                trasierSpan.getIncomingHeader().putAll(toSingleValueMap(request.headers()));
                byte[] body = request.body();
                if(body != null && body.length > 0 && !configuration.isPayloadTracingDisabled()) {
                    trasierSpan.setIncomingData(new String(body));
                }
            } catch (Exception e) {
                LOGGER.error("Error while logging request", e);
            }
        }
    }

    @Override
    public void onResponse(feign.Response response, feign.Request.Options options, Span span) {
        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            if (response != null) {
                trasierSpan.setOutgoingContentType(ContentType.JSON);
                try {
                    trasierSpan.getOutgoingHeader().putAll(toSingleValueMap(response.headers()));
                    // TODO LP 20.05.2020: Activate and retest
//                    String responseBody = StreamUtils.copyToString(response.body().asInputStream(), Charset.defaultCharset());
//                    trasierSpan.setOutgoingData(responseBody);
                } catch (Exception e) {
                    LOGGER.error("Error while logging response", e);
                }
            }
        }
    }

    @Override
    public void onError(Exception e, feign.Request request, Span span) {
        if (span instanceof TrasierSpan) {
            com.trasier.client.api.Span trasierSpan = ((TrasierSpan) span).unwrap();
            trasierSpan.setFinishProcessingTimestamp(System.currentTimeMillis());
            trasierSpan.setStatus(TrasierConstants.STATUS_ERROR);

            if(e != null && !configuration.isPayloadTracingDisabled()) {
                String exception = ExceptionUtils.getString(e);
                trasierSpan.setOutgoingData(exception);
            }
        }
    }

    private Map<String, String> toSingleValueMap(Map<String, Collection<String>> headers) {
        LinkedHashMap<String, String> singleValueMap = new LinkedHashMap<>(headers.size());
        headers.forEach((key, valueList) -> singleValueMap.put(key, valueList.iterator().next()));
        return singleValueMap;
    }

}