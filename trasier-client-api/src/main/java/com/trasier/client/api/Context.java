package com.trasier.client.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Context {
    @NonNull
    private String conversationId;
    @NonNull
    private String traceId;
    @NonNull
    private String spanId;
    private Map<String, ?> baggageItems;
}
