package com.trasier.client.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.configuration.TrasierClientConfiguration;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class OAuthTokenSafe {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenSafe.class);
    private static final int EXPIRES_IN_TOLERANCE = 60;

    private final TrasierClientConfiguration clientConfiguration;
    private final String authUrl;
    private final AsyncHttpClient client;
    private final ObjectMapper mapper;

    private OAuthToken token;
    private AsyncHandler<Void> asyncHandler;
    private long tokenExpiresAt;
    private long refreshTokenExpiresAt;
    private AtomicBoolean isFeatching = new AtomicBoolean(false);

    public OAuthTokenSafe(TrasierClientConfiguration clientConfiguration, String authUrl, AsyncHttpClient client) {
        this.clientConfiguration = clientConfiguration;
        this.authUrl = authUrl;
        this.client = client;
        this.mapper = new ObjectMapper();
        this.asyncHandler = new AsyncTokenHandler();
    }

    public String getToken() {
        if (isTokenInvalid()) {
            refreshToken();
        }
        return token != null ? token.getAccessToken() : null;
    }

    public void refreshToken() {
        if (isTokenInvalid()) {
            if (!isFeatching.getAndSet(true)) {
                if (isTokenInvalid()) {
                    try {
                        String basicAuth = Base64.getEncoder().encodeToString((clientConfiguration.getClientId() + ":" + clientConfiguration.getClientSecret()).getBytes());
                        Request request = createRequest(basicAuth, createTokenRequest());
                        client.executeRequest(request, asyncHandler);
                    } catch (Exception e) {
                        LOGGER.error("Could not fetch token, maybe you need to set a proxy or consider disabling trasier.", e);
                    }
                }
            }
        }
    }

    Request createRequest(String basicAuth, String tokenRequest) {
        BoundRequestBuilder requestBuilder = client
                .preparePost(authUrl)
                .setReadTimeout(5000)
                .setRequestTimeout(5000)
                .setCharset(StandardCharsets.UTF_8)
                .setHeader("Authorization", "Basic " + basicAuth)
                //.setFormParams(formParams) // TODO Use formParams instead of concatenated body
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setBody(tokenRequest);
        return requestBuilder.build();
    }

    String createTokenRequest() {
        String tokenRequest = "scope=&client_id=" + clientConfiguration.getClientId();

        if (isRefreshTokenInvalid()) {
            tokenRequest += "&grant_type=client_credentials";
        } else {
            tokenRequest += "&grant_type=refresh_token";
            tokenRequest += "&refresh_token=" + token.getRefreshToken();
        }

        return tokenRequest;
    }

    private boolean isTokenInvalid() {
        return token == null || tokenExpiresAt < System.currentTimeMillis();
    }

    private boolean isRefreshTokenInvalid() {
        return token == null || token.getRefreshToken() == null || refreshTokenExpiresAt < System.currentTimeMillis();
    }

    private class AsyncTokenHandler extends AsyncCompletionHandler<Void> {

        @Override
        public Void onCompleted(Response response) throws Exception {
            try {
                int responseCode = response.getStatusCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    OAuthToken token = mapper.readValue(response.getResponseBody(), OAuthToken.class);
                    if (token != null) {
                        long tokenIssued = System.currentTimeMillis();
                        OAuthTokenSafe.this.token = token;
                        OAuthTokenSafe.this.tokenExpiresAt = tokenIssued + ((Long.parseLong(token.getExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
                        OAuthTokenSafe.this.refreshTokenExpiresAt = tokenIssued + ((Long.parseLong(token.getRefreshExpiresIn()) - EXPIRES_IN_TOLERANCE) * 1000);
                    } else {
                        throw new IOException("Cannot parse token.");
                    }
                } else {
                    throw new IOException("Cannot fetch token. Responce code: " + responseCode);
                }
            } catch(Exception e) {
                LOGGER.error("Could not fetch token -> resetting.", e);

                OAuthTokenSafe.this.token = null;
                OAuthTokenSafe.this.tokenExpiresAt = 0;
                OAuthTokenSafe.this.refreshTokenExpiresAt = 0;
            } finally {
                isFeatching.getAndSet(false);
            }

            return null;
        }

        @Override
        public void onThrowable(Throwable t) {
            isFeatching.getAndSet(false);
            LOGGER.error("Could not fetch token, maybe you need to set a proxy or consider disabling trasier.", t);
        }
    }

}