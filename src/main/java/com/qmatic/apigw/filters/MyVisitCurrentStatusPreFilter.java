package com.qmatic.apigw.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.filters.util.JsonUtil;
import com.qmatic.apigw.filters.util.RequestContextUtil;
import com.qmatic.apigw.rest.TinyVisit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;

@Component
public class MyVisitCurrentStatusPreFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(MyVisitCurrentStatusPreFilter.class);

    @Autowired
	VisitCacheManager visitCacheManager;

    @Override
	public String filterType() {
		return FilterConstants.PRE_FILTER;
	}

	@Override
	public int filterOrder() {
		return 10;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
        return FilterConstants.MY_VISIT_CURRENT_STATUS.equals(ctx.get(FilterConstants.PROXY))
                && HttpServletResponse.SC_UNAUTHORIZED != ctx.getResponseStatusCode()
                && HttpServletResponse.SC_NOT_FOUND != ctx.getResponseStatusCode();
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
        if (!isValidRequest(ctx)) {
            RequestContextUtil.setResponseBadRequest(ctx);
        } else {
            Long branchId = Long.valueOf(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx));
            Long visitId = Long.valueOf(RequestContextUtil.getPathParameter(FilterConstants.VISITS, ctx)); // NFE
            TinyVisit visit = visitCacheManager.getVisit(branchId, visitId);
            if (visit != null) {
                try {
                    RequestContextUtil.writeResponse(ctx, JsonUtil.convert(visit));
                } catch (JsonProcessingException e) {
                    log.warn("Could not serialize visit with id {} on branch {}", visitId, branchId, e);
                    RequestContextUtil.setResponseInternalServerError(ctx);
                }
            } else {
                log.warn("Could not fetch visit with id {} on branch {}", visitId, branchId);
                RequestContextUtil.setResponseNotFound(ctx);
            }
        }
		return null;
	}

    private boolean isValidRequest(RequestContext ctx) {
        return StringUtils.isNumeric(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx))
                && StringUtils.isNumeric(RequestContextUtil.getPathParameter(FilterConstants.VISITS, ctx));
    }
}
