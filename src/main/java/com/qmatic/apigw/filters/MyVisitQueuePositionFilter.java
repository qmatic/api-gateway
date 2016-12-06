package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MyVisitQueuePositionFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(MyVisitQueuePositionFilter.class);

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
		return "my_visit_queue_position".equals(ctx.get("proxy"));
	}

	@Override
	public Object run() {
		log.debug("Running filter " +  getClass().getSimpleName());
		RequestContext ctx = RequestContext.getCurrentContext();

        String requestedVisitId = getRequestedVisitId(ctx);
        if(requestedVisitId != null) {
            getVisitPosition(ctx, requestedVisitId);
        }
		return null;
	}

    private void getVisitPosition(RequestContext ctx, String requestedVisitId) {
        String unfilteredResponseBody = ctx.getResponseBody();
        if (unfilteredResponseBody != null && !unfilteredResponseBody.isEmpty()) {
            try {
                String filteredResponseBody = getVisitJsonWithPosition(requestedVisitId, unfilteredResponseBody);
                setFilteredResponseBody(ctx, filteredResponseBody);
            } catch (Exception e) {
                handleException(ctx, "HTTP Response parsing error : " + e.getMessage());
            }
        }
    }

    private void setFilteredResponseBody(RequestContext ctx, String visitJsonWithPosition) {
        ctx.setResponseBody(visitJsonWithPosition);
        ctx.set("cacheResponse", true);
    }

    private String getRequestedVisitId(RequestContext ctx) {
        String requestedVisitId = null;
        try {
            requestedVisitId = getRequestedVisitIdFromRequestContext(ctx);
        } catch (Exception e) {
            handleException(ctx, "HTTP Request parsing error : " + e.getMessage());
        }
        return requestedVisitId;
    }

    private void handleException(RequestContext ctx, String msg) {
        log.warn(msg);
        ctx.setResponseBody("{}");
    }

    private String getRequestedVisitIdFromRequestContext(RequestContext ctx) throws Exception {
        Map<String, List<String>> requestQueryParams = ctx.getRequestQueryParams();
        return getVisitIdFromRequestQueryParams(requestQueryParams);
    }

    private String getVisitIdFromRequestQueryParams(Map<String, List<String>> requestQueryParams) throws Exception {
        if(requestQueryParams != null && requestQueryParams.containsKey(FilterConstants.VISIT_ID)) {
            return requestQueryParams.get(FilterConstants.VISIT_ID).get(0);
        } else {
            throw new Exception("visitId not found among request parameters");
        }
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

	protected int getQueuingPosition(String visitId, JSONArray visits) throws Exception {
		int aVisitId;
		for (int i = 0; i < visits.length(); i++) {
			aVisitId = visits.getJSONObject(i).getInt("visitId");
			if (aVisitId == Integer.parseInt(visitId)) {
				return i+1;
			}
		}
		throw new Exception("visitId=" + visitId + " not found");
	}
}
