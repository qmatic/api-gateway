package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.filters.BasicAuthFilter;
import com.qmatic.apigw.properties.OrchestraProperties;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * Created by davtol on 2016-01-13.
 */
public class BasicAuthFilterTest {

    @InjectMocks
    private BasicAuthFilter basicAuthFilter;

    @Mock
    private OrchestraProperties orchestraProperties;

    static String API_TOKEN = "c7a1331a-32d-11e5-bf7f-feff819acdc9f";

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(GatewayConstants.AUTH_TOKEN, API_TOKEN);
        RequestContext context = new RequestContext();
        context.setRequest(mockHttpServletRequest);
        //context.setResponse(new MockHttpServletResponse());
        //context.set("error.status_code", HttpStatus.NOT_FOUND.value());
        //context.set(GatewayConstants.RESPONSE_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        //context.addZuulResponseHeader(eMessage.first(), eMessage.second());
        RequestContext.testSetCurrentContext(context);
    }

    @Test
    public void testFilterShouldRun() {
        Assert.assertTrue(new BasicAuthFilter().shouldFilter());
    }

    @Test
    public void testFilterRun() {
        OrchestraProperties.UserCredentials userCredentials = new OrchestraProperties.UserCredentials();
        userCredentials.setUser("superadmin");
        userCredentials.setPasswd("ulan");
        when(this.orchestraProperties.getCredentials(API_TOKEN)).thenReturn(userCredentials);
        basicAuthFilter.run();
        Assert.assertFalse(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("Authorization"));
    }

}
