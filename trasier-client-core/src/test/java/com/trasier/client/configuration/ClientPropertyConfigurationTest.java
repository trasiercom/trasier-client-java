package com.trasier.client.configuration;

import org.junit.After;
import org.junit.Before;

public class ClientPropertyConfigurationTest {

    @Before
    public void setup() {
        System.setProperty("trasier.client.clientId", "client-id");
        System.setProperty("trasier.client.clientSecret", "client-secret");
        System.setProperty("trasier.client.accountId", "111");
        System.setProperty("trasier.client.spaceKey", "space");
    }

    @After
    public void cleanup() {
        System.setProperty("trasier.client.clientId", "");
        System.setProperty("trasier.client.clientSecret", "");
        System.setProperty("trasier.client.spaceKey", "");
    }

}