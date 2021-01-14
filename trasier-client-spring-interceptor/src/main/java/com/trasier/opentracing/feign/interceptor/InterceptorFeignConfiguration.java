package com.trasier.opentracing.feign.interceptor;

import com.trasier.client.configuration.TrasierClientConfiguration;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.feign.TrasierFeignContextBeanPostProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class InterceptorFeignConfiguration {

    @Autowired
    private TrasierClientConfiguration configuration;

    @Autowired
    @Lazy
    private Tracer tracer;

    @Bean
    public FeignSpanDecorator trasierFeignSpanDecorator() {
        return createSpanDecorator();
    }

    @Bean
    public BeanPostProcessor trasierFeignTracingAutoConfiguration(BeanFactory beanFactory) {
        List<FeignSpanDecorator> spanDecorators = new ArrayList<>();
        spanDecorators.add(new FeignSpanDecorator.StandardTags());
        spanDecorators.add(createSpanDecorator());
        return new TrasierFeignContextBeanPostProcessor(tracer, beanFactory, spanDecorators);
    }

    private TrasierFeignSpanDecorator createSpanDecorator() {
        return new TrasierFeignSpanDecorator(configuration, tracer);
    }

}