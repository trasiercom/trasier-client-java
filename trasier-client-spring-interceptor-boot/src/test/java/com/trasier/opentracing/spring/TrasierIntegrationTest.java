package com.trasier.opentracing.spring;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.opentracing.TrasierScopeManager;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.opentracing.spring.boot.TrasierOpentracingConfiguration;
import com.trasier.client.spring.rest.TrasierSpringRestConfiguration;
import com.trasier.opentracing.spring.interceptor.InterceptorWebConfiguration;
import com.trasier.opentracing.spring.testsupport.ClientCollector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.trasier.client.api.TrasierConstants.HEADER_CONVERSATION_ID;
import static com.trasier.client.api.TrasierConstants.HEADER_SPAN_ID;
import static com.trasier.client.api.TrasierConstants.HEADER_TRACE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@Import({TrasierOpentracingConfiguration.class, TrasierSpringRestConfiguration.class, InterceptorWebConfiguration.class})
public class TrasierIntegrationTest {

    public static final String MOCKED_HTTP_ENDPOINT = "http://localhost:76762/submit";
    private static ClientCollector clientCollector = new ClientCollector();

    @TestConfiguration
    static class TestContextConfiguration {

        @Bean
        public RestTemplate testRestTemplate() {
            HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            httpRequestFactory.setConnectionRequestTimeout(200);
            httpRequestFactory.setConnectTimeout(200);
            httpRequestFactory.setReadTimeout(200);
            return new RestTemplate(httpRequestFactory);
        }

        @Bean
        public TrasierTracer tracer() {
            return new TrasierTracer(clientCollector, new TrasierClientConfiguration(), new TrasierScopeManager());
        }
    }

    @Autowired
    @Qualifier("testRestTemplate")
    private RestTemplate testRestTemplate;

    @Autowired
    private TrasierClientConfiguration configuration;

    @Before
    public void setUp() throws URISyntaxException {
        configuration.setActivated(true);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(testRestTemplate);
        mockServer.expect(ExpectedCount.manyTimes(),
                requestTo(new URI(MOCKED_HTTP_ENDPOINT)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{body: response}"));
    }

    @After
    public void tearDown() {
        clientCollector.getSendSpans().clear();
    }

    @Test
    public void testSendRestRequest() {
        // when
        this.executeRestCall("{body: request}", null);

        // then
        List<Span> sendSpans = clientCollector.getSendSpans();
        assertEquals(1, sendSpans.size());

        Span createdSpan = sendSpans.get(0);
        assertNotNull(createdSpan.getId());
        assertNotNull(createdSpan.getTraceId());
        assertNotNull(createdSpan.getConversationId());

        assertEquals(createdSpan.getIncomingHeader().get("X-Conversation-Id"), createdSpan.getConversationId());
        assertEquals(createdSpan.getIncomingHeader().get("X-Trace-Id"), createdSpan.getTraceId());
        assertEquals(createdSpan.getIncomingHeader().get("X-Span-Id"), createdSpan.getId());
        assertEquals("/submit", createdSpan.getName());
        assertEquals(ContentType.JSON, createdSpan.getIncomingContentType());
        assertEquals(ContentType.JSON, createdSpan.getOutgoingContentType());
        assertEquals("{body: request}", createdSpan.getIncomingData());
        assertEquals("{body: response}", createdSpan.getOutgoingData());
    }

    @Test
    public void testCreateChildSpans() {
        // when
        String conversationId = "c3e5e09a-4640-4366-a5b2-83ec5d37503a";
        String traceIdId = "eeb3d06b-ba22-4e05-8a16-56180b09fc48";
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_CONVERSATION_ID, conversationId);
        headers.set(HEADER_TRACE_ID, traceIdId);

        executeRestCall("Call 1", headers);

        // then
        List<Span> sendSpans = clientCollector.getSendSpans();
        assertEquals(1, sendSpans.size());

        Span parentSpan = sendSpans.get(0);
        assertNotNull(parentSpan.getId());
        assertNotNull(parentSpan.getTraceId());
        assertNotNull(parentSpan.getConversationId());

        assertEquals(parentSpan.getIncomingHeader().get(HEADER_CONVERSATION_ID), conversationId);
        assertEquals(parentSpan.getIncomingHeader().get(HEADER_TRACE_ID), traceIdId);
        assertEquals(parentSpan.getIncomingHeader().get(HEADER_SPAN_ID), parentSpan.getId());
        assertEquals("/submit", parentSpan.getName());
        assertEquals("Call 1", parentSpan.getIncomingData());

        // and when
        executeRestCall("Call 2", headers);

        // then
        sendSpans = clientCollector.getSendSpans();
        assertEquals(2, sendSpans.size());

        Span childSpan = sendSpans.get(1);
        assertNotNull(childSpan.getId());
        assertNotNull(childSpan.getTraceId());
        assertNotNull(childSpan.getConversationId());

        assertEquals(childSpan.getIncomingHeader().get(HEADER_CONVERSATION_ID), conversationId);
        assertEquals(childSpan.getIncomingHeader().get(HEADER_TRACE_ID), traceIdId);
        assertEquals(childSpan.getIncomingHeader().get(HEADER_SPAN_ID), childSpan.getId());
        assertEquals("/submit", childSpan.getName());
        assertEquals("Call 2", childSpan.getIncomingData());
    }

    private void executeRestCall(String requestBody, HttpHeaders headers) {
        try {
            if (headers != null) {
                HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
                testRestTemplate.exchange(MOCKED_HTTP_ENDPOINT, HttpMethod.POST, entity, String.class);
            } else {
                testRestTemplate.postForEntity(MOCKED_HTTP_ENDPOINT, requestBody, null, String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
