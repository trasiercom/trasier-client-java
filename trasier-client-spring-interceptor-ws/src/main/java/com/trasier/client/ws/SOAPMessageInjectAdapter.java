
package com.trasier.client.ws;

import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import io.opentracing.propagation.TextMap;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class SOAPMessageInjectAdapter implements TextMap {

    private final TrasierClientConfiguration clientConfig;
    private final Span span;

    SOAPMessageInjectAdapter(SOAPMessageContext context, TrasierClientConfiguration clientConfig, Span span) {
        this.clientConfig = clientConfig;
        this.span = span;
        initHeaderMap(context);
        if (span.getIncomingHeader() == null) {
            span.setIncomingHeader(new HashMap<>());
        }
    }

    private Map<String, List<String>> initHeaderMap(SOAPMessageContext context) {
        Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
        if (headers == null) {
            headers = new HashMap<>();
            context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        }
        headers.put(TrasierConstants.HEADER_CONVERSATION_ID, Arrays.asList(span.getConversationId()));
        headers.put(TrasierConstants.HEADER_TRACE_ID, Arrays.asList(span.getTraceId()));
        headers.put(TrasierConstants.HEADER_SPAN_ID, Arrays.asList(span.getId()));
        if (clientConfig.getSystemName() != null) {
            headers.put(TrasierConstants.HEADER_INCOMING_ENDPOINT_NAME, Arrays.asList(clientConfig.getSystemName()));
        }
        return headers;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return null;
    }

    @Override
    public void put(String key, String value) {
        if (value != null) {
            span.getIncomingHeader().put(key, value);
        }
    }

}
