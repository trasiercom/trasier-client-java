package com.trasier.client.auth;

public interface AuthInterceptor {

    String getToken();

    void refreshToken();

}
