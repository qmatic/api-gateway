package com.qmatic.apigw.filters;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

public class OrchestraResponseErrorFilterTest {

    private Pair<String, String> eMessage = new Pair<>(FilterConstants.ERROR_MESSAGE, "ERROR: this line should be stopped filtered");

    @BeforeMethod
    public void setUp() throws Exception {
        RequestContext context = new RequestContext();
        context.setRequest(new MockHttpServletRequest());
        context.setResponse(new MockHttpServletResponse());
        context.set("error.status_code", HttpStatus.NOT_FOUND.value());
        context.set(FilterConstants.RESPONSE_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        context.addZuulResponseHeader(eMessage.first(), eMessage.second());
        RequestContext.testSetCurrentContext(context);
    }

    @Test
    public void testFilterShouldRun() {
        Assert.assertTrue(new OrchestraResponseErrorFilter().shouldFilter());
    }

    @Test
    public void testFilterRun() {
        Assert.assertTrue(RequestContext.getCurrentContext().getZuulResponseHeaders().contains(eMessage));
        new OrchestraResponseErrorFilter().run();
        Assert.assertFalse(RequestContext.getCurrentContext().getZuulResponseHeaders().contains(eMessage));
    }
}
