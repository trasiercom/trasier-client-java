package com.trasier.client.auth;

public class NoAuthInterceptor implements AuthInterceptor {

    @Override
    public String getToken() {
        throw new IllegalStateException("getToken() should not be called if NoAuth is used");
    }

    @Override
    public void refreshToken() {
    }
}
