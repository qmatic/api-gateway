package com.qmatic.apigw.filters.util;

import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class Responder {
    public static void writeResponse(RequestContext ctx, String response) {
        ctx.setResponseStatusCode(HttpServletResponse.SC_OK);
        ctx.getResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        ctx.setResponseBody(response);
        ctx.setSendZuulResponse(false);
    }

    public static void abortRequest(RequestContext ctx) {
        ctx.removeRouteHost();
        ctx.setResponseStatusCode(400);
        ctx.setSendZuulResponse(false);
    }

}
