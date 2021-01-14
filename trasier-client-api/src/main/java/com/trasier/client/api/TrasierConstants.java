package com.trasier.client.api;

import java.util.regex.Pattern;

public interface TrasierConstants {
    String HEADER_CONVERSATION_SAMPLE = "X-Conversation-Sample";
    String HEADER_CONVERSATION_ID = "X-Conversation-Id";
    String HEADER_TRACE_ID = "X-Trace-Id";
    String HEADER_SPAN_ID = "X-Span-Id";
    String HEADER_INCOMING_ENDPOINT_NAME = "X-Incoming-Endpoint-Name";

    String UNKNOWN_IN = "UNKNOWN_IN";
    String UNKNOWN_OUT = "UNKNOWN_OUT";
    String UNKNOWN_WS_CALL = "UNKNOWN_WS_CALL";

    String STATUS_ERROR = "ERROR";
    String STATUS_OK = "OK";

    Pattern DEFAULT_SKIP_PATTERN = Pattern.compile(
            "/api-docs.*|/autoconfig|/configprops|/dump|/health|/info|/metrics.*|" +
                    ".*/healthCheckServlet|.*/checkServlet|/admin/check|/actuatorhealth|" +
                    "/hystrix.stream|/mappings|/swagger.*|" +
                    ".*\\.wsdl|.*\\.xsd|.*\\.png|.*\\.css|.*\\.js|.*\\.html|/favicon.ico");
}