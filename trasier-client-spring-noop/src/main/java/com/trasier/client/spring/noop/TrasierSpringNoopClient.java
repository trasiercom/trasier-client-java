package com.trasier.client.spring.noop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.TrasierCompressSpanInterceptor;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.client.TrasierSpringClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("trasierSpringClient")
public class TrasierSpringNoopClient implements TrasierSpringClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierSpringNoopClient.class);

    private final TrasierClientConfiguration clientConfiguration;

    private TrasierCompressSpanInterceptor compressSpanInterceptor;

    @Autowired(required = false)
    private final List<TrasierSpanInterceptor> spanInterceptors = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TrasierSpringNoopClient(TrasierClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;

        if (clientConfiguration.isActivated()) {
            if (!clientConfiguration.isCompressPayloadDisabled()) {
                this.compressSpanInterceptor = new TrasierCompressSpanInterceptor();
            }
        }
    }

    @Override
    public boolean sendSpan(Span span) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        if (span.isCancel()) {
            return false;
        }

        applyInterceptors(span);

        if (span.isCancel()) {
            return false;
        }

        if (compressSpanInterceptor != null) {
            compressSpanInterceptor.intercept(span);
        }

        try {
            LOGGER.trace(objectMapper.writeValueAsString(span));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        if (!clientConfiguration.isActivated()) {
            return false;
        }

        return true;
    }

    private void applyInterceptors(Span span) {
        for (TrasierSpanInterceptor spanInterceptor : this.spanInterceptors) {
            spanInterceptor.intercept(span);
        }
    }

    @Override
    public void close() {
    }

}