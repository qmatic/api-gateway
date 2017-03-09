package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class CharacterEncodingPostFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(CharacterEncodingPostFilter.class);

    @Value("${orchestra.encoding}")
    private String encoding;

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
        return true;
    }

    @Override
    public Object run() {
        log.debug("Running filter " +  getClass().getSimpleName());

        RequestContext ctx = RequestContext.getCurrentContext();
        if(ctx.getResponse() != null) {
            if(encoding != null && isCharsetSupported(encoding)) {
                ctx.getResponse().setCharacterEncoding(encoding);
            } else {
                ctx.getResponse().setCharacterEncoding(FilterConstants.UTF_8_ENCODING);
            }
        }

        return null;
    }

    private Boolean isCharsetSupported(String name) {
        return Charset.availableCharsets().keySet().contains(name);
    }
}
