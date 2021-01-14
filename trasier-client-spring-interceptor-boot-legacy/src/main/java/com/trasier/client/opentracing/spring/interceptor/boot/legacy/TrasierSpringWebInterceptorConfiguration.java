package com.trasier.client.opentracing.spring.interceptor.boot.legacy;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierFilterConfigurations;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import com.trasier.opentracing.spring.interceptor.InterceptorWebConfiguration;
import com.trasier.opentracing.spring.interceptor.servlet.TrasierBufferFilter;
import com.trasier.opentracing.spring.interceptor.servlet.TrasierServletFilterSpanDecorator;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.starter.WebClientTracingProperties;
import io.opentracing.contrib.spring.web.starter.WebTracingProperties;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Configuration
@ConditionalOnBean({Tracer.class})
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnProperty(prefix = WebClientTracingProperties.CONFIGURATION_PREFIX, name = "enabled", matchIfMissing = true)
@Import(InterceptorWebConfiguration.class)
public class TrasierSpringWebInterceptorConfiguration {
    @Autowired
    private List<org.springframework.boot.web.servlet.FilterRegistrationBean> filterRegistrationBeanList;

    @Autowired
    private ObjectProvider<List<ServletFilterSpanDecorator>> servletFilterSpanDecorator;

    @Autowired(required = false)
    private List<TrasierSpanResolverInterceptor> samplingInterceptors;

    @Bean
    public FilterRegistrationBean trasierBufferFilter(TrasierClientConfiguration configuration, TrasierFilterConfigurations filterConfigurations, WebTracingProperties tracingConfiguration) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(calculateAndFixOrder(tracingConfiguration));
        registrationBean.setFilter(new TrasierBufferFilter(configuration, filterConfigurations));
        registrationBean.setUrlPatterns(tracingConfiguration.getUrlPatterns());
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean tracingFilter(TrasierClientConfiguration configuration, Tracer tracer, WebTracingProperties tracingConfiguration) {
        List<ServletFilterSpanDecorator> decorators = servletFilterSpanDecorator.getIfAvailable();
        if (CollectionUtils.isEmpty(decorators)) {
            decorators = new ArrayList<>();
            decorators.add(ServletFilterSpanDecorator.STANDARD_TAGS);
            decorators.add(new TrasierServletFilterSpanDecorator(configuration, samplingInterceptors == null ? Collections.emptyList(): samplingInterceptors));
        }

        TracingFilter tracingFilter = new TracingFilter(tracer, decorators, Pattern.compile(tracingConfiguration.getSkipPattern()));

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(tracingFilter);
        filterRegistrationBean.setUrlPatterns(tracingConfiguration.getUrlPatterns());
        filterRegistrationBean.setOrder(tracingConfiguration.getOrder());
        filterRegistrationBean.setAsyncSupported(true);

        return filterRegistrationBean;
    }

    private int calculateAndFixOrder(WebTracingProperties tracingConfiguration) {
        int configuredTracingOrder = tracingConfiguration.getOrder();
        fixOpentracingFilterOrder(tracingConfiguration, configuredTracingOrder);
        return configuredTracingOrder;
    }

    private void fixOpentracingFilterOrder(WebTracingProperties tracingConfiguration, int configuredTracingOrder) {
        int fixedOrder = configuredTracingOrder + 1;
        tracingConfiguration.setOrder(fixedOrder);
        if (filterRegistrationBeanList != null) {
            filterRegistrationBeanList.stream()
                    .filter(regBean -> regBean.getFilter() instanceof TracingFilter)
                    .forEach(filterRegistrationBean -> filterRegistrationBean.setOrder(fixedOrder));
        }
    }

}