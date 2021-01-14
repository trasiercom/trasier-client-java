package com.trasier.client.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

@Builder(builderMethodName = "hiddenBuilder")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Span {
    @NonNull
    private String id;
    @NonNull
    private String traceId;
    @NonNull
    private String conversationId;
    @NonNull
    private String name;

    private String parentId;
    private String status;
    private Map<String, String> tags;
    private Map<String, String> features;

    private Long startTimestamp;
    private Long beginProcessingTimestamp;
    private Endpoint incomingEndpoint;
    private ContentType incomingContentType;
    private String incomingData;
    private Map<String, String> incomingHeader;

    private Long finishProcessingTimestamp;
    private Long endTimestamp;
    private Endpoint outgoingEndpoint;
    private ContentType outgoingContentType;
    private String outgoingData;
    private Map<String, String> outgoingHeader;
    private boolean cancel;
    private boolean payloadDisabled;

    public static SpanBuilder newSpan(String name, String conversationId, String traceId, String spanId) {
        return hiddenBuilder().name(name).conversationId(conversationId).traceId(traceId).id(spanId).status("OK");
    }

    private static SpanBuilder hiddenBuilder() {
        return new SpanBuilder();
    }

    public Map<String, String> getIncomingHeader() {
        if (this.incomingHeader == null) {
            this.incomingHeader = new LinkedHashMap<>();
        }
        return this.incomingHeader;
    }

    public Map<String, String> getOutgoingHeader() {
        if (this.outgoingHeader == null) {
            this.outgoingHeader = new LinkedHashMap<>();
        }
        return this.outgoingHeader;
    }

    public Map<String, String> getTags() {
        if (this.tags == null) {
            this.tags = new LinkedHashMap<>();
        }
        return this.tags;
    }

    public Map<String, String> getFeatures() {
        if (this.features == null) {
            this.features = new LinkedHashMap<>();
        }
        return this.features;
    }
}
