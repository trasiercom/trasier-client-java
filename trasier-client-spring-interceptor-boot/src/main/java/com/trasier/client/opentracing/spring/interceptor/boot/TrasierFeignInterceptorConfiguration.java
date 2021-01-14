package com.trasier.client.opentracing.spring.interceptor.boot;

import com.trasier.opentracing.feign.interceptor.InterceptorFeignConfiguration;
import feign.Request;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.feign.FeignTracingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass({Request.class, FeignSpanDecorator.class})
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(FeignTracingAutoConfiguration.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
@ConditionalOnProperty(name = "opentracing.spring.cloud.feign.enabled", havingValue = "true", matchIfMissing = true)
@Import(InterceptorFeignConfiguration.class)
public class TrasierFeignInterceptorConfiguration {
}