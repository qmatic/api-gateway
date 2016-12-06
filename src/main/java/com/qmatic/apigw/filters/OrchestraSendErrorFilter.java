package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This filter handles connection refused exceptions
 */
@Component
public class OrchestraSendErrorFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(OrchestraSendErrorFilter.class);
    private static final String ERROR_EXCEPTION = "error.exception";
    private static final String ERROR_STATUS_CODE = "error.status_code";

    protected final String ORCHESTRA_SEND_ERROR_FILTER_RAN = "orchestraSendErrorFilter.ran";

    @Value("${logZuulExceptions}")
    private boolean logZuulExceptions;

    @Override
    public String filterType() {
        return FilterConstants.POST_FILTER;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // only forward to errorPath if it hasn't been forwarded to already
        return ctx.containsKey(ERROR_STATUS_CODE) &&
                !ctx.getBoolean(ORCHESTRA_SEND_ERROR_FILTER_RAN, false);
    }

    @Override
    public Object run() {
        log.debug("Running filter " +  getClass().getSimpleName());

        RequestContext ctx = RequestContext.getCurrentContext();
        int statusCode = (Integer) ctx.get(ERROR_STATUS_CODE);
        String responseBody = "";
        if(ctx.containsKey(ERROR_EXCEPTION)) {
            responseBody = ctx.get(ERROR_EXCEPTION).toString();
        }
        outputError(statusCode, responseBody);
        if(logZuulExceptions) {
            logException();
        }
        return null;
    }

    protected void outputError(Integer statusCode, String responseBody) {
        RequestContext ctx = RequestContext.getCurrentContext();
        log.debug("Routing failed, status : " + statusCode.toString() + " message : \"" + responseBody + "\"");
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
        ctx.set(ORCHESTRA_SEND_ERROR_FILTER_RAN, true);
    }

    protected void logException() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Object e = ctx.get(ERROR_EXCEPTION);
        log.warn("Error during filtering", Throwable.class.cast(e));
    }
}
