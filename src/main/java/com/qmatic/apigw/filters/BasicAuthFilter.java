package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.SSOCookieCacheManager;
import com.qmatic.apigw.filters.util.RequestContextUtil;
import com.qmatic.apigw.properties.OrchestraProperties;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.charset.Charset;

@EnableConfigurationProperties(OrchestraProperties.class)
@Component
public class BasicAuthFilter extends ZuulFilter {

	@Autowired
	OrchestraProperties orchestraProperties;

	@Autowired
	SSOCookieCacheManager ssoCookieCacheManager;

	private static final Logger log = LoggerFactory.getLogger(BasicAuthFilter.class);

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_FILTER;
	}

	@Override
	public int filterOrder() {
		return 6;
	}

	@Override
	public Object run(){
		RequestContext ctx = RequestContext.getCurrentContext();
		String authToken = RequestContextUtil.getAuthToken(ctx);
		if (authToken == null || authToken.isEmpty()) {
			log.debug("Token is empty");
			RequestContextUtil.setResponseUnauthorized(ctx);
			return null;
		}

		String cookieName = getCookieName(ctx);
		if (cookieName != null) {
			SSOCookieCacheManager.Cookie ssoCookieFromCache = ssoCookieCacheManager.getSSOCookieFromCache(authToken, cookieName);
			if (ssoCookieFromCache != null) {
				ctx.addZuulRequestHeader("Cookie", ssoCookieFromCache.getName() + "=" + ssoCookieFromCache.getValue());
			}
		}

		String userCredentials = getUserCredentials(authToken);
		if (userCredentials == null) {
			log.debug("Missing user credentials for token : " + authToken);
			RequestContextUtil.setResponseUnauthorized(ctx);
			return null;
		} else {
			ctx.addZuulRequestHeader("Authorization", "Basic " +
					new String(Base64.encodeBase64((userCredentials).getBytes(GatewayConstants.UTF8_CHARSET)), Charset.forName("US-ASCII")));
		}

		setRefererHeader(ctx);

		//Disable default zuul error Filter
		ctx.set("sendErrorFilter.ran", true);
		return null;
	}

	private String getCookieName(RequestContext ctx) {
		URL routeHost = ctx.getRouteHost();
		if (routeHost != null) {
			String routeHostPath = routeHost.getPath();
			if (routeHostPath.startsWith("/qsystem/mobile")) {
				return GatewayConstants.JSESSIONID;
			} else {
				return GatewayConstants.SSOCOOKIE;
			}
		}
		return null;
	}

	private String getUserCredentials(String apiToken) {
		OrchestraProperties.UserCredentials credentials = orchestraProperties.getCredentials(apiToken);
		if (credentials != null) {
			return credentials.getUser() + ":" + credentials.getPasswd();
		}
		return null;
	}

	private void setRefererHeader(RequestContext ctx) {
		ctx.addZuulRequestHeader("referer", ctx.getRequest().getRequestURL().toString());
	}
}
