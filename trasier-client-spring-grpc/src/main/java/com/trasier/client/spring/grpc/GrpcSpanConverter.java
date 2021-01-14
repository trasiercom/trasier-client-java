package com.trasier.client.spring.grpc;

import java.util.Map;
import java.util.Objects;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;

public class GrpcSpanConverter {

    public com.trasier.api.client.protobuf.Span convert(Span span) {
        com.trasier.api.client.protobuf.Span.Builder builder = com.trasier.api.client.protobuf.Span.newBuilder();

        builder.setId(span.getId());
        builder.setTraceId(span.getTraceId());
        builder.setConversationId(span.getConversationId());

        if (span.getName() != null) {
            builder.setName(span.getName());
        }
        if (span.getParentId() != null) {
            builder.setParentId(span.getParentId());
        }
        if (span.getStatus() != null) {
            builder.setStatus(span.getStatus());
        }
        if (span.getTags() != null) {
            builder.putAllTags(dropNullValues(span.getTags()));
        }
        if (span.getFeatures() != null) {
            builder.putAllFeatures(dropNullValues(span.getFeatures()));
        }
        if (span.getStartTimestamp() != null) {
            builder.setStartTimestamp(span.getStartTimestamp());
        }
        if (span.getBeginProcessingTimestamp() != null) {
            builder.setBeginProcessingTimestamp(span.getBeginProcessingTimestamp());
        }
        if (span.getIncomingEndpoint() != null) {
            builder.setIncomingEndpoint(convertEndpoint(span.getIncomingEndpoint()));
        }
        if (span.getIncomingContentType() != null) {
            builder.setIncomingContentType(convertContentType(span.getIncomingContentType()));
        }
        if (span.getIncomingData() != null) {
            builder.setIncomingData(span.getIncomingData());
        }
        if (span.getIncomingHeader() != null) {
            builder.putAllIncomingHeader(dropNullValues(span.getIncomingHeader()));
        }
        if (span.getFinishProcessingTimestamp() != null) {
            builder.setFinishProcessingTimestamp(span.getFinishProcessingTimestamp());
        }
        if (span.getEndTimestamp() != null) {
            builder.setEndTimestamp(span.getEndTimestamp());
        }
        if (span.getOutgoingEndpoint() != null) {
            builder.setOutgoingEndpoint(convertEndpoint(span.getOutgoingEndpoint()));
        }
        if (span.getOutgoingContentType() != null) {
            builder.setOutgoingContentType(convertContentType(span.getOutgoingContentType()));
        }
        if (span.getOutgoingData() != null) {
            builder.setOutgoingData(span.getOutgoingData());
        }
        if (span.getOutgoingHeader() != null) {
            builder.putAllOutgoingHeader(dropNullValues(span.getOutgoingHeader()));
        }
        return builder.build();
    }

    private com.trasier.api.client.protobuf.ContentType convertContentType(ContentType outgoingContentType) {
        return outgoingContentType == null ? null : com.trasier.api.client.protobuf.ContentType.valueOf(outgoingContentType.name());
    }

    private Map<String, String> dropNullValues(Map<String, String> map) {
        map.values().removeIf(Objects::isNull);
        return map;
    }

    private com.trasier.api.client.protobuf.Endpoint convertEndpoint(Endpoint endpoint) {
        if(endpoint != null) {
            com.trasier.api.client.protobuf.Endpoint.Builder builder = com.trasier.api.client.protobuf.Endpoint.newBuilder();
            if (endpoint.getName() != null) {
                builder.setName(endpoint.getName());
            }
            if (endpoint.getIpAddress() != null) {
                builder.setIpAddress(endpoint.getIpAddress());
            }
            if (endpoint.getPort() != null) {
                builder.setPort(endpoint.getPort());
            }
            if (endpoint.getHostname() != null) {
                builder.setHostname(endpoint.getHostname());
            }
            return builder.build();
        }
        return null;
    }

}