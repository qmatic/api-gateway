package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.SSOCookieCacheManager;
import com.qmatic.apigw.properties.OrchestraProperties;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class BasicAuthFilterTest {

    @InjectMocks
    private BasicAuthFilter basicAuthFilter;

    @Mock
    private OrchestraProperties orchestraProperties;

    @Mock
    private SSOCookieCacheManager ssoCookieCacheManager;

    static String API_TOKEN = "c7a1331a-32d-11e5-bf7f-feff819acdc9f";
    static String COOKIE = "ssocookie";

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
        RequestContext.testSetCurrentContext(context);
    }

    @Test
    public void testFilterShouldRun() {
        Assert.assertTrue(new BasicAuthFilter().shouldFilter());
    }

    @Test
    public void testFilterRunNoSSOCookie() {
        OrchestraProperties.UserCredentials userCredentials = new OrchestraProperties.UserCredentials();
        userCredentials.setUser("superadmin");
        userCredentials.setPasswd("ulan");
        when(this.orchestraProperties.getCredentials(API_TOKEN)).thenReturn(userCredentials);
        basicAuthFilter.run();
        verify(ssoCookieCacheManager).getSSOCookieFromCache(API_TOKEN);
        Assert.assertFalse(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("Authorization"));
        Assert.assertFalse(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("cookie"));
    }

    @Test
    public void testFilterRunSSOCookieExists() {
        OrchestraProperties.UserCredentials userCredentials = new OrchestraProperties.UserCredentials();
        userCredentials.setUser("superadmin");
        userCredentials.setPasswd("ulan");
        when(this.orchestraProperties.getCredentials(API_TOKEN)).thenReturn(userCredentials);
        when(ssoCookieCacheManager.getSSOCookieFromCache(API_TOKEN)).thenReturn(COOKIE);
        basicAuthFilter.run();

        Assert.assertFalse(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("Authorization"));
        Assert.assertTrue(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("cookie"));

    }

}
