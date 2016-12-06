package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class RequestCacheWriterFilter extends RequestCacheFilterBase {

    private static final Logger log = LoggerFactory.getLogger(RequestCacheWriterFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.POST_FILTER;
    }

    @Override
    public boolean shouldFilter() {
        return super.shouldFilter() && isResponseBodyEmpty();
    }

    @Override
    public Object run() {
        log.debug("Running filter " + getClass().getSimpleName());
        RequestContext ctx = RequestContext.getCurrentContext();
        if(!isValidResponse()) {
            return RESULT_DOES_NOT_MATTER;
        }
        if (!cacheManager.isHandledByCache(getProxy(), getRequestUri())) {
            return RESULT_DOES_NOT_MATTER;
        }
        InputStream responseDataStream = ctx.getResponseDataStream();
        String result = getResponse(responseDataStream);
        cacheManager.writeRequestToCache(getProxy(), getRequestUri(), getRouteHost(), getQueryParameters() , result);
        ctx.setResponseBody(result);
        return RESULT_DOES_NOT_MATTER;
    }

    protected boolean isValidResponse() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Integer status = ctx.getResponse().getStatus();
        if(status == 200) {
            return true;
        }
        log.debug("Invalid response {} for request : {}", status, getRequestUri());
        return false;
    }

    protected String getResponse(InputStream inputStream) {
        try {
            String result = IOUtils.toString(inputStream);
            return result;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
