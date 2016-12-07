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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BasicAuthPostFilterTest {

    @InjectMocks
    private BasicAuthPostFilter basicAuthPostFilter;

    @Mock
    private OrchestraProperties orchestraProperties;

    @Mock
    private SSOCookieCacheManager ssoCookieCacheManager;

    static String API_TOKEN = "c7a1331a-32d-11e5-bf7f-feff819acdc9f";
    static String COOKIE = "f01239a9-ee06-453a-9788-814da6f8368a";
    static String COOKIE_HEADER  = "SSOcookie=f01239a9-ee06-453a-9788-814da6f8368a; authorization: Basic c3VwZXJhZG1pbjp1bGFu";

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
        context.addZuulResponseHeader("Set-Cookie", COOKIE_HEADER);
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
        RequestContext.getCurrentContext().set(GatewayConstants.RESPONSE_STATUS_CODE, 200);
        basicAuthPostFilter.run();

        verify(ssoCookieCacheManager).writeSSOCookieToCache(API_TOKEN, COOKIE);
    }

    @Test
    public void testFilterRunForStatus401() {
        OrchestraProperties.UserCredentials userCredentials = new OrchestraProperties.UserCredentials();
        userCredentials.setUser("superadmin");
        userCredentials.setPasswd("ulan");
        when(this.orchestraProperties.getCredentials(API_TOKEN)).thenReturn(userCredentials);
        RequestContext.getCurrentContext().set(GatewayConstants.RESPONSE_STATUS_CODE, 401);

        basicAuthPostFilter.run();

        verify(ssoCookieCacheManager).deleteSSOCookieFromCache(API_TOKEN);
    }
}
