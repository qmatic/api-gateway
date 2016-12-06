package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyVisitLastQueueEventFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(MyVisitLastQueueEventFilter.class);

	@Override
	public String filterType() {
		return FilterConstants.POST_FILTER;
	}

	// Must be run AFTER RequestCacheWriterFilter
	@Override
	public int filterOrder() {
		return 20;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return FilterConstants.MY_VISIT_LAST_QUEUE_EVENT.equals(ctx.get(FilterConstants.PROXY));
	}

	@Override
	public Object run() {
		log.debug("Running filter " +  getClass().getSimpleName());
		RequestContext ctx = RequestContext.getCurrentContext();
		String httpResponseBody = ctx.getResponseBody();
		if (httpResponseBody != null && !httpResponseBody.isEmpty()) {
			try {
				ctx.setResponseBody(getLastEvent(httpResponseBody));
			} catch (Exception e) {
				log.warn("HTTP Response parsing error : " + e.getMessage());
			}
		}
		return null;
	}

	protected String getLastEvent(String responseBody) throws Exception {
		JSONObject obj = new JSONObject("{\"events\":" + responseBody + "}");
		JSONArray result = obj.getJSONArray("events");
		JSONObject jsonObject = result.getJSONObject(result.length() - 1);
		return "{\"lastEvent\":" + jsonObject.toString() + "}";
	}

}
