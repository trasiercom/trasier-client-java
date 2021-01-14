package com.trasier.client.opentracing;

import io.opentracing.SpanContext;

import java.util.Map;
import java.util.Objects;

public class TrasierSpanContext implements SpanContext {
    private final String conversationId;
    private final String traceId;
    private final String spanId;
    private final boolean sample;
    private final Map<String, String> baggageItems;

    public TrasierSpanContext(String conversationId, String traceId, String spanId, boolean sample, Map<String, String> baggageItems) {
        this.conversationId = conversationId;
        this.traceId = traceId;
        this.spanId = spanId;
        this.baggageItems = baggageItems;
        this.sample = sample;
    }

    public Map<String, String> getBaggageItems() {
        return baggageItems;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return baggageItems.entrySet();
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public boolean isSample() {
        return sample;
    }

    @Override
    public String toTraceId() {
        return traceId;
    }

    @Override
    public String toSpanId() {
        return spanId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrasierSpanContext that = (TrasierSpanContext) o;
        return sample == that.sample &&
                Objects.equals(conversationId, that.conversationId) &&
                Objects.equals(traceId, that.traceId) &&
                Objects.equals(spanId, that.spanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, traceId, spanId, sample);
    }
}