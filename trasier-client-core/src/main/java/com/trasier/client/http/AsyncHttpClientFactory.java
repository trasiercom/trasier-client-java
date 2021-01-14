package com.trasier.client.http;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierProxyConfiguration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Realm;
import org.asynchttpclient.proxy.ProxyServer;

public final class AsyncHttpClientFactory {
    public static void setProxy(DefaultAsyncHttpClientConfig.Builder clientBuilder, TrasierProxyConfiguration trasierProxyConfiguration) {
        setProxy(clientBuilder, trasierProxyConfiguration.getHost(), trasierProxyConfiguration.getPort(), trasierProxyConfiguration.getUsername(), trasierProxyConfiguration.getPassword(), trasierProxyConfiguration.getScheme());
    }

    public static void setProxy(DefaultAsyncHttpClientConfig.Builder clientBuilder, String host, Integer port) {
        setProxy(clientBuilder, host, port, null, null, null);
    }

    public static void setProxy(DefaultAsyncHttpClientConfig.Builder clientBuilder, String host, Integer port, String username, String password, String scheme) {
        if(host != null && !host.trim().isEmpty() && port != null) {
            ProxyServer.Builder proxyServerBuilder = new ProxyServer.Builder(host.trim(), port);
            if (username != null && password != null && scheme != null) {
                Realm.Builder realm = new Realm.Builder(username, password);
                realm.setScheme(Realm.AuthScheme.valueOf(scheme));
                proxyServerBuilder.setRealm(realm.build());
            }
            clientBuilder.setProxyServer(proxyServerBuilder.build());
        }
    }

    public static DefaultAsyncHttpClientConfig.Builder createBuilder(TrasierClientConfiguration clientConfiguration) {
        //setting values via -Dorg.asynchttpclient.nameOfTheProperty is possible
        return Dsl.config()
                .setThreadPoolName("trasier")
                .setMaxConnections(clientConfiguration.getAhcMaxConnections())
                .setConnectTimeout(clientConfiguration.getAhcConnectTimeout())
                .setReadTimeout(clientConfiguration.getAhcReadTimeout())
                .setRequestTimeout(clientConfiguration.getAhcRequestTimeout())
                .setUseInsecureTrustManager(true);
    }

    public static AsyncHttpClient createClient(DefaultAsyncHttpClientConfig.Builder clientBuilder) {
        return Dsl.asyncHttpClient(clientBuilder);
    }

    public static AsyncHttpClient createDefaultClient(TrasierClientConfiguration clientConfiguration) {
        return AsyncHttpClientFactory.createClient(AsyncHttpClientFactory.createBuilder(clientConfiguration));
    }

}
