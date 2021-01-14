package com.trasier.client.auth;

import org.junit.Assert;
import org.junit.Test;

public class OAuthTokenTest {
    @Test
    public void getAccessToken() {
        OAuthToken token = new OAuthToken();
        token.setAccessToken("at");
        Assert.assertEquals("at", token.getAccessToken());
    }

    @Test
    public void getExpiresIn() {
        OAuthToken token = new OAuthToken();
        token.setExpiresIn("1");
        Assert.assertEquals("1", token.getExpiresIn());
    }

    @Test
    public void getRefreshToken() {
        OAuthToken token = new OAuthToken();
        token.setRefreshToken("rt");
        Assert.assertEquals("rt", token.getRefreshToken());
    }

    @Test
    public void getRefreshExpiresIn() {
        OAuthToken token = new OAuthToken();
        token.setRefreshExpiresIn("2");
        Assert.assertEquals("2", token.getRefreshExpiresIn());
    }
}