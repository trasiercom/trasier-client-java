package com.trasier.client.opentracing;

import com.trasier.client.api.Client;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrasierTracer implements Tracer {
    private Client client;
    private TrasierClientConfiguration configuration;
    private TrasierScopeManager trasierScopeManager;

    public TrasierTracer(Client client, TrasierClientConfiguration configuration, TrasierScopeManager trasierScopeManager) {
        this.client = client;
        this.configuration = configuration;
        this.trasierScopeManager = trasierScopeManager;
    }

    @Override
    public ScopeManager scopeManager() {
        return trasierScopeManager;
    }

    @Override
    public Span activeSpan() {
        return trasierScopeManager.activeSpan();
    }

    @Override
    public Scope activateSpan(Span span) {
        return trasierScopeManager.activate(span);
    }

    @Override
    public void close() {
        //TODO Hackergarten
        //Close everything
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new TrasierSpanBuilder(client, configuration, this, operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C context) {
        if (context instanceof TextMap) {
            TrasierSpanContext trasierSpanContext = (TrasierSpanContext) spanContext;
            ((TextMap) context).put(TrasierConstants.HEADER_CONVERSATION_ID, trasierSpanContext.getConversationId());
            ((TextMap) context).put(TrasierConstants.HEADER_TRACE_ID, trasierSpanContext.getTraceId());
            ((TextMap) context).put(TrasierConstants.HEADER_SPAN_ID, trasierSpanContext.getSpanId());
            ((TextMap) context).put(TrasierConstants.HEADER_CONVERSATION_SAMPLE, Boolean.toString(trasierSpanContext.isSample()));
            ((TextMap) context).put(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME, configuration.getSystemName());
        }
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C context) {
        if (context instanceof TextMap) {
            String conversationId = null;
            String traceId = null;
            String spanId = null;
            Boolean sample = Boolean.TRUE;

            Map<String, String> baggageItems = new HashMap<>();
            for (Map.Entry<String, String> entry : ((TextMap) context)) {
                if (TrasierConstants.HEADER_CONVERSATION_ID.equalsIgnoreCase(entry.getKey())) {
                    conversationId = entry.getValue();
                } else if (TrasierConstants.HEADER_TRACE_ID.equalsIgnoreCase(entry.getKey())) {
                    traceId = entry.getValue();
                } else if (TrasierConstants.HEADER_SPAN_ID.equalsIgnoreCase(entry.getKey())) {
                    spanId = entry.getValue();
                } else if (TrasierConstants.HEADER_CONVERSATION_SAMPLE.equalsIgnoreCase(entry.getKey())) {
                    sample = Boolean.valueOf(entry.getValue());
                }
            }

            if (conversationId != null && !conversationId.trim().isEmpty()) {
                if (traceId == null || traceId.isEmpty()) {
                    traceId = UUID.randomUUID().toString();
                }
                if (spanId == null || spanId.isEmpty()) {
                    spanId = UUID.randomUUID().toString();
                }
                TrasierSpanContext spanContext = new TrasierSpanContext(conversationId, traceId, spanId, sample, baggageItems);
                return spanContext;
            }
        }

        return null;
    }
}
