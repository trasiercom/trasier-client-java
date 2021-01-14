package com.trasier.client.opentracing;

import com.trasier.client.api.Client;
import com.trasier.client.api.Endpoint;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrasierSpanBuilder implements Tracer.SpanBuilder {
    private Client client;
    private TrasierClientConfiguration configuration;
    private TrasierTracer tracer;
    private String conversationId;
    private String traceId;
    private String spanId;
    private String operationName;
    private long startTimestamp;
    private boolean cancel;
    private boolean ignoreActiveSpan;
    private TrasierSpanContext reference;
    private Map<String, String> tags;
    private Map<String, String> baggageItems;

    public TrasierSpanBuilder(Client client, TrasierClientConfiguration configuration, TrasierTracer tracer, String operationName) {
        this.client = client;
        this.configuration = configuration;
        this.tracer = tracer;
        this.operationName = operationName;
        this.baggageItems = new HashMap<>();

        this.startTimestamp = System.currentTimeMillis();
        this.tags = new HashMap<>();

        conversationId = UUID.randomUUID().toString();
        traceId = conversationId;
        spanId = UUID.randomUUID().toString();
    }

    @Override
    public TrasierSpanBuilder asChildOf(SpanContext parent) {
        return addReference(References.CHILD_OF, parent);
    }

    @Override
    public TrasierSpanBuilder asChildOf(Span parent) {
        return asChildOf(parent != null ? parent.context() : null);
    }

    @Override
    public TrasierSpanBuilder addReference(String type, SpanContext context) {
        if (reference != null || context == null) {
            return this;
        }

        if (References.CHILD_OF.equals(type) || References.FOLLOWS_FROM.equals(type)) {
            reference = (TrasierSpanContext) context;
            conversationId = reference.getConversationId();
            traceId = reference.getTraceId();
            cancel = !reference.isSample();
            baggageItems = reference.getBaggageItems();
        }

        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        this.ignoreActiveSpan = true;
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        if (value != null) {
            tags.put(key, value);
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        tags.put(key, Boolean.toString(value));
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        if (value != null) {
            tags.put(key, value.toString());
        }
        return this;
    }

    @Override
    public <T> Tracer.SpanBuilder withTag(Tag<T> tag, T value) {
        if (tag != null && value != null) {
            tags.put(tag.getKey(), value.toString());
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
        return this;
    }

    @Override
    public Span start() {
        boolean server = Tags.SPAN_KIND_SERVER.equals(tags.get(Tags.SPAN_KIND.getKey()));

        if (reference == null && !ignoreActiveSpan) {
            Span parent = tracer.scopeManager().activeSpan();
            if (parent != null) {
                asChildOf(parent);
            }
        }

        if (server && reference != null) {
            spanId = reference.getSpanId();
        }

        com.trasier.client.api.Span.SpanBuilder wrappedBuilder = com.trasier.client.api.Span.newSpan(operationName, conversationId, traceId, spanId);
        if (server) {
            wrappedBuilder.outgoingEndpoint(new Endpoint(configuration.getSystemName()));
            wrappedBuilder.incomingEndpoint(new Endpoint("UNKNOWN"));
        } else {
            wrappedBuilder.incomingEndpoint(new Endpoint(configuration.getSystemName()));
            wrappedBuilder.outgoingEndpoint(new Endpoint("UNKNOWN"));
        }
        wrappedBuilder.startTimestamp(startTimestamp);
        wrappedBuilder.cancel(cancel);
        if (reference != null) {
            wrappedBuilder.parentId(reference.getSpanId());
        }
        com.trasier.client.api.Span wrapped = wrappedBuilder.build();
        wrapped.setTags(tags);

        TrasierSpan span = new TrasierSpan(client, wrapped, baggageItems);
        return span;
    }
}
