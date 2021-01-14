package com.trasier.client.configuration;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class ApplicationConfigurationTest {

    @Test
    public void shouldUseDefaultProperties() {
        // given
        TrasierEndpointConfiguration sut = new TrasierEndpointConfiguration();

        // when / then
        assertTrue(sut.getAuthEndpoint().length() > 1);
        assertTrue(sut.getHttpEndpoint().length() > 1);
    }

}