package com.trasier.client.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import org.asynchttpclient.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OAuthTokenSafeTest {

    private TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();
    private TrasierEndpointConfiguration appConfig = new TrasierEndpointConfiguration();
    private ObjectMapper mapper = new ObjectMapper();
    private Response response = mock(Response.class);
    private AsyncHttpClient client;

    @Before
    public void init() {
        AsyncHttpClient httpClient = new DefaultAsyncHttpClient() {
            @Override
            public ListenableFuture<Response> executeRequest(Request request, AsyncHandler handler) {
                return null;
            }
        };
        AsyncHttpClient client =  spy(httpClient);
        when(client.executeRequest(any(org.asynchttpclient.Request.class), any())).thenAnswer(invocation -> {
            AsyncCompletionHandler<Response> handler = invocation.getArgument(1);
            handler.onCompleted(response);
            return null;
        });
        this.client = client;
    }

    @Test
    public void testRefreshTokenRequestedOnce() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (80 * 1000));
        token.setRefreshExpiresIn("" + (160 * 1000));

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when
        sut.getToken();
        sut.getToken();

        // then
        verify(client, times(1)).executeRequest(any(Request.class), any(AsyncHandler.class));
    }

    @Test
    public void testRefreshExpiredToken() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (-40 * 1000));

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when
        sut.getToken();
        sut.getToken();

        // then
        verify(client, times(2)).executeRequest(any(Request.class), any(AsyncHandler.class));
    }

    @Test
    public void testRefreshExpiredTokenValidRefreshToken() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (-80 * 1000));
        token.setRefreshExpiresIn("" + (80 * 1000));

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when 1
        String withoutTokenRequestEntity = sut.createTokenRequest();
        sut.getToken();

        // then 1
        Assert.assertTrue(withoutTokenRequestEntity.contains("client_credentials"));
        verify(client, times(1)).executeRequest(any(Request.class), any(AsyncHandler.class));

        // when 2
        String withInvalidTokenRequestEntity = sut.createTokenRequest();
        sut.getToken();

        // then 2
        Assert.assertTrue(withInvalidTokenRequestEntity.contains("refresh_token"));
        Assert.assertTrue(withInvalidTokenRequestEntity.contains("refreshTokenMock"));
        verify(client, times(2)).executeRequest(any(Request.class), any(AsyncHandler.class));
    }

    @Test
    public void testRefreshTokenKeepsFailing() {
        // given
        when(response.getStatusCode()).thenReturn(404);
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when
        String token1 = sut.getToken();
        String token2 = sut.getToken();

        // then
        assertNull(token1);
        assertNull(token2);
        verify(client, times(2)).executeRequest(any(Request.class), any(AsyncHandler.class));
    }

    @Test
    public void testRefreshTokenFailedOnceOnce() throws JsonProcessingException {
        // given
        OAuthToken token = new OAuthToken();
        token.setAccessToken("accessTokenMock");
        token.setRefreshToken("refreshTokenMock");
        token.setExpiresIn("" + (80 * 1000));
        token.setRefreshExpiresIn("" + (160 * 1000));

        when(response.getStatusCode()).thenReturn(404).thenReturn(200);
        when(response.getResponseBody()).thenReturn(mapper.writeValueAsString(token));
        OAuthTokenSafe sut = new OAuthTokenSafe(clientConfig, appConfig.getAuthEndpoint(), client);

        // when
        String token1 = sut.getToken();
        String token2 = sut.getToken();

        // then
        assertNull(token1);
        assertNotNull(token2);
        verify(client, times(2)).executeRequest(any(Request.class), any(AsyncHandler.class));
    }

}