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
				ctx.setResponseBody(getVisitJsonWithPosition(visitId, httpResponseBody));
				ctx.set("cacheResponse", true);
			} catch (Exception e) {
				log.warn("HTTP Response parsing error : " + e.getMessage());
				ctx.setResponseBody("{}");
			}
		}
		return null;
	}

	protected String getVisitJsonWithPosition(String visitId, String orderedResponseBody) throws Exception {
		JSONObject obj = new JSONObject("{\"result\":" + orderedResponseBody + "}");
		JSONArray orderedVisits= obj.getJSONArray("result");
		int queuePosition = getQueuingPosition(visitId, orderedVisits);
		JSONObject visit = orderedVisits.getJSONObject(queuePosition-1);
		visit.put("position", queuePosition);
		visit.put("queueSize", orderedVisits.length());
		return "{\"visit\":" + visit.toString() + "}";
	}

	protected int getQueuingPosition(String visitId, JSONArray list) throws Exception {
		int aVisitId;
		for (int i = 0; i < list.length(); i++) {
			aVisitId = list.getJSONObject(i).getInt("visitId");
			if (aVisitId == Integer.parseInt(visitId)) {
				return i+1;
			}
		}
		throw new Exception("visitId=" + visitId + " not found");
	}
}
