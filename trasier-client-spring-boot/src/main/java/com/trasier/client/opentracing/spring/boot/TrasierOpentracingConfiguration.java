package com.trasier.client.opentracing.spring.boot;

import com.trasier.client.api.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierEndpointConfiguration;
import com.trasier.client.configuration.TrasierFilterConfigurations;
import com.trasier.client.configuration.TrasierProxyConfiguration;
import com.trasier.client.opentracing.TrasierScopeManager;
import com.trasier.client.opentracing.TrasierTracer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(name = "io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration")
public class TrasierOpentracingConfiguration {

    @Bean
    @ConfigurationProperties("trasier.client")
    public TrasierClientConfiguration trasierSpringClientConfiguration() {
        return new TrasierClientConfiguration();
    }

    @Bean
    @ConfigurationProperties("trasier.endpoint")
    protected TrasierEndpointConfiguration endpointConfig() {
        return new TrasierEndpointConfiguration();
    }

    @Bean
    @ConfigurationProperties("trasier.proxy")
    public TrasierProxyConfiguration trasierSpringProxyConfiguration() {
        return new TrasierProxyConfiguration();
    }

    @Bean
    public TrasierTracer trasierTracer(Client client, TrasierClientConfiguration configuration) {
        return new TrasierTracer(client, configuration, new TrasierScopeManager());
    }

    @Bean
    @ConfigurationProperties(prefix = "trasier.client.filters")
    public TrasierFilterConfigurations trasierFilterConfigurations() {
        return new TrasierFilterConfigurations();
    }

}
