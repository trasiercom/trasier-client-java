package com.trasier.client.opentracing.spring.interceptor.boot;

import com.trasier.opentracing.spring.interceptor.InterceptorWSConfiguration;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;

@Configuration
@ConditionalOnBean({Tracer.class})
@ConditionalOnClass({WebServiceTemplate.class, ClientInterceptorAdapter.class})
@ConditionalOnProperty(
        prefix = "opentracing.spring.web.client",
        name = {"enabled"},
        matchIfMissing = true
)
@AutoConfigureAfter({TrasierSpringWebInterceptorConfiguration.class})
@Import(InterceptorWSConfiguration.class)
public class TrasierSpringWSInterceptorConfiguration {
}