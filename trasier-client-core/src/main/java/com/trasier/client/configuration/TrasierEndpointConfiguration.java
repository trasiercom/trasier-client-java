package com.trasier.client.configuration;

public class TrasierEndpointConfiguration {

    private static final String DEFAULT_HTTP_ENDPOINT = "https://writer.trasier.com/api/accounts/{accountId}/spaces/{spaceKey}/spans";
    private static final String DEFAULT_GRPC_ENDPOINT = "grpc.trasier.com:443";
    private static final String DEFAULT_AUTH_ENDPOINT = "https://auth.trasier.com/auth/realms/trasier-prod/protocol/openid-connect/token";

    private String httpEndpoint = DEFAULT_HTTP_ENDPOINT;
    private String grpcEndpoint = DEFAULT_GRPC_ENDPOINT;
    private String authEndpoint = DEFAULT_AUTH_ENDPOINT;

    public String getHttpEndpoint() {
        return httpEndpoint;
    }

    public void setHttpEndpoint(String httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

    public String getGrpcEndpoint() {
        return grpcEndpoint;
    }

    public void setGrpcEndpoint(String grpcEndpoint) {
        this.grpcEndpoint = grpcEndpoint;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

}