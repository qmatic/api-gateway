package com.qmatic.apigw.filters.util;


import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import org.springframework.beans.factory.annotation.Value;

public class RequestContextUtil {


    public static String getPathParameter(String parameterName, RequestContext ctx) {
        String path = String.valueOf(ctx.getRequest().getRequestURL());

        int parameterIndex = path.indexOf(parameterName);
        if(parameterIndex < 0 ) {
            return null;
        }
        String[] parameterSubstring = path.substring(parameterIndex).split("/");

        if(parameterSubstring == null || parameterSubstring.length < 2) {
            return null;
        }

        return parameterSubstring[1];
    }

    public static String getAuthToken(RequestContext ctx) {
        return ctx.getRequest().getHeader(GatewayConstants.AUTH_TOKEN);
    }

    public static String getQueryParameter(String parameterName, RequestContext ctx) {
        return ctx.getRequestQueryParams().get(parameterName).get(0);
    }

    public static void setResponseUnauthorized( RequestContext ctx) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(401);
        ctx.setSendZuulResponse(false);
    }

    public static void setResponseBadrequest( RequestContext ctx ) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(400);
        ctx.setSendZuulResponse(false);
    }
}
