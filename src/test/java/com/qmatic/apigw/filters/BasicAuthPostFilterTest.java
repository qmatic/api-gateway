package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.SSOCookieCacheManager;
import com.qmatic.apigw.caching.SSOCookieCacheManager.Cookie;
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

    private static String API_TOKEN = "c7a1331a-32d-11e5-bf7f-feff819acdc9f";
    private static Cookie SSO_COOKIE = new Cookie("SSOcookie", "f01239a9-ee06-453a-9788-814da6f8368a");
    private static Cookie JSESSIONID_COOKIE = new Cookie("JSESSIONID", "f01239a9-ee06-453a-9788-814da6f8368a");
    private static String SSO_COOKIE_HEADER = "SSOcookie=f01239a9-ee06-453a-9788-814da6f8368a; authorization: Basic c3VwZXJhZG1pbjp1bGFu";
    private static String JSESSIONID_COOKIE_HEADER = "JSESSIONID=f01239a9-ee06-453a-9788-814da6f8368a; authorization: Basic c3VwZXJhZG1pbjp1bGFu";

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
        context.set(FilterConstants.RESPONSE_STATUS_CODE, 200);
        RequestContext.testSetCurrentContext(context);

        OrchestraProperties.UserCredentials userCredentials = new OrchestraProperties.UserCredentials();
        userCredentials.setUser("superadmin");
        userCredentials.setPasswd("ulan");

        when(this.orchestraProperties.getCredentials(API_TOKEN)).thenReturn(userCredentials);
    }

    @Test
    public void testFilterShouldRun() {
        Assert.assertTrue(new BasicAuthFilter().shouldFilter());
    }

    @Test
    public void testFilterRunWithSsoCookie() {
        RequestContext.getCurrentContext().addOriginResponseHeader("Set-Cookie", SSO_COOKIE_HEADER);

        basicAuthPostFilter.run();

        verify(ssoCookieCacheManager).writeSSOCookieToCache(API_TOKEN, SSO_COOKIE);
    }

    @Test
    public void testFilterRunWithJsessionId() {
        RequestContext.getCurrentContext().addOriginResponseHeader("Set-Cookie", JSESSIONID_COOKIE_HEADER);

        basicAuthPostFilter.run();

        verify(ssoCookieCacheManager).writeSSOCookieToCache(API_TOKEN, JSESSIONID_COOKIE);
    }
}
