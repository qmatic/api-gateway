package com.qmatic.apigw.filters;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
        String result = getResponse(ctx);
        cacheManager.writeRequestToCache(getProxy(), getRequestUri(), getRouteHost(), getQueryParameters() , result);
        ctx.setResponseBody(result);
        removeContentEncodingHeader(ctx);
        return RESULT_DOES_NOT_MATTER;
    }

    private void removeContentEncodingHeader(RequestContext ctx) {
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
        zuulResponseHeaders.removeIf(header -> header != null && header.first() != null && header.first().equals("Content-Encoding"));
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

    protected String getResponse(RequestContext ctx) {
        try {
            InputStream inputStream = ctx.getResponseDataStream();
            String result;
            if (ctx.getResponseGZipped()) {
                 result = StreamUtils.copyToString(new GZIPInputStream(inputStream),
                         Charset.forName(FilterConstants.UTF_8_ENCODING));
            }
            else {
                result = IOUtils.toString(inputStream, Charset.forName(FilterConstants.UTF_8_ENCODING));
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
