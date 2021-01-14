package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierFilterConfigurations;
import com.trasier.client.interceptor.TrasierSpanResolverInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TrasierSpanFilterInterceptor implements TrasierSpanResolverInterceptor {

    @Autowired(required = false)
    private TrasierFilterConfigurations filterConfigurations;

    public TrasierSpanFilterInterceptor() {
        this.filterConfigurations = new TrasierFilterConfigurations();
    }

    public TrasierSpanFilterInterceptor(TrasierFilterConfigurations configurations) {
        this.filterConfigurations = configurations;
    }

    @Override
    public void interceptUrlResolved(Span span, String url) {
        if (!span.isCancel()) {
            interceptByUrl(span, url);
        }
    }

    @Override
    public void interceptMetadataResolved(Span span) {
        if (!span.isCancel()) {
            interceptByOperationName(span);
        }
    }

    private void interceptByUrl(Span span, String url) {
        if (TrasierConstants.DEFAULT_SKIP_PATTERN.matcher(url).matches()) {
            span.setCancel(true);
            return;
        }
        if (filterConfigurations != null) {
            if (filterConfigurations.getDisablePayload() != null) {
                Pattern disablePattern = filterConfigurations.getDisablePayload().getUrl();
                if (!span.isCancel() && disablePattern != null && disablePattern.matcher(url).matches()) {
                    span.setPayloadDisabled(true);
                }
            }
            if (filterConfigurations.getAllow() != null) {
                Pattern allowPattern = filterConfigurations.getAllow().getUrl();
                if (!span.isCancel() && allowPattern != null && !allowPattern.matcher(url).matches()) {
                    span.setCancel(true);
                }
            }
            if (filterConfigurations.getCancel() != null) {
                Pattern cancelPattern = filterConfigurations.getCancel().getUrl();
                if (!span.isCancel() && cancelPattern != null && cancelPattern.matcher(url).matches()) {
                    span.setCancel(true);
                }
            }
        }
    }

    private void interceptByOperationName(Span span) {
        String operationName = span.getName();
        if ("OPTIONS".equalsIgnoreCase(operationName)) {
            span.setCancel(true);
            return;
        }

        if (filterConfigurations != null) {
            if (filterConfigurations.getDisablePayload() != null) {
                Pattern disablePattern = filterConfigurations.getDisablePayload().getOperation();
                if (!span.isCancel() && disablePattern != null && disablePattern.matcher(operationName).matches()) {
                    span.setPayloadDisabled(true);
                }
            }
            if (filterConfigurations.getAllow() != null) {
                Pattern allowPattern = filterConfigurations.getAllow().getOperation();
                if (!span.isCancel() && allowPattern != null && !allowPattern.matcher(operationName).matches()) {
                    span.setCancel(true);
                }
            }
            if (filterConfigurations.getCancel() != null) {
                Pattern cancelPattern = filterConfigurations.getCancel().getOperation();
                if (!span.isCancel() && cancelPattern != null && cancelPattern.matcher(operationName).matches()) {
                    span.setCancel(true);
                }
            }
        }
    }

}
