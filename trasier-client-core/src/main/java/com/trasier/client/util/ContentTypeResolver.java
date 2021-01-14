package com.trasier.client.util;

import com.trasier.client.api.ContentType;

public final class ContentTypeResolver {
    
    public static ContentType resolveFromPayload(String payload) {
        if (payload != null) {
            if (payload.startsWith("<")) {
                return ContentType.XML;
            } else if (payload.startsWith("{") || payload.startsWith("[")) {
                return ContentType.JSON;
            } else if (!payload.isEmpty()) {
                return ContentType.TEXT;
            }
        }
        return null;
    }
    
}
