package com.trasier.client.spring.context;

import com.trasier.client.api.Span;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TrasierSpringAccessor {

    public Span createChildSpan(String operationName) {
        if (isTracing()) {
            Span currentSpan = TrasierContextHolder.getSpan();
            Span.SpanBuilder spanBuilder = Span.newSpan(operationName, currentSpan.getConversationId(), currentSpan.getTraceId(), UUID.randomUUID().toString());
            Span newSpan = spanBuilder.build();
            TrasierContextHolder.setSpan(newSpan);
            return newSpan;
        }
        return null;
    }

    public Span createSpan(String operationName, String conversationId, String traceId, String spanId) {
        String traceIdNotNull = traceId != null ? traceId : UUID.randomUUID().toString();
        String spanIdNotNull = spanId != null ? spanId : UUID.randomUUID().toString();
        Span.SpanBuilder spanBuilder = Span.newSpan(operationName, conversationId, traceIdNotNull, spanIdNotNull);
        spanBuilder.startTimestamp(System.currentTimeMillis());
        Span span = spanBuilder.build();
        TrasierContextHolder.setSpan(span);
        return span;
    }

    public void closeSpan(Span span) {
        if (isTracing() && span != null) {
            Span currentSpan = TrasierContextHolder.getSpan();
            if (!isValidSpan(span, currentSpan)) {
                throw new IllegalArgumentException("Tried to close wrong span.");
            } else {
                currentSpan.setEndTimestamp(System.currentTimeMillis());
                TrasierContextHolder.closeSpan();
            }
        }
    }

    private boolean isValidSpan(Span span, Span currentSpan) {
        return span.getId().equals(currentSpan.getId()) && span.getTraceId().equals(currentSpan.getTraceId()) && span.getConversationId().equals(currentSpan.getConversationId());
    }

    public Span getCurrentSpan() {
        return TrasierContextHolder.getSpan();
    }

    public boolean isTracing() {
        return TrasierContextHolder.isTracing();
    }
}