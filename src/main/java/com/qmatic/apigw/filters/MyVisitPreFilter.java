package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyVisitPreFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(MyVisitPreFilter.class);

	@Override
	public String filterType() {
		return "pre";
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
		if (!checkVisitId(ctx)) {
			ctx.removeRouteHost();
			ctx.setResponseStatusCode(400);
			ctx.setSendZuulResponse(false);
		}
		return null;
	}

	private boolean checkVisitId(RequestContext ctx) {
		try {
			return ctx.getRequestQueryParams().get("visitId").get(0) != "";
		} catch (Exception e) {
			return false;
		}
	}

}
