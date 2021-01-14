package io.opentracing.contrib.spring.cloud.feign;

import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Tracer;
import org.springframework.beans.factory.BeanFactory;

import java.util.List;

public class TrasierFeignContextBeanPostProcessor extends FeignContextBeanPostProcessor {
    public TrasierFeignContextBeanPostProcessor(Tracer tracer, BeanFactory beanFactory, List<FeignSpanDecorator> spanDecorators) {
        super(tracer, beanFactory, spanDecorators);
    }
}
