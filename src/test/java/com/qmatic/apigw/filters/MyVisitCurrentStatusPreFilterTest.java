package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.VisitCacheManager;
import com.qmatic.apigw.filters.util.RequestContextUtil;
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

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyLong;
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
        ctx.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
        assertFalse(testee.shouldFilter());
    }

    @Test
    public void filterShouldNotRunWhenNotFound() {
        ctx.setResponseStatusCode(HttpServletResponse.SC_NOT_FOUND);
        assertFalse(testee.shouldFilter());
    }

    @Test
    public void filterShouldNotRunWhenBadRequest() {
        ctx.setResponseStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        assertFalse(testee.shouldFilter());
    }

    @Test
    public void abortIfInvalidPathParameter() {
        httpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/invalid/visits/1");

        testee.run();

        assertEquals(ctx.getResponse().getStatus(), HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void visitNotFound() {
        httpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/1/visits/1");
        when(visitCacheManager.getVisit(anyLong(), anyLong())).thenReturn(null);
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                RequestContextUtil.setResponseNotFound(ctx);
                return null;
            }
        }).when(visitCacheManager).createVisitNotFoundResponse(ctx);

        testee.run();

        assertEquals(ctx.getResponse().getStatus(), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void visitFound() {
        httpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/1/visits/1");
        when(visitCacheManager.getVisit(anyLong(), anyLong())).thenReturn(new VisitStatus());

        testee.run();

        assertEquals(ctx.getResponse().getStatus(), HttpServletResponse.SC_OK);
    }
}
