package com.qmatic.apigw.filters;

import com.qmatic.apigw.caching.SSOCookieCacheManager.Cookie;
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

import javax.ws.rs.core.HttpHeaders;
import java.util.List;

@Component
public class BasicAuthPostFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthPostFilter.class);

    @Value("${logZuulExceptions}")
    private boolean logZuulExceptions;

    @Autowired
    private SSOCookieCacheManager ssoCookieCacheManager;

    @Override
    public String filterType() {
        return FilterConstants.POST_FILTER;
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
        String authToken = ctx.getRequest().getHeader(GatewayConstants.AUTH_TOKEN);

        if (authToken != null) {
            List<Pair<String, String>> originResponseHeaders = ctx.getOriginResponseHeaders();
            for (Pair<String, String> responseHeader : originResponseHeaders) {
                if (responseHeader.first().equals(HttpHeaders.SET_COOKIE) &&
                        (responseHeader.second().contains(GatewayConstants.SSOCOOKIE) || responseHeader.second().contains(GatewayConstants.JSESSIONID))) {
                    String[] split = responseHeader.second().split(";");
                    String cookieName = split[0].split("=")[0];
                    String cookieValue = split[0].split("=")[1];
                    Cookie cookie = new Cookie(cookieName, cookieValue);
                    ssoCookieCacheManager.writeSSOCookieToCache(authToken, cookie);
                }
            }
        }
        return null;
    }
}
