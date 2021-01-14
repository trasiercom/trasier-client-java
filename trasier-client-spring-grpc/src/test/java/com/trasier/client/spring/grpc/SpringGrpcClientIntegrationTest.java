package com.trasier.client.spring.grpc;

import com.trasier.client.api.ContentType;
import com.trasier.client.api.Endpoint;
import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpringGrpcClientIntegrationTest {

    @Test
    @Ignore
    public void sendSpanOneByOne() throws InterruptedException, SSLException {
        TrasierSpringGrpcConfiguration config = new TrasierSpringGrpcConfiguration();

        TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();
        appConfig.setGrpcEndpoint("trasier-dev-writer.app.trasier.com:8082");

        TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
        clientConfig.setActivated(true);
        clientConfig.setAccountId("123456");
        clientConfig.setClientId("clientId");
        clientConfig.setSpaceKey("my-space");
        clientConfig.setSystemName("ping");
        clientConfig.setClientSecret("abcd1234");

        final String token = "secret-token";
        TrasierAuthClientInterceptor authClientInterceptor = new TrasierAuthClientInterceptor(clientConfig, (OAuthTokenSafe) null);
        TrasierSpringGrpcClient client = new TrasierSpringGrpcClient(clientConfig, appConfig, authClientInterceptor);

        Span.SpanBuilder spanBuilder = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GIVE_50_CHF").startTimestamp(System.currentTimeMillis());

        spanBuilder.incomingEndpoint(new Endpoint("Frank"));
        spanBuilder.incomingContentType(ContentType.XML);
        spanBuilder.incomingData("<chf>50</chf>");

        Span span = spanBuilder.build();
        System.out.println(span.getConversationId());

        System.out.println("RQ: " + span);

        // application service call to trace happens here
        Thread.sleep(500);

        spanBuilder.outgoingContentType(ContentType.XML);
        spanBuilder.status("ERROR");
        spanBuilder.outgoingData("<response>Sorry, I'm broke!</response>");

        client.sendSpan(spanBuilder.build());
        client.sendSpan(spanBuilder.status(Boolean.TRUE.toString()).build());
        System.out.println("RS: " + spanBuilder.build());

        TimeUnit.SECONDS.sleep(3);

        client.close();
    }
}