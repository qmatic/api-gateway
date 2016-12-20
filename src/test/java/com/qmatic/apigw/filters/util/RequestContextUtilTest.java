package com.qmatic.apigw.filters.util;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.filters.FilterConstants;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class RequestContextUtilTest {

    private static final String AUTH_TOKEN = "d0516eee-a32d-11e5-bf7f-feff819cdc9f";
    RequestContext ctx;
    MockHttpServletRequest httpServletRequest;
    MockHttpServletResponse httpServletResponse;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        ctx = new RequestContext();
        RequestContext.testSetCurrentContext(ctx);

        httpServletRequest = new MockHttpServletRequest();
        ctx.setRequest(httpServletRequest);

        httpServletResponse = new MockHttpServletResponse();
        ctx.setResponse(httpServletResponse);
    }

    @Test
    public void getPathParameter() throws Exception {
        httpServletRequest.setRequestURI(FilterConstants.BRANCHES + "/1");

        String actual = RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx);

        assertEquals(actual, "1");
    }

    @Test
    public void getAuthToken() throws Exception {
        httpServletRequest.addHeader(GatewayConstants.AUTH_TOKEN, AUTH_TOKEN);

        String actual = RequestContextUtil.getAuthToken(ctx);

        assertEquals(actual, AUTH_TOKEN);
    }

    @Test
    public void getQueryParameter() throws Exception {
        String visit_id = "1";
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put(FilterConstants.VISIT_ID, Collections.singletonList(visit_id));
        ctx.setRequestQueryParams(queryParams);

        String actual = RequestContextUtil.getQueryParameter(FilterConstants.VISIT_ID, ctx);

        assertEquals(actual, "1");
    }

    @Test
    public void setResponseUnauthorized() throws Exception {
        RequestContextUtil.setResponseUnauthorized(ctx);

        assertEquals(ctx.getResponseStatusCode(), HttpServletResponse.SC_UNAUTHORIZED);
        assertFalse(ctx.sendZuulResponse());
    }

    @Test
    public void setResponseBadRequest() throws Exception {
        RequestContextUtil.setResponseBadRequest(ctx, "reason");

        assertEquals(ctx.getResponseStatusCode(), HttpServletResponse.SC_BAD_REQUEST);
        assertEquals(ctx.getResponseBody(), HttpServletResponse.SC_BAD_REQUEST + ": reason");
        assertFalse(ctx.sendZuulResponse());
    }

    @Test
    public void setResponseNotFound() throws Exception {
        RequestContextUtil.setResponseNotFound(ctx);

        assertEquals(ctx.getResponseStatusCode(), HttpServletResponse.SC_NOT_FOUND);
        assertFalse(ctx.sendZuulResponse());
    }

    @Test
    public void setResponseInternalServerError() throws Exception {
        RequestContextUtil.setResponseInternalServerError(ctx);

        assertEquals(ctx.getResponseStatusCode(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertFalse(ctx.sendZuulResponse());
    }

    @Test
    public void writeResponse() throws Exception {
        RequestContextUtil.writeResponse(ctx, "response");

        assertEquals(ctx.getResponseStatusCode(), HttpServletResponse.SC_OK);
        assertEquals(ctx.getResponse().getHeader(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertEquals(ctx.getResponseBody(), "response");
        assertFalse(ctx.sendZuulResponse());
    }

    @Test
    public void setEmptyResponse() throws Exception {
        RequestContextUtil.setEmptyResponse(ctx);

        assertEquals(ctx.getResponseStatusCode(), HttpServletResponse.SC_OK);
        assertEquals(ctx.getResponse().getHeader(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertEquals(ctx.getResponseBody(), "{}");
        assertFalse(ctx.sendZuulResponse());
    }
}