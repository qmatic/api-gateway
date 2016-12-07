package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
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

	private final String MOBILE_USER = "mobile";

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
		return isChecksumEnforceEnabled() && isMobileUser() && isChecksumRoute();
	}

	private boolean isChecksumEnforceEnabled() {
		return enableChecksum;
	}

	private boolean isChecksumRoute() {
		RequestContext ctx = RequestContext.getCurrentContext();

		for (String route : orchestraProperties.getRoutes()) {
			if (route.equals(ctx.get("proxy"))) {
				return true;
			}
		}
		return false;
	}

	private boolean isMobileUser() {
		String token = getAuthToken();
		String userCredentials = getUserName(token);

		if(userCredentials == null) {
			return false;
		}

		return userCredentials.equals(MOBILE_USER);
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if(!correctChecksum(ctx)) {
			unauthorized();
		}
		return null;
	}

	private boolean correctChecksum(RequestContext ctx) {
		String visitId = getVisitIdFromRequest(ctx);
		String branchId = getBranchIdFromRequest(ctx);
		String requestChecksum = getVisitChecksum();

		return requestChecksum.equals(getCachedChecksum(branchId, visitId));
	}

	protected String getCachedChecksum(String branchId, String visitId) {
		String cachedChecksum = null;

		return cachedChecksum;
	}

	private String getVisitIdFromRequest(RequestContext ctx) {
		OrchestraProperties.VisitIdParameter visitIdParameter = orchestraProperties.getVisitIdParameter((String) ctx.get("proxy"));
		String visitId = getVisitIdFromPath(visitIdParameter.getParameter(), ctx);
		return visitId;
	}

	private String getVisitIdFromPath(String parameter, RequestContext ctx) {
		String path = String.valueOf(ctx.getRequest().getRequestURL());

		int parameterIndex = path.indexOf(parameter);
		if(parameterIndex <0 ) {
			return null;
		}
		String[] parameterSubstring = path.substring(parameterIndex).split("/");

		if(parameterSubstring == null || parameterSubstring.length < 2) {
			return null;
		}

		return parameterSubstring[1];
	}

	private String getBranchIdFromRequest(RequestContext ctx) {
		String path = String.valueOf(ctx.getRequest().getRequestURL());

		int parameterIndex = path.indexOf("branches");
		if(parameterIndex < 0 ) {
			return null;
		}
		String[] parameterSubstring = path.substring(parameterIndex).split("/");

		if(parameterSubstring == null || parameterSubstring.length < 2) {
			return null;
		}

		return parameterSubstring[1];
	}

	private String getParameterFromRequest(String parameter, RequestContext ctx) {
		return ctx.getRequestQueryParams().get(parameter).get(0);
	}

	protected String getAuthToken() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRequest().getHeader(GatewayConstants.AUTH_TOKEN);
	}

	protected String getVisitChecksum() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRequestQueryParams().get(GatewayConstants.VISIT_CHECKSUM).get(0);
	}

	private String getUserName(String apiToken) {
		OrchestraProperties.UserCredentials credentials = orchestraProperties.getCredentials(apiToken);
		if (credentials != null) {
			return credentials.getUser();
		}
		return null;
	}

	private void unauthorized() {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.removeRouteHost();
		ctx.setResponseStatusCode(401);
		ctx.setSendZuulResponse(false);
	}

	private void badrequest() {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.removeRouteHost();
		ctx.setResponseStatusCode(400);
		ctx.setSendZuulResponse(false);
	}

}
