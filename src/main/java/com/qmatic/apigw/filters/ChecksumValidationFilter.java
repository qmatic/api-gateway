package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.filters.util.RequestContextUtil;
import com.qmatic.apigw.properties.OrchestraProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChecksumValidationFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(ChecksumValidationFilter.class);

	@Value("${orchestra.enableChecksum}")
	private boolean enableChecksum = true;

	@Autowired
	OrchestraProperties orchestraProperties;

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 6;
	}

	@Override
	public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
		return isChecksumEnforceEnabled() && isMobileUser(ctx) && isChecksumRoute(ctx);
	}

	private boolean isChecksumEnforceEnabled() {
		return enableChecksum;
	}

	private boolean isChecksumRoute(RequestContext ctx) {
        for (String route : orchestraProperties.getRoutes()) {
			if (route.equals(ctx.get("proxy"))) {
				return true;
			}
		}
		return false;
	}

	private boolean isMobileUser(RequestContext ctx) {
		String token = RequestContextUtil.getAuthToken(ctx);
		String userCredentials = getUserName(token);

		if(userCredentials == null) {
			return false;
		}

		return userCredentials.equals(GatewayConstants.MOBILE_USER);
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if(!isCorrectChecksum(ctx)) {
            RequestContextUtil.setResponseUnauthorized(ctx);
		}
		return null;
	}

	private boolean isCorrectChecksum(RequestContext ctx) {
		String visitId = getVisitIdFromRequest(ctx);
		String branchId = getBranchIdFromRequest(ctx);
		String requestChecksum = getVisitChecksum(ctx);

		return requestChecksum.equals(getCachedChecksum(branchId, visitId));
	}

	private String getCachedChecksum(String branchId, String visitId) {
		String cachedChecksum = null;

		return cachedChecksum;
	}

	private String getVisitIdFromRequest(RequestContext ctx) {
		OrchestraProperties.VisitIdParameter visitIdParameter = orchestraProperties.getVisitIdParameter((String) ctx.get("proxy"));
		String visitId = RequestContextUtil.getPathParameter(visitIdParameter.getParameter(), ctx);
		return visitId;
	}

    private String getBranchIdFromRequest(RequestContext ctx) {
        String branchId = RequestContextUtil.getPathParameter("branches", ctx);
        return branchId;
    }

	protected String getVisitChecksum(RequestContext ctx) {
		return RequestContextUtil.getQueryParameter(GatewayConstants.VISIT_CHECKSUM, ctx);
	}

	private String getUserName(String apiToken) {
		OrchestraProperties.UserCredentials credentials = orchestraProperties.getCredentials(apiToken);
		if (credentials != null) {
			return credentials.getUser();
		}
		return null;
	}
}
