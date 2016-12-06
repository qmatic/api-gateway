package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MyVisitPreFilter extends ZuulFilter {

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
		return "my_visit_queue_position".equals(ctx.get("proxy"));
		// || "my_visit_last_queue_event".equals(ctx.get("proxy")) ;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (!checkVisitIdIsValid(ctx)) {
			ctx.removeRouteHost();
			ctx.setResponseStatusCode(400);
			ctx.setSendZuulResponse(false);
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

}
