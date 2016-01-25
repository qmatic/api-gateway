package com.qmatic.apigw.filters;

import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This filter handles connection refused exceptions
 */
@Component
public class OrchestraResponseErrorFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(OrchestraResponseErrorFilter.class);

    @Value("${logZuulExceptions}")
    private boolean logZuulExceptions;

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String responseStatusCode = Integer.toString((Integer) ctx.get(GatewayConstants.RESPONSE_STATUS_CODE));
        return responseStatusCode.startsWith("5") || responseStatusCode.startsWith("4");
    }

    @Override
    public Object run() {
        log.debug("Running filter " +  getClass().getSimpleName());
        filterErrorMessage();
        return null;
    }

    protected void filterErrorMessage() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.removeRouteHost();
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
        for (Pair<String, String>header : zuulResponseHeaders) {
            if (header.first().equals(GatewayConstants.ERROR_MESSAGE)) {
                if (logZuulExceptions) {
                    log.warn(getClass().getSimpleName(), "Error message:" + header.second());
                }
                header.setSecond("See gateway log files for full error message.");
            }
        }
    }
}
