package com.trasier.opentracing.spring;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.opentracing.TrasierTracer;
import com.trasier.client.opentracing.spring.boot.TrasierOpentracingConfiguration;
import com.trasier.client.spring.rest.TrasierSpringRestConfiguration;
import com.trasier.client.spring.spancontrol.TrasierSpanFilterInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@Import({TrasierOpentracingConfiguration.class, TrasierSpringRestConfiguration.class})
public class TrasierConfigurationTest {

    @Autowired
    private TrasierClientConfiguration trasierSpringClientConfiguration;

    @Autowired
    private TrasierEndpointConfiguration endpointConfig;

    @Autowired
    private TrasierTracer trasierTracer;

    @Autowired
    private TrasierSpanFilterInterceptor spanFilterInterceptor;

    @Test
    public void testAutoconfigWorks() {
        assertNotNull(trasierSpringClientConfiguration);
        assertNotNull(endpointConfig);
        assertNotNull(trasierTracer);
        assertNotNull(spanFilterInterceptor);
    }

}
