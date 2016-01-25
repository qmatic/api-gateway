package com.qmatic.apigw.caching;

import com.qmatic.apigw.properties.CacheProperties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.qmatic.apigw.properties.CacheProperties.CacheRoute;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

@Test
public class RequestCacheManagerTest {

    @Mock
    CacheProperties cacheProperties;

    @Mock
    CacheManager cacheManager;

    private RequestCacheManager testee;

    @BeforeTest
    public void setup() {
        MockitoAnnotations.initMocks(this);

        testee = new RequestCacheManager(cacheManager);
        testee.setCacheProperties(cacheProperties);
    }

    public void testDefaultCacheCacheKeyWithoutQueryParameters() {
        String routeHost = "http://localhost:8080/something";
        String queryParameters = "parameter=someValue";
        String requestUri = "/services";
        boolean queryParameterSensitive = false;

        String cacheKey = getDefaultCacheCacheKey(routeHost, requestUri, queryParameters, queryParameterSensitive);
        String expectedKey = routeHost + requestUri;
        assertEquals(cacheKey, expectedKey);
    }

    public void testDefaultCacheCacheKeyWithQueryParameters() {
        String routeHost = "http://localhost:8080/something";
        String queryParameters = "parameter=someValue";
        String requestUri = "/services";
        boolean queryParameterSensitive = true;

        String cacheKey = getDefaultCacheCacheKey(routeHost, requestUri, queryParameters, queryParameterSensitive);
        String expectedKey = routeHost + requestUri + "?" + queryParameters;
        assertEquals(cacheKey, expectedKey);
    }

    protected String getDefaultCacheCacheKey(String routeHost, String requestUri, String queryParameters, boolean queryParameterSensitive) {
        String defaultCacheName = "request";

        boolean isDefaultCache = true;
        CacheRoute cacheRoute = getCacheRoute(isDefaultCache, queryParameterSensitive);

        mockDefaultCacheName(defaultCacheName);
        mockDefaultCacheQueryParameters(queryParameterSensitive);

        String cacheKey = testee.getCacheKey(cacheRoute, requestUri, routeHost, queryParameters);
        return cacheKey;
    }

    protected CacheRoute getCacheRoute(boolean isDefaultCache, boolean isQueryParameterSensitive) {
        CacheRoute route = new CacheRoute();
        route.setDefaultCache(isDefaultCache);
        route.setUniquePerQueryParameter(isQueryParameterSensitive);
        return route;
    }

    public void testNoneDefaultCacheCacheKeyWithoutQueryParameters() {
        String requestUri = "/services";
        String queryParameters = "parameter=someValue";
        boolean queryParameterSensitive = false;

        String cacheKey = getNoneDefaultCacheKey(requestUri, queryParameters, queryParameterSensitive);
        assertEquals(cacheKey, requestUri);
    }

    public void testNoneDefaultCacheKeyWithQueryParameters() {
        String requestUri = "/services";
        String queryParameters = "parameter=someValue";
        boolean queryParameterSensitive = true;

        String cacheKey = getNoneDefaultCacheKey(requestUri, queryParameters, queryParameterSensitive);
        String expectedKey = requestUri + "?" + queryParameters;
        assertEquals(cacheKey, expectedKey);
    }

    protected String getNoneDefaultCacheKey(String requestUri, String queryParameters, boolean queryParameterSensitive) {
        boolean isDefaultCache = false;
        CacheRoute cacheRoute = getCacheRoute(isDefaultCache, queryParameterSensitive);
        String cacheKey = testee.getCacheKey(cacheRoute, requestUri, null, queryParameters);
        return cacheKey;
    }

    private void mockDefaultCacheName(String defaultCacheName) {
        when(cacheProperties.getDefaultCacheName()).thenReturn(defaultCacheName);
    }

    private void mockDefaultCacheQueryParameters(boolean useQueryParameters) {
        when(cacheProperties.isDefaultCacheUniquePerQueryParameter()).thenReturn(useQueryParameters);
    }

    private void mockDefaultCache() {
        CacheRoute defaultCacheRoute = new CacheRoute();
        defaultCacheRoute.setDefaultCache(true);
        when(cacheProperties.getDefaultCacheRoute()).thenReturn(defaultCacheRoute);
    }

    public void testIsHandledWithDisabledDefaultCache() {
        mockUseDefaultCache(false);
        mockNoneMatchingRoutes();
        mockDefaultCache();

        boolean result = testee.isHandledByCache("proxy", "requestURI");

        assertFalse(result);
    }

    public void testIsHandledWithEnabledDefaultCache() {
        mockUseDefaultCache(true);
        mockNoneMatchingRoutes();
        mockDefaultCache();

        boolean result = testee.isHandledByCache("proxy", "requestURI");

        assertTrue(result);
    }

    public void testIsHandledWithCacheRouteMatch() {
        mockUseDefaultCache(false);
        String noneDefaultCacheName = "custom";
        String requestUri = "/services";
        String proxy = "proxy";

        mockMatchingRoute(requestUri, noneDefaultCacheName, proxy);
        ArrayList<CacheRoute> cacheRoutes = new ArrayList<>(cacheProperties.getRoutes().values());
        when(cacheProperties.getRoutesForProxy(proxy)).thenReturn(cacheRoutes);

        boolean result = testee.isHandledByCache(proxy, requestUri);
        assertTrue(result);
    }

    private void mockUseDefaultCache(boolean defaultCache) {
        when(cacheProperties.isUseDefaultCache()).thenReturn(defaultCache);
    }

    private void mockNoneMatchingRoutes() {
        HashMap<String, CacheRoute> cacheRoutes = new HashMap<>();
        for(Integer i=0;i<2;i++) {
            CacheRoute cacheRouteItem = new CacheRoute();
            cacheRouteItem.setCacheName("mock" + i.toString());
            cacheRouteItem.setMatch("notMatchingAnything" + i.toString());
            cacheRoutes.put("mock" + i.toString(), cacheRouteItem);
        }
        when(cacheProperties.getRoutes()).thenReturn(cacheRoutes);
    }

    private void mockMatchingRoute(String requestURI, String cacheName, String proxy) {
        CacheRoute cacheRouteItem = new CacheRoute();
        cacheRouteItem.setCacheName(cacheName);
        cacheRouteItem.setMatch(requestURI);
        cacheRouteItem.setRoutes(proxy);
        cacheRouteItem.setupListOfRoutes();
        HashMap<String, CacheRoute> cacheRoutes = new HashMap<>();
        cacheRoutes.put("mock", cacheRouteItem);
        when(cacheProperties.getRoutes()).thenReturn(cacheRoutes);
    }
}
