package com.qmatic.apigw.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.filters.util.JsonUtil;
import com.qmatic.apigw.filters.util.Responder;
import com.qmatic.apigw.rest.TinyVisit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MyVisitQueuePositionPreFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(MyVisitQueuePositionPreFilter.class);

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
		return FilterConstants.MY_VISIT_QUEUE_POSITION.equals(ctx.get(FilterConstants.PROXY));
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (!checkVisitIdIsValid(ctx)) {
			Responder.abortRequest(ctx);
		} else {
			Long branchId = getUrlParameter(ctx, FilterConstants.BRANCHES);
			Long visitId = Long.valueOf(ctx.getRequestQueryParams().get(FilterConstants.VISIT_ID).get(0));
			TinyVisit visit = visitCacheManager.getVisit(visitId, branchId);
			if (visit != null) {
				try {
					Responder.writeResponse(ctx, "{\"visit\":" + JsonUtil.convert(visit) + "}");
				} catch (JsonProcessingException e) {
					log.warn("", e);
				}
			} else {
				log.warn("Couldn't fetch visit with id {} on branch {}", visitId, branchId);
				Responder.abortRequest(ctx);
			}
		}
		return null;
	}

	private boolean checkVisitIdIsValid(RequestContext ctx) {
		List<String> visitId = ctx.getRequestQueryParams().get(FilterConstants.VISIT_ID);
		if (visitId != null && visitId.size() == 1) {
			return StringUtils.isNumeric(visitId.get(0));
		}
		return false;
	}

	protected Long getUrlParameter(RequestContext ctx, String paramName) {
		String urlParameter = ctx.getRequest().getRequestURI().split(paramName)[1].split("/")[1].split("\\?")[0];
		return Long.valueOf(urlParameter);
	}
}
