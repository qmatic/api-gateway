package com.qmatic.apigw.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.properties.OrchestraProperties;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@EnableConfigurationProperties(OrchestraProperties.class)
@Component
public class BasicAuthFilter extends ZuulFilter {

    @Autowired
	OrchestraProperties orchestraProperties;

    @Value("${orchestra.blockUnathorized}")
    private boolean blockUnathorized;

    private static final Logger log = LoggerFactory.getLogger(BasicAuthFilter.class);

	@Override
	public boolean shouldFilter() {
        return true;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		String token = getAuthToken();
		if (token == null || token.isEmpty()) {
			log.debug("Token is empty");
			unauthorized();
			return null;
		}
		String userCredentials = getUserCredentials(token);
		if (userCredentials == null) {
            		log.debug("Missing user credentials for token : " + token);
            		unauthorized();
			return null;
		}
		ctx.addZuulRequestHeader("Authorization", "Basic " +
				new String(Base64.encodeBase64((userCredentials).getBytes(GatewayConstants.UTF8_CHARSET)), Charset.forName("US-ASCII")));

		//Disable default zuul error Filter
		ctx.set("sendErrorFilter.ran", true);
		return null;
	}

    private void unauthorized() {
        if(!blockUnathorized) {
            log.debug("Forwarding unauthorized request");
            return;
        }
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(401);
        ctx.setSendZuulResponse(false);
    }

	private String getUserCredentials(String apiToken) {
		OrchestraProperties.UserCredentials credentials = orchestraProperties.getCredentials(apiToken);
		if (credentials != null) {
			return credentials.getUser() + ":" + credentials.getPasswd();
		}
		return null;
	}

	protected String getAuthToken() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRequest().getHeader(GatewayConstants.AUTH_TOKEN);
	}

}
