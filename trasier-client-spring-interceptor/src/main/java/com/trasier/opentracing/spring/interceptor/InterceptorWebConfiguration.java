package com.trasier.opentracing.spring.interceptor;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import com.trasier.opentracing.spring.interceptor.rest.TrasierClientRequestInterceptor;
import com.trasier.opentracing.spring.interceptor.servlet.TrasierServletFilterSpanDecorator;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.RestTemplateSpanDecorator;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
public class InterceptorWebConfiguration {
    @Autowired
    private Tracer tracer;

    @Autowired
    private TrasierClientConfiguration configuration;

    @Autowired(required = false)
    private Set<RestTemplate> restTemplates;

    @Autowired(required = false)
    private List<RestTemplateSpanDecorator> spanDecorators;

    @Autowired(required = false)
    private List<TrasierSpanResolverInterceptor> samplingFilter;

    @Bean
    public TrasierServletFilterSpanDecorator trasierServletFilterSpanDecorator() {
        return new TrasierServletFilterSpanDecorator(configuration, samplingFilter == null ? Collections.emptyList() : samplingFilter);
    }

    @PostConstruct
    public void init() {
        if (restTemplates != null) {
            restTemplates.forEach(this::registerTracingInterceptor);
        }
    }

    public void registerTracingInterceptor(RestTemplate restTemplate) {
        if(configuration.isActivated()) {
            List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();
            if (existingInterceptors == null || notYetRegistered(existingInterceptors.stream(), TrasierClientRequestInterceptor.class)) {
                List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
                if (existingInterceptors != null) {
                    interceptors.addAll(existingInterceptors);
                }
                if (interceptors.stream().noneMatch(i -> i instanceof TracingRestTemplateInterceptor)) {
                    interceptors.add(new TracingRestTemplateInterceptor(tracer, spanDecorators == null ? Collections.emptyList() : spanDecorators));
                }
                if (interceptors.stream().noneMatch(i -> i instanceof TrasierClientRequestInterceptor)) {
                    interceptors.add(new TrasierClientRequestInterceptor(tracer, configuration, samplingFilter == null ? Collections.emptyList() : samplingFilter));
                }
                if (!(restTemplate.getRequestFactory() instanceof BufferingClientHttpRequestFactory)) {
                    restTemplate.setInterceptors(Collections.emptyList());
                    restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
                }
                restTemplate.setInterceptors(interceptors);

            }
        }
    }

    private boolean notYetRegistered(Stream<?> interceptors, Class<?> clazz) {
        return (interceptors).noneMatch(interceptor -> clazz.isAssignableFrom(interceptor.getClass()));
    }

}