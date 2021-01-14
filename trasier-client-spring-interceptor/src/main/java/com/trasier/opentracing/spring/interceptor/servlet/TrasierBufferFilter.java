package com.trasier.opentracing.spring.interceptor.servlet;

import com.trasier.client.api.TrasierConstants;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.configuration.TrasierFilterConfigurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class TrasierBufferFilter extends GenericFilterBean {

    private static final Integer MAX_REQUEST_SIZE = 1024 * 1024;

    @Autowired
    private TrasierClientConfiguration configuration;

    @Autowired(required = false)
    private TrasierFilterConfigurations filterConfigurations;

    public TrasierBufferFilter() {
    }

    public TrasierBufferFilter(TrasierClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public TrasierBufferFilter(TrasierClientConfiguration configuration, TrasierFilterConfigurations filterConfigurations) {
        this.configuration = configuration;
        this.filterConfigurations = filterConfigurations;
    }

    @Override
    protected void initFilterBean() {
        if (configuration == null || filterConfigurations == null) {
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            if (configuration == null) {
                configuration = webApplicationContext.getBean(TrasierClientConfiguration.class);
            }
            if (filterConfigurations == null) {
                filterConfigurations = webApplicationContext.getBean(TrasierFilterConfigurations.class);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!configuration.isActivated() || configuration.isPayloadTracingDisabled()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        boolean useContentCaching = useContentCaching(servletRequest);
        if (useContentCaching) {
            ContentCachingRequestWrapper request = createCachedRequest((HttpServletRequest) servletRequest);
            ContentCachingResponseWrapper response = createCachedResponse((HttpServletResponse) servletResponse);

            filterChain.doFilter(request, response);

            response.copyBodyToResponse();
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    protected ContentCachingResponseWrapper createCachedResponse(HttpServletResponse servletResponse) {
        return servletResponse instanceof ContentCachingResponseWrapper ? (ContentCachingResponseWrapper) servletResponse : new ContentCachingResponseWrapper(servletResponse);
    }

    protected ContentCachingRequestWrapper createCachedRequest(HttpServletRequest servletRequest) {
        return servletRequest instanceof ContentCachingRequestWrapper ? (ContentCachingRequestWrapper) servletRequest : new ContentCachingRequestWrapper(servletRequest, MAX_REQUEST_SIZE);
    }

    private boolean useContentCaching(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            StringBuffer requestURL = httpServletRequest.getRequestURL();
            if (requestURL != null) {
                return useContentCaching(requestURL.toString());
            }
        }
        return false;
    }

    public boolean useContentCaching(String url) {
        if (TrasierConstants.DEFAULT_SKIP_PATTERN.matcher(url).matches()) {
            return false;
        }
        if (filterConfigurations != null) {
            if (filterConfigurations.getDisablePayload() != null) {
                Pattern disablePattern = filterConfigurations.getDisablePayload().getUrl();
                if (disablePattern != null && disablePattern.matcher(url).matches()) {
                    return false;
                }
            }
            if (filterConfigurations.getAllow() != null) {
                Pattern allowPattern = filterConfigurations.getAllow().getUrl();
                if (allowPattern != null && !allowPattern.matcher(url).matches()) {
                    return false;
                }
            }
            if (filterConfigurations.getCancel() != null) {
                Pattern cancelPattern = filterConfigurations.getCancel().getUrl();
                if (cancelPattern != null && cancelPattern.matcher(url).matches()) {
                    return false;
                }
            }
        }
        return true;
    }

}