package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.filters.util.RequestContextUtil;
import com.qmatic.apigw.properties.OrchestraProperties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
public class ChecksumValidationFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(ChecksumValidationFilter.class);

	@Value("${orchestra.enableChecksum}")
	private boolean enableChecksum = true;

	@Autowired
	VisitCacheManager visitCacheManager;
	@Autowired
	OrchestraProperties orchestraProperties;

	@Override
	public String filterType() {
		return FilterConstants.PRE_FILTER;
	}

	@Override
	public int filterOrder() {
		return 6;
	}

	@Override
	public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
		return isChecksumEnforceEnabled() && isChecksumRoute(ctx)
				&& HttpServletResponse.SC_UNAUTHORIZED != ctx.getResponseStatusCode();
	}

	private boolean isChecksumEnforceEnabled() {
		return enableChecksum;
	}

	private boolean isChecksumRoute(RequestContext ctx) {
        for (String route : orchestraProperties.getRoutes()) {
			if (route.equals(ctx.get(FilterConstants.PROXY))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (!isValidRequest(ctx)) {
			RequestContextUtil.setResponseBadRequest(ctx, "One or more parameters are missing or invalid. Check if the path is valid.");
		} else {
			String cachedChecksum = getCachedChecksum(ctx);
			if (cachedChecksum == null || !isCorrectChecksum(ctx, cachedChecksum)) {
				visitCacheManager.createVisitNotFoundResponse(ctx);
			}
		}
		return null;
	}

	private boolean isValidRequest(RequestContext ctx) {
		OrchestraProperties.ChecksumRoute checksumRoute = orchestraProperties.getChecksumRoute((String) ctx.get(FilterConstants.PROXY));
		return StringUtils.isNumeric(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx))
				&& StringUtils.isNumeric(RequestContextUtil.getPathParameter(checksumRoute.getParameter(), ctx));
	}

	private String getCachedChecksum(RequestContext ctx) {
		Long branchId = getBranchIdFromRequest(ctx);
        Long visitId = getVisitIdFromRequest(ctx);
		if (branchId != null && visitId != null) {
			return getCachedChecksum(branchId, visitId);
		}
		return null;
	}

	private boolean isCorrectChecksum(RequestContext ctx, String cachedChecksum) {
		String requestChecksum = getVisitChecksum(ctx);
		if (requestChecksum != null) {
			return requestChecksum.equals(cachedChecksum);
		}
		return false;
	}

	private String getCachedChecksum(Long branchId, Long visitId) {
		return visitCacheManager.getChecksum(branchId, visitId);
	}

	private Long getVisitIdFromRequest(RequestContext ctx) {
		OrchestraProperties.ChecksumRoute checksumRoute = orchestraProperties.getChecksumRoute((String) ctx.get(FilterConstants.PROXY));
		return Long.valueOf(RequestContextUtil.getPathParameter(checksumRoute.getParameter(), ctx));
	}

    private Long getBranchIdFromRequest(RequestContext ctx) {
		return Long.valueOf(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx));
    }

	protected String getVisitChecksum(RequestContext ctx) {
		return RequestContextUtil.getQueryParameter(FilterConstants.VISIT_CHECKSUM, ctx);
	}

	private String getUserName(String apiToken) {
		OrchestraProperties.UserCredentials credentials = orchestraProperties.getCredentials(apiToken);
		if (credentials != null) {
			return credentials.getUser();
		}
		return null;
	}
}
