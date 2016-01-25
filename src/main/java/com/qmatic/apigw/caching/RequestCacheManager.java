package com.qmatic.apigw.caching;

import com.qmatic.apigw.properties.CacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import static com.qmatic.apigw.properties.CacheProperties.*;

@Component
@EnableConfigurationProperties(CacheProperties.class)
public class RequestCacheManager {

    private static final Logger log = LoggerFactory.getLogger(RequestCacheManager.class);

    private CacheManager cacheManager;

    @Autowired
    protected CacheProperties cacheProperties;

    @Autowired
    public RequestCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void writeRequestToCache(String proxy, String requestURI, String routeHost, String queryParameters, String value) {
        if (!isHandledByCache(proxy, requestURI)) {
            log.debug("Did not find cache for request: \"{}\"", requestURI);
            return;
        }

        CacheRoute cacheRoute = getCacheRouteForRequest(proxy, requestURI);
        String cacheName = getCacheNameForRequest(cacheRoute);
        String cacheKey = getCacheKey(cacheRoute, requestURI, routeHost, queryParameters);
        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return;
        }
        log.debug("Writing to cache : \"{}\" with key \"{}\" for proxy : \"{}\" and request : \"{}\"", cacheName, cacheKey, proxy, requestURI);
        cacheContainer.put(cacheKey, value);
    }

    public String getRequestFromCache(String proxy, String requestURI, String routeHost, String queryParameters) {
        if (!isHandledByCache(proxy, requestURI)) {
            log.debug("Did not find cache for request: \"{}\"", requestURI);
            return null;
        }

        CacheRoute cacheRoute = getCacheRouteForRequest(proxy, requestURI);
        String cacheName = getCacheNameForRequest(cacheRoute);
        String cacheKey = getCacheKey(cacheRoute, requestURI, routeHost, queryParameters);

        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return null;
        }
        Cache.ValueWrapper valueWrapper = cacheManager.getCache(cacheName).get(cacheKey);
        if (valueWrapper == null) {
            log.debug("Unable to find value for key \"{}\" in cache : \"{}\"", cacheKey, cacheName);
            return null;
        }
        log.debug("Reading from cache : \"{}\" using key \"{}\" for proxy : \"{}\" and request : \"{}\"", cacheName, cacheKey, proxy, requestURI);
        if (valueWrapper.get() == null) {
            log.debug("Cached value is null.");
            return null;
        }
        return valueWrapper.get().toString();
    }

    /**
     * for default cache we use full forwarding URL as key
     * for other caches we use the "request uri" as key
     */
    protected String getCacheKey(CacheRoute cacheRoute, String requestURI, String routeHost, String queryParameters) {
        if(cacheRoute.isDefaultCache()) {
            return getCacheKeyForDefaultCache(routeHost, requestURI, queryParameters);
        }
        return getCacheKeyForNoneDefaultCache(cacheRoute, requestURI, queryParameters);
    }

    protected String getCacheKeyForDefaultCache(String routeHost, String requestURI, String queryParameters) {
        String routeAndRequest = routeHost + requestURI;
        if(cacheProperties.isDefaultCacheUniquePerQueryParameter()) {
            return routeAndRequest + "?" + queryParameters;
        }
        return routeAndRequest;
    }

    protected String getCacheKeyForNoneDefaultCache(CacheRoute cacheRoute, String requestURI, String queryParameters) {
        if(cacheRoute.isUniquePerQueryParameter()) {
            return requestURI + "?" + queryParameters;
        }
        return requestURI;
    }


    public boolean isHandledByCache(String proxy, String requestURI) {
        CacheRoute cacheRouteForRequest = getCacheRouteForRequest(proxy, requestURI);
        if(cacheRouteForRequest.isDefaultCache()) {
            return cacheProperties.isUseDefaultCache();
        }
        return true;
    }

    public String getCacheNameForRequest(CacheRoute cacheRouteForRequest) {
        return cacheRouteForRequest.getCacheName();
    }

    public CacheRoute getCacheRouteForRequest(String proxy, String requestURI) {
        for(CacheRoute cacheRoute : cacheProperties.getRoutesForProxy(proxy)) {
            if(requestURI.matches(cacheRoute.getMatch())) {
                return cacheRoute;
            }
        }
        return cacheProperties.getDefaultCacheRoute();
    }

    protected void setCacheProperties(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }
}
