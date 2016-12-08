package com.qmatic.apigw.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.filters.util.JsonUtil;
import com.qmatic.apigw.filters.util.Responder;
import com.qmatic.apigw.rest.TinyVisit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        return FilterConstants.MY_VISIT_CURRENT_STATUS.equals(ctx.get(FilterConstants.PROXY));
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		Long branchId = getUrlParameter(ctx, FilterConstants.BRANCHES);
		Long visitId = getUrlParameter(ctx, FilterConstants.VISITS);
		TinyVisit visit = visitCacheManager.getVisit(visitId, branchId);
		if (visit != null) {
			try {
                Responder.writeResponse(ctx, JsonUtil.convert(visit));
			} catch (JsonProcessingException e) {
				log.warn("", e);
                Responder.abortRequest(ctx);
            }
		} else {
			log.warn("Couldn't fetch visit with id {} on branch {}", visitId, branchId);
            Responder.abortRequest(ctx);
		}
		return null;
	}

	protected Long getUrlParameter(RequestContext ctx, String paramName) {
		String urlParameter = ctx.getRequest().getRequestURI().split(paramName)[1].split("/")[1].split("\\?")[0];
		return Long.valueOf(urlParameter);
	}
}
