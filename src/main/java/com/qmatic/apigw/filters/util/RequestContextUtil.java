package com.qmatic.apigw.filters.util;


import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

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
        try {
            return ctx.getRequestQueryParams().get(parameterName).get(0);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public static void setResponseUnauthorized( RequestContext ctx) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
        ctx.setSendZuulResponse(false);
    }

    public static void setResponseBadRequest(RequestContext ctx ) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        ctx.setSendZuulResponse(false);
    }

    public static void setResponseNotFound(RequestContext ctx ) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(HttpServletResponse.SC_NOT_FOUND);
        ctx.setSendZuulResponse(false);
    }

    public static void setResponseInternalServerError(RequestContext ctx ) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ctx.setSendZuulResponse(false);
    }

    public static void writeResponse(RequestContext ctx, String response) {
        ctx.setResponseStatusCode(HttpServletResponse.SC_OK);
        ctx.getResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        ctx.setResponseBody(response);
        ctx.setSendZuulResponse(false);
    }
}
