package com.trasier.client.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrasierEndpointConfigurationTest {
    @Test
    public void getWriterEndpoint() {
        TrasierEndpointConfiguration configuration = new TrasierEndpointConfiguration();
        configuration.setHttpEndpoint("writer");
        assertEquals("writer", configuration.getHttpEndpoint());
    }

    @Test
    public void getAuthEndpoint() {
        TrasierEndpointConfiguration configuration = new TrasierEndpointConfiguration();
        configuration.setAuthEndpoint("auth");
        assertEquals("auth", configuration.getAuthEndpoint());
    }
}