package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.rest.VisitStatus;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MyVisitCurrentStatusPreFilterTest {

    @Mock
    private VisitCacheManager visitCacheManager;

    @InjectMocks
    MyVisitCurrentStatusPreFilter testee;

    RequestContext ctx;
    MockHttpServletRequest httpServletRequest;
    MockHttpServletResponse httpServletResponse;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        ctx = new RequestContext();
        ctx.set(FilterConstants.PROXY, FilterConstants.MY_VISIT_CURRENT_STATUS);
        RequestContext.testSetCurrentContext(ctx);

        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(GatewayConstants.AUTH_TOKEN, "d0516eee-a32d-11e5-bf7f-feff819cdc9f");
        ctx.setRequest(httpServletRequest);

        httpServletResponse = new MockHttpServletResponse();
        ctx.setResponse(new MockHttpServletResponse());
    }

    @Test
    public void filterShouldRun() {
        assertTrue(testee.shouldFilter());
    }

    @Test
    public void filterShouldNotRunWhenUnauthorized() {
        ctx.setResponseStatusCode(401);
        assertFalse(testee.shouldFilter());
    }

    @Test
    public void filterShouldNotRunWhenNotFound() {
        ctx.setResponseStatusCode(404);
        assertFalse(testee.shouldFilter());
    }

    @Test
    public void filterShouldNotRunWhenBadRequest() {
        ctx.setResponseStatusCode(400);
        assertFalse(testee.shouldFilter());
    }

    @Test
    public void abortIfInvalidPathParameter() {
        httpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/invalid/visits/1");

        testee.run();

        assertEquals(ctx.getResponse().getStatus(), 400);
    }

    @Test
    public void visitNotFound() {
        httpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/1/visits/1");
        when(visitCacheManager.getVisit(anyLong(), anyLong())).thenReturn(null);
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                ctx.getResponse().setStatus(404);
                return null;
            }
        }).when(visitCacheManager).createVisitNotFoundResponse(ctx);

        testee.run();

        assertEquals(ctx.getResponse().getStatus(), 404);
    }

    @Test
    public void visitFound() {
        httpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/1/visits/1");
        when(visitCacheManager.getVisit(anyLong(), anyLong())).thenReturn(new VisitStatus());

        testee.run();

        assertEquals(ctx.getResponse().getStatus(), 200);
    }
}
