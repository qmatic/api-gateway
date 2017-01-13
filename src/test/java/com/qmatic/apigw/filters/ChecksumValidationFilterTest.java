package com.qmatic.apigw.filters;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.VisitCacheManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

public class ChecksumValidationFilterTest {

    @InjectMocks
    private ChecksumValidationFilter checksumValidationFilter;

    @Mock
    private OrchestraProperties orchestraProperties;

    @Mock
    private VisitCacheManager visitCacheManager;

    static String MOBILE_API_TOKEN = "d0516eee-a32d-11e5-bf7f-feff819cdc9f";
    static String SUPERADMIN_API_TOKEN = "c7a1331a-32d-11e5-bf7f-feff819acdc9f";
    static String CHECKSUM_ROUTE = "checksum_route";
    static String NON_CHECKSUM_ROUTE = "non_checksum_route";

    static String CHECKSUM_PARAMETER = FilterConstants.VISIT_CHECKSUM;
    static String CHECKSUM_VALUE = "1234";
    static String INVALID_CHECKSUM_VALUE = "4321";

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        OrchestraProperties.UserCredentials mobileUserCredentials = new OrchestraProperties.UserCredentials();
        mobileUserCredentials.setUser("mobile");
        mobileUserCredentials.setPasswd("ulan");
        when(this.orchestraProperties.getCredentials(MOBILE_API_TOKEN)).thenReturn(mobileUserCredentials);

        OrchestraProperties.UserCredentials superadminUserCredentials = new OrchestraProperties.UserCredentials();
        superadminUserCredentials.setUser("superadmin");
        superadminUserCredentials.setPasswd("ulan");
        when(this.orchestraProperties.getCredentials(SUPERADMIN_API_TOKEN)).thenReturn(superadminUserCredentials);

        Set<String> checksumRoutes = new HashSet<String>();
        checksumRoutes.add(CHECKSUM_ROUTE);
        when(this.orchestraProperties.getRoutes()).thenReturn(checksumRoutes);

        OrchestraProperties.ChecksumRoute mobileTicketParameter = new OrchestraProperties.ChecksumRoute();
        mobileTicketParameter.setParameter("ticket");
        when(this.orchestraProperties.getChecksumRoute(CHECKSUM_ROUTE)).thenReturn(mobileTicketParameter);

        when(this.visitCacheManager.getChecksum(anyLong(), anyLong())).thenReturn("1234");

    }

    @Test
    public void filterShouldRun() {
        MockHttpServletRequest mobileUserHttpServletRequest = createMockHttpRequest(MOBILE_API_TOKEN);
        createRequestContext(mobileUserHttpServletRequest, CHECKSUM_ROUTE, CHECKSUM_VALUE);

        Assert.assertTrue(checksumValidationFilter.shouldFilter());
    }

    @Test
    public void filterShouldRunWithInvalidCkecksum() {
        MockHttpServletRequest mobileUserHttpServletRequest = createMockHttpRequest(MOBILE_API_TOKEN);
        createRequestContext(mobileUserHttpServletRequest, CHECKSUM_ROUTE, INVALID_CHECKSUM_VALUE);

        Assert.assertTrue(checksumValidationFilter.shouldFilter());
    }

    @Test
    public void filterShouldNotRunInvalidRoute() {
        MockHttpServletRequest mobileUserHttpServletRequest = createMockHttpRequest(MOBILE_API_TOKEN);
        createRequestContext(mobileUserHttpServletRequest, NON_CHECKSUM_ROUTE, CHECKSUM_VALUE);

        Assert.assertFalse(checksumValidationFilter.shouldFilter());
    }

    @Test
    public void runFilterWithInvalidChecksum() {
        MockHttpServletRequest mobileUserHttpServletRequest = createMockHttpRequest(MOBILE_API_TOKEN);
        createRequestContext(mobileUserHttpServletRequest, CHECKSUM_ROUTE, INVALID_CHECKSUM_VALUE);

        checksumValidationFilter.run();

        Assert.assertTrue(RequestContext.getCurrentContext().getResponseStatusCode() == HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void runFilterWithCorrectChecksum() {
        MockHttpServletRequest mobileUserHttpServletRequest = createMockHttpRequest(MOBILE_API_TOKEN);
        createRequestContext(mobileUserHttpServletRequest, CHECKSUM_ROUTE, CHECKSUM_VALUE);

        checksumValidationFilter.run();

        Assert.assertFalse(RequestContext.getCurrentContext().getResponseStatusCode() == HttpServletResponse.SC_UNAUTHORIZED);
    }


    private void createRequestContext(MockHttpServletRequest httpServletRequest, String proxyRoute, String checksum) {
        RequestContext context = new RequestContext();
        context.setRequest(httpServletRequest);
        context.setResponse(new MockHttpServletResponse());

        context.set("proxy", proxyRoute);

        Map<String, List<String>> queryParams = new HashMap<>();
        List<String> queryParamValues = new ArrayList<String>();
        queryParamValues.add(checksum);
        queryParams.put(CHECKSUM_PARAMETER, queryParamValues);
        context.setRequestQueryParams(queryParams);

        RequestContext.testSetCurrentContext(context);
    }

    private MockHttpServletRequest createMockHttpRequest(String userAuthToken) {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(GatewayConstants.AUTH_TOKEN, userAuthToken);
        mockHttpServletRequest.setRequestURI("http://localhost:9090/MobileTicket/MyVisit/branches/1/ticket/1");

        return mockHttpServletRequest;
    }
}
