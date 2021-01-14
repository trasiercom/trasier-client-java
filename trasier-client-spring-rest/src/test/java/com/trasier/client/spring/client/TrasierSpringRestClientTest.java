package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.http.TrasierHttpClient;
import com.trasier.client.spring.rest.TrasierSpringRestClient;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TrasierSpringRestClientTest {

    private TrasierSpringRestClient sut;
    private TrasierClientConfiguration clientConfig;
    private AsyncHttpClient httpClient = mock(AsyncHttpClient.class);
    private Response response = mock(Response.class);

    @Before
    public void setup() throws InterruptedException, ExecutionException, TimeoutException {
        TrasierEndpointConfiguration endpointConfiguration = new TrasierEndpointConfiguration();
        clientConfig = new TrasierClientConfiguration();
        clientConfig.setActivated(true);
        clientConfig.setAccountId("123");
        clientConfig.setClientId("clientId");
        clientConfig.setSpaceKey("my-space");
        clientConfig.setSystemName("ping");
        clientConfig.setClientSecret("abcd1234");
        OAuthTokenSafe tokenSafe = mock(OAuthTokenSafe.class);
        Mockito.when(tokenSafe.getToken()).thenReturn("a-token");
        TrasierHttpClient trasierHttpClient = new TrasierHttpClient(clientConfig, endpointConfiguration, tokenSafe, httpClient);

        ListenableFuture<Response> future = Mockito.mock(ListenableFuture.class);
        Request request = Mockito.mock(Request.class);
        BoundRequestBuilder requestBuilder = Mockito.mock(BoundRequestBuilder.class);
        Mockito.when(future.get(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(response);
        Mockito.when(httpClient.preparePost(ArgumentMatchers.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setReadTimeout(ArgumentMatchers.anyInt())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setRequestTimeout(ArgumentMatchers.anyInt())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setCharset(ArgumentMatchers.any())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setHeader(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.setBody(ArgumentMatchers.any(byte[].class))).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.build()).thenReturn(request);
        Mockito.when(httpClient.executeRequest(ArgumentMatchers.any(Request.class))).thenReturn(future);

        sut = new TrasierSpringRestClient(trasierHttpClient, null);
    }

    @Test
    public void testShouldNotSendSpansWhenDeactivated() {
        // given
        clientConfig.setActivated(false);
        Span span = Span.newSpan("", "", "", "").build();

        // when
        sut.sendSpan(span);

        // then
        verify(httpClient, times(0)).preparePost(anyString());
        verify(httpClient, times(0)).executeRequest(any(Request.class), any());
    }

    @Test
    public void testShouldSendAndEnrichSpan() {
        // given
        clientConfig.setActivated(true);
        Span span = Span.newSpan("", "", "", "").build();

        // when
        sut.sendSpan(span);

        // then
        assertNotNull(span.getTags().get("trasier_client.-"));
        verify(httpClient, times(1)).preparePost(anyString());
        verify(httpClient, times(1)).executeRequest(any(Request.class), any());
    }

    @Test
    public void testShouldSendAndEnrichSpansOnServerSide() {
        // given
        clientConfig.setActivated(true);
        Span span = Span.newSpan("", "", "", "").build();
        span.setTags(new HashMap<>());
        span.getTags().put("span.kind", "server");

        // when
        sut.sendSpan(span);

        // then
        assertNotNull(span.getTags().get("trasier_client.server"));
        verify(httpClient, times(1)).executeRequest(any(Request.class), any());
    }

    @Test
    public void doNothingOnEmptyCollection() {
        // given
        clientConfig.setActivated(true);

        // when
        sut.sendSpans(Collections.emptyList());

        // then
        verify(httpClient, times(0)).preparePost(anyString());
        verify(httpClient, times(0)).executeRequest(any(Request.class), any());
    }

}