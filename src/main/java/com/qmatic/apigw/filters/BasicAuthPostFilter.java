package com.qmatic.apigw.filters;

import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.SSOCookieCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This filter handles connection refused exceptions
 */
@Component
public class BasicAuthPostFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthPostFilter.class);

    @Value("${logZuulExceptions}")
    private boolean logZuulExceptions;

    @Autowired
    private SSOCookieCacheManager ssoCookieCacheManager;

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
       return true;
    }

    @Override
    public Object run() {
        log.debug("Running filter " +  getClass().getSimpleName());

        RequestContext ctx = RequestContext.getCurrentContext();
        String authToken = ctx.getRequest().getHeader("auth-token");

        String responseStatusCode = Integer.toString((Integer) ctx.get(GatewayConstants.RESPONSE_STATUS_CODE));
        if ("401".equals(responseStatusCode)) {
            ssoCookieCacheManager.deleteSSOCookieFromCache(authToken);
        } else if (authToken != null) {
            List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
            ListIterator<Pair<String, String>> iterator = zuulResponseHeaders.listIterator();
            while(iterator.hasNext()){
                Pair<String, String> responseHeader = iterator.next();
                if (responseHeader.first().equals("Set-Cookie") && responseHeader.second().contains("SSOcookie")) {
                    String[] split = responseHeader.second().split(";");
                    String cookie = split[0].split("=")[1];
                    ssoCookieCacheManager.writeSSOCookieToCache(authToken, cookie);
                    iterator.remove();
                }
            }
        }
        return null;
    }
}
