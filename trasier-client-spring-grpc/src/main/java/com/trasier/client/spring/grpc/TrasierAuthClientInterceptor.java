package com.trasier.client.spring.grpc;

import com.trasier.client.auth.OAuthTokenSafe;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.http.AsyncHttpClientFactory;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.asynchttpclient.AsyncHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TrasierAuthClientInterceptor implements ClientInterceptor {

    private final TrasierClientConfiguration clientConfiguration;
    private final OAuthTokenSafe oAuthTokenSafe;

    @Autowired
    public TrasierAuthClientInterceptor(TrasierClientConfiguration clientConfiguration, TrasierEndpointConfiguration endpointConfiguration) {
        this.clientConfiguration = clientConfiguration;
        AsyncHttpClient client = AsyncHttpClientFactory.createDefaultClient(clientConfiguration);
        this.oAuthTokenSafe = new OAuthTokenSafe(clientConfiguration, endpointConfiguration.getAuthEndpoint(), client);
    }

    public TrasierAuthClientInterceptor(TrasierClientConfiguration clientConfiguration, OAuthTokenSafe tokenSafe) {
        this.clientConfiguration = clientConfiguration;
        this.oAuthTokenSafe = tokenSafe;
    }

    @PostConstruct
    public void init() {
        if (clientConfiguration.isActivated()) {
            oAuthTokenSafe.refreshToken();
        }
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ClientInterceptors.CheckedForwardingClientCall(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            protected void checkedStart(Listener listener, Metadata metadata) {
                String token = oAuthTokenSafe.getToken();
                if(token != null && !token.isEmpty()) {
                    metadata.put(Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER), token);
                    metadata.put(Metadata.Key.of("accountId", Metadata.ASCII_STRING_MARSHALLER), clientConfiguration.getAccountId());
                    metadata.put(Metadata.Key.of("spaceKey", Metadata.ASCII_STRING_MARSHALLER), clientConfiguration.getSpaceKey());
                    delegate().start(listener, metadata);
                }
            }
        };
    }

}