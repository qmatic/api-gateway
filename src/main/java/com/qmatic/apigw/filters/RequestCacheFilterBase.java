package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.caching.RequestCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

abstract public class RequestCacheFilterBase extends ZuulFilter {

    public static final String ROUTE_HOST = "routeHost";
    public static final Object RESULT_DOES_NOT_MATTER = null;

    private static final Logger log = LoggerFactory.getLogger(RequestCacheFilterBase.class);

    @Autowired
    protected RequestCacheManager cacheManager;

    public boolean isHandledByCache() {
        if(getProxy() == null || getRequestUri() == null || getRouteHost() == null) {
            return false;
        }
        Boolean isHandledByCache = cacheManager.isHandledByCache(getProxy(), getRequestUri());
        log.debug("isHandledByCache : \"{}\", proxy : \"{}\", request : \"{}\"", isHandledByCache, getProxy(), getRequestUri());
        return isHandledByCache;
    }

    public String getRequestUri() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if(!ctx.containsKey(FilterConstants.REQUEST_URI)) {
            return null;
        }
        String requestURI = ctx.get(FilterConstants.REQUEST_URI).toString();
        return requestURI;
    }

    public String getRouteHost() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if(!ctx.containsKey(ROUTE_HOST)) {
            return null;
        }
        String routeHost = ctx.get(ROUTE_HOST).toString();
        return routeHost;
    }

    public boolean isGet() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return "GET".equals(ctx.getRequest().getMethod());
    }

    public String getProxy() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if(!ctx.containsKey(FilterConstants.PROXY)) {
            return null;
        }
        String proxy = ctx.get(FilterConstants.PROXY).toString();
        return proxy;
    }

    @Override
    public boolean shouldFilter() {
        if(!isGet()) {
            return false;
        }
        return isHandledByCache();
    }

    protected boolean isResponseBodyEmpty() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getResponseBody() == null;
    }

    /**
     * After authentication
     */
    @Override
    public int filterOrder() {
        return 11;
    }

    protected String getQueryParameters() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getRequest().getQueryString();
    }
}
