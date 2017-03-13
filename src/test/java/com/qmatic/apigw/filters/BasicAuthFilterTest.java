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
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BasicAuthFilterTest {

    @InjectMocks
    private BasicAuthFilter basicAuthFilter;

    @Mock
    private OrchestraProperties orchestraProperties;

    @Mock
    private SSOCookieCacheManager ssoCookieCacheManager;

    static String API_TOKEN = "c7a1331a-32d-11e5-bf7f-feff819acdc9f";
    static Cookie COOKIE = new Cookie("SSOcookie", "something");

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
        context.setRouteHost(new URL("http://localhost:8080/qsystem/rest/something"));
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
    public void runFilterNoSSOCookie() {
        basicAuthFilter.run();

        verify(ssoCookieCacheManager).getSSOCookieFromCache(API_TOKEN, GatewayConstants.SSOCOOKIE);
        Assert.assertTrue(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("authorization"));
        Assert.assertFalse(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("cookie"));
    }

    @Test
    public void runFilterSSOCookieExists() {
        when(ssoCookieCacheManager.getSSOCookieFromCache(API_TOKEN, GatewayConstants.SSOCOOKIE)).thenReturn(COOKIE);

        basicAuthFilter.run();

        Assert.assertTrue(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("authorization"));
        Assert.assertTrue(RequestContext.getCurrentContext().getZuulRequestHeaders().containsKey("cookie"));
    }

    @Test
    public void runFilterIncorrectUser() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContext.getCurrentContext().setResponse(response);
        when(this.orchestraProperties.getCredentials(API_TOKEN)).thenReturn(null);
        basicAuthFilter.setBlockUnauthorized(true);

        basicAuthFilter.run();

        Assert.assertTrue(RequestContext.getCurrentContext().getResponseStatusCode() == HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void runFilterMobilePath() throws Exception {
        RequestContext.getCurrentContext().setRouteHost(new URL("http://localhost:8080/qsystem/mobile/something"));

        basicAuthFilter.run();

        verify(ssoCookieCacheManager).getSSOCookieFromCache(API_TOKEN, GatewayConstants.JSESSIONID);
    }
}
