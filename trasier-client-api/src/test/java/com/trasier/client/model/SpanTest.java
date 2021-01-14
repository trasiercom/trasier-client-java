package com.trasier.client.model;

import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SpanTest {
    @Test
    public void shoudlCheckMandatoryFields() {
        assertFalse(isThrowingException(Span.newSpan("TestOp", "1", "2", "3").startTimestamp(1L)));
        assertFalse(isThrowingException(Span.newSpan("TestOp", "1", "2", "3")
                .outgoingEndpoint(new Endpoint("Consumer")).endTimestamp(2L)
        ));
    }

    private boolean isThrowingException(Span.SpanBuilder builder) {
        try {
            builder.build();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}