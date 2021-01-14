package com.trasier.client.spring.client;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.spring.rest.TrasierSpringRestClient;
import com.trasier.client.spring.rest.TrasierSpringRestConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TrasierSpringRestConfiguration.class, TrasierClientConfiguration.class, TrasierEndpointConfiguration.class})
public class SpringRestClientIntegrationTest {
    @Autowired
    private TrasierSpringRestClient client;

    @Test
    @Ignore
    public void sendSpanOneByOne() throws InterruptedException {
        Span.SpanBuilder spanBuilder = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GIVE_50_CHF").startTimestamp(System.currentTimeMillis());

        spanBuilder.incomingEndpoint(new Endpoint("Frank"));
        spanBuilder.incomingContentType(ContentType.XML);
        spanBuilder.incomingData("<chf>50</chf>");

        Span span = spanBuilder.build();
        java.lang.System.out.println(span.getConversationId());

        java.lang.System.out.println("RQ: " + span);

        // application service call to trace happens here
        Thread.sleep(500);

        spanBuilder.outgoingContentType(ContentType.XML);
        spanBuilder.status("ERROR");
        spanBuilder.outgoingData("<response>Sorry, I'm broke!</response>");

        client.sendSpan(spanBuilder.build());
        client.sendSpan(spanBuilder.status(Boolean.TRUE.toString()).build());
        java.lang.System.out.println("RS: " + spanBuilder.build());
    }

    @Test
    @Ignore
    public void sendSpansBulk() throws InterruptedException {
        Span.SpanBuilder spanBuilder = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GIVE_50_CHF").endTimestamp(System.currentTimeMillis());

        spanBuilder.incomingEndpoint(new Endpoint("Frank"));
        spanBuilder.incomingContentType(ContentType.XML);
        spanBuilder.incomingData("<chf>50</chf>");

        Span span = spanBuilder.build();
        java.lang.System.out.println(span.getConversationId());

        // application service call to trace happens here
        Thread.sleep(500);

        spanBuilder.outgoingContentType(ContentType.XML);
        spanBuilder.status("ERROR");
        spanBuilder.outgoingData("<response>Sorry, I'm broke!</response>");

        List<Span> spans = new ArrayList<>();
        spans.add(spanBuilder.build());
        spanBuilder.id(UUID.randomUUID().toString());
        spans.add(spanBuilder.build());

        java.lang.System.out.println("Sending spans: " + spans);

        client.sendSpans(spans);
    }
}