package com.trasier.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trasier.client.api.Client;
import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.util.ProjectUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TrasierHttpClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierHttpClient.class);

    private final TrasierClientConfiguration clientConfiguration;
    private final ObjectMapper mapper;
    private final AsyncHttpClient client;
    private final OAuthTokenSafe tokenSafe;
    private final List<TrasierSpanInterceptor> spanInterceptors;
    private final String writerEndpointUrl;
    private final TrasierHttpClientHandler handler;

    public TrasierHttpClient(TrasierClientConfiguration clientConfiguration, TrasierEndpointConfiguration endpointConfiguration, OAuthTokenSafe tokenSafe, AsyncHttpClient client) {
        this.clientConfiguration = clientConfiguration;
        this.tokenSafe = tokenSafe;
        this.client = client;
        this.mapper = createObjectMapper();
        this.spanInterceptors = new ArrayList<>();
        this.writerEndpointUrl = createWriterEndpointUrl(clientConfiguration, endpointConfiguration);
        this.handler = new TrasierHttpClientHandler(clientConfiguration.getLogMetricsInterval());
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    @Override
    public boolean sendSpan(Span span) {
        return sendSpans(Collections.singletonList(span));
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        if (clientConfiguration.isActivated()) {
            //copy to be able to delete
            spans = new ArrayList<>(spans);
            applyInterceptors(spans);
            if (!spans.isEmpty()) {
                enrichSpans(spans);
                try {
                    return sendSpansInternal(spans);
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return false;
    }

    protected boolean sendSpansInternal(List<Span> spans) throws JsonProcessingException {
        String token = tokenSafe.getToken();
        if(token != null && !token.isEmpty()) {
            BoundRequestBuilder requestBuilder = client
                    .preparePost(writerEndpointUrl)
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", "Bearer " + token)
                    .setBody(mapper.writeValueAsBytes(spans));
            Request request = requestBuilder.build();
            client.executeRequest(request, handler);
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void addSpanInterceptor(TrasierSpanInterceptor interceptor) {
        this.spanInterceptors.add(interceptor);
    }

    private String createWriterEndpointUrl(TrasierClientConfiguration clientConfiguration, TrasierEndpointConfiguration endpointConfiguration) {
        String httpEndpoint = endpointConfiguration.getHttpEndpoint();
        if (clientConfiguration.getAccountId() != null) {
            httpEndpoint = httpEndpoint.replace("{accountId}", clientConfiguration.getAccountId());
        }
        if (clientConfiguration.getSpaceKey() != null) {
            httpEndpoint = httpEndpoint.replace("{spaceKey}", clientConfiguration.getSpaceKey());
        }
        return httpEndpoint;
    }

    private void applyInterceptors(List<Span> spans) {
        spans.removeIf(span -> {
            boolean cancel = span.isCancel();
            if (!cancel) {
                applyInterceptors(span);
                cancel = span.isCancel();
            }
            return cancel;
        });
    }

    private void applyInterceptors(Span span) {
        for (TrasierSpanInterceptor spanInterceptor : this.spanInterceptors) {
            spanInterceptor.intercept(span);
        }
    }

    private void enrichSpans(List<Span> spans) {
        spans.forEach(this::enrichVersion);
    }

    private void enrichVersion(Span span) {
        if (span.getTags() == null) {
            span.setTags(new HashMap<>());
        }
        String spanKind = span.getTags().get("span.kind");
        if (spanKind == null) {
            spanKind = "-";
        }
        span.getTags().put("trasier_client." + spanKind, "trasier-core-ahc");
        span.getTags().put("trasier_client_version." + spanKind, ProjectUtils.getProjectVersion());
    }
}