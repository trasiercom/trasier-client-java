package com.trasier.client.spring.rest;

import com.trasier.client.api.Span;
import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.configuration.TrasierProxyConfiguration;
import com.trasier.client.http.AsyncHttpClientFactory;
import com.trasier.client.http.TrasierHttpClient;
import com.trasier.client.interceptor.TrasierCompressSpanInterceptor;
import com.trasier.client.interceptor.TrasierSpanInterceptor;
import com.trasier.client.spring.client.TrasierSpringClient;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("trasierSpringClient")
public class TrasierSpringRestClient implements TrasierSpringClient {

    private final TrasierHttpClient client;
    private TrasierCompressSpanInterceptor compressSpanInterceptor;

    @Autowired
    public TrasierSpringRestClient(TrasierEndpointConfiguration endpointConfiguration, TrasierClientConfiguration clientConfiguration, Optional<TrasierProxyConfiguration> optionalProxyConfiguration, Optional<List<TrasierSpanInterceptor>> optionalSpanInterceptors) {
        AsyncHttpClient client = createHttpClient(clientConfiguration, optionalProxyConfiguration);
        OAuthTokenSafe tokenSafe = new OAuthTokenSafe(clientConfiguration, endpointConfiguration.getAuthEndpoint(), client);
        TrasierHttpClient trasierHttpClient = new TrasierHttpClient(clientConfiguration, endpointConfiguration, tokenSafe, client);
        this.client = trasierHttpClient;
        if (clientConfiguration.isActivated()) {
            tokenSafe.refreshToken();
            optionalSpanInterceptors.ifPresent(it -> it.forEach(this.client::addSpanInterceptor));
            if (!clientConfiguration.isCompressPayloadDisabled()) {
                this.compressSpanInterceptor = new TrasierCompressSpanInterceptor();
            }
        }
    }

    protected AsyncHttpClient createHttpClient(TrasierClientConfiguration clientConfiguration, Optional<TrasierProxyConfiguration> optionalProxyConfiguration) {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = AsyncHttpClientFactory.createBuilder(clientConfiguration);
        if (optionalProxyConfiguration.isPresent() && optionalProxyConfiguration.get().getHost() != null && !optionalProxyConfiguration.get().getHost().trim().isEmpty()) {
            AsyncHttpClientFactory.setProxy(clientBuilder, optionalProxyConfiguration.get());
        } else {
            String httpProxyHost = System.getProperty("http.proxyHost");
            Integer httpProxyPort = Integer.getInteger("http.proxyPort");
            if (httpProxyHost != null && httpProxyPort != null) {
                AsyncHttpClientFactory.setProxy(clientBuilder, httpProxyHost, httpProxyPort);
            } else {
                String httpsProxyHost = System.getProperty("https.proxyHost");
                Integer httpsProxyPort = Integer.getInteger("https.proxyPort");
                if (httpsProxyHost != null && httpsProxyPort != null) {
                    AsyncHttpClientFactory.setProxy(clientBuilder, httpsProxyHost, httpsProxyPort);
                }
            }
        }
        return AsyncHttpClientFactory.createClient(clientBuilder);
    }

    public TrasierSpringRestClient(TrasierHttpClient client, TrasierCompressSpanInterceptor compressSpanInterceptor) {
        this.client = client;
        this.compressSpanInterceptor = compressSpanInterceptor;
    }

    @Override
    public boolean sendSpan(Span span) {
        if (span.isCancel()) {
            return false;
        }
        if (compressSpanInterceptor != null) {
            compressSpanInterceptor.intercept(span);
        }
        return client.sendSpan(span);
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        spans.removeIf(Span::isCancel);
        if (compressSpanInterceptor != null) {
            spans.forEach(compressSpanInterceptor::intercept);
        }

        return client.sendSpans(spans);
    }

    @Override
    public void close() {
        client.close();
    }

}