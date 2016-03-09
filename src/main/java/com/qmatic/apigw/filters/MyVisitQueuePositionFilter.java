package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyVisitQueuePositionFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(MyVisitQueuePositionFilter.class);

	@Override
	public String filterType() {
		return "post";
	}

	// Must be run AFTER RequestCacheWriterFilter
	@Override
	public int filterOrder() {
		return 20;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return "my_visit_queue_position".equals(ctx.get("proxy"));
	}

	@Override
	public Object run() {
		log.debug("Running filter " +  getClass().getSimpleName());
		RequestContext ctx = RequestContext.getCurrentContext();
		String visitId = ctx.getRequestQueryParams().get("visitId").get(0);
		String httpResponseBody = ctx.getResponseBody();
		if (httpResponseBody != null && !httpResponseBody.isEmpty()) {
			try {
				ctx.setResponseBody(getPosition(visitId, httpResponseBody));
				ctx.set("cacheResponse", true);
			} catch (Exception e) {
				log.warn("HTTP Response parsing error : " + e.getMessage());
				ctx.setResponseBody("{}");
			}
		}
		return null;
	}

	protected String getPosition(String visitId, String responseBody) throws Exception {
		boolean visitIdFound = false;
		int pos = 0;
		JSONObject obj = new JSONObject("{\"result\":" + responseBody + "}");
		JSONArray list= obj.getJSONArray("result");
		for (int i = 0; i < list.length(); i++) {
			int vId = list.getJSONObject(i).getInt("visitId");
			if (vId <= Integer.parseInt(visitId)) {
				pos += 1;
			}
			if (vId == Integer.parseInt(visitId)) {
				visitIdFound = true;
			}
		}
		if (!visitIdFound) {
			throw new Exception("visitId=" + visitId + " not found");
		}
		JSONObject lastObj = list.getJSONObject(pos-1);
		lastObj.put("position", pos);
		return "{\"visit\":" + lastObj.toString() + "}";
	}
}
