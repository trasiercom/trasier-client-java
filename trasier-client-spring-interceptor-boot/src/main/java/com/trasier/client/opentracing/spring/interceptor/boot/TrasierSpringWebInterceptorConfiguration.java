package com.trasier.client.opentracing.spring.interceptor.boot;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierFilterConfigurations;
import com.trasier.opentracing.spring.interceptor.InterceptorWebConfiguration;
import com.trasier.opentracing.spring.interceptor.servlet.TrasierBufferFilter;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.starter.WebClientTracingProperties;
import io.opentracing.contrib.spring.web.starter.WebTracingProperties;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@ConditionalOnBean({Tracer.class})
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnProperty(prefix = WebClientTracingProperties.CONFIGURATION_PREFIX, name = "enabled", matchIfMissing = true)
@Import(InterceptorWebConfiguration.class)
public class TrasierSpringWebInterceptorConfiguration {
    @Autowired
    private List<FilterRegistrationBean> filterRegistrationBeanList;

    @Bean
    public FilterRegistrationBean trasierBufferFilter(TrasierClientConfiguration configuration, TrasierFilterConfigurations filterConfigurations, WebTracingProperties tracingConfiguration) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(calculateAndFixOrder(tracingConfiguration));
        registrationBean.setFilter(new TrasierBufferFilter(configuration, filterConfigurations));
        registrationBean.setUrlPatterns(tracingConfiguration.getUrlPatterns());
        registrationBean.setAsyncSupported(true);
        return registrationBean;
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