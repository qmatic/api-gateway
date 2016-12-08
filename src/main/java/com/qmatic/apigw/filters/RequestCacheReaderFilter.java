package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.filters.util.Responder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestCacheReaderFilter extends RequestCacheFilterBase {

    private static final Logger log = LoggerFactory.getLogger(RequestCacheReaderFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_FILTER;
    }

    @Override
    public Object run() {
        log.debug("Running filter " +  getClass().getSimpleName());
        String cachedResponse = cacheManager.getRequestFromCache(getProxy(), getRequestUri(), getRouteHost(), getQueryParameters());
        if(cachedResponse == null || cachedResponse.isEmpty()) {
            return RESULT_DOES_NOT_MATTER;
        }
        if (!isResponseBodyEmpty()) {
            log.warn("Response body already set for URI : {}", getRequestUri());
            return RESULT_DOES_NOT_MATTER;
        }
        Responder.writeResponse(RequestContext.getCurrentContext(), cachedResponse);
        return RESULT_DOES_NOT_MATTER;
    }
}
