package com.qmatic.apigw.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.filters.util.JsonUtil;
import com.qmatic.apigw.filters.util.RequestContextUtil;
import com.qmatic.apigw.rest.VisitStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Deprecated
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
		return FilterConstants.MY_VISIT_QUEUE_POSITION.equals(ctx.get(FilterConstants.PROXY))
				&& HttpServletResponse.SC_UNAUTHORIZED != ctx.getResponseStatusCode()
				&& HttpServletResponse.SC_NOT_FOUND != ctx.getResponseStatusCode()
				&& HttpServletResponse.SC_BAD_REQUEST != ctx.getResponseStatusCode();
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (!isValidRequest(ctx)) {
			RequestContextUtil.setResponseBadRequest(ctx, "One or more parameters are missing or invalid. Check if the path is valid.");
		} else {
			Long branchId = Long.valueOf(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx));
			Long visitId = Long.valueOf(ctx.getRequestQueryParams().get(FilterConstants.VISIT_ID).get(0));
			VisitStatus visit = visitCacheManager.getVisit(branchId, visitId);
			if (visit != null) {
				if (visit.getQueueId() != null) {
					try {
						RequestContextUtil.writeResponse(ctx, "{\"visit\":" + JsonUtil.convert(visit) + "}");
					} catch (JsonProcessingException e) {
						log.warn("Could not serialize visit with id {} on branch {}", visitId, branchId, e);
						RequestContextUtil.setResponseInternalServerError(ctx);
					}
				} else {
					// Myfunwait expects status 200 and an empty response body if the visit is not in a queue
					RequestContextUtil.setEmptyResponse(ctx);
				}
			} else {
				log.warn("Could not fetch visit with id {} on branch {}", visitId, branchId);
				RequestContextUtil.setResponseNotFound(ctx);
			}
		}
		return null;
	}

	private boolean isValidRequest(RequestContext ctx) {
		return checkVisitIdIsValid(ctx) && StringUtils.isNumeric(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx));
	}

	private boolean checkVisitIdIsValid(RequestContext ctx) {
		List<String> visitId = ctx.getRequestQueryParams().get(FilterConstants.VISIT_ID);
		if (visitId != null && visitId.size() == 1) {
			return StringUtils.isNumeric(visitId.get(0));
		}
		return false;
	}
}
