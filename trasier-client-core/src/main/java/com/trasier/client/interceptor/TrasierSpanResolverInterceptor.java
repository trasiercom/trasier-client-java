package com.trasier.client.interceptor;

import com.trasier.client.api.Span;

/**
 * Executed while the span data is resolved. This means the span information may not be
 * complete at this point.
 */
public interface TrasierSpanResolverInterceptor {

    /**
     * Intercepts when the request url was resolved (but before span metadata was resolved)
     * @param span - raw span, not filled with metadata
     * @param url - request url
     */
    void interceptUrlResolved(Span span, String url);

    /**
     * Resolved metadata like operation name, endpoints
     * @param span - span filled with metadata
     */
    void interceptMetadataResolved(Span span);


}
