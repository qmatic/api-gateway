package com.qmatic.apigw.caching;

import com.qmatic.apigw.properties.CacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class SSOCookieCacheManager {

    private static final Logger log = LoggerFactory.getLogger(SSOCookieCacheManager.class);

    private CacheManager cacheManager;

    @Autowired
    protected CacheProperties cacheProperties;
    private String cacheName = "ssoCookieCache";

    @Autowired
    public SSOCookieCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void writeSSOCookieToCache(String authToken, String cookie) {

        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return;
        }
        log.debug("Writing to cache : \"{}\" with authtoken \"{}\" and SSOCookie \"{}\"", cacheName, authToken, cookie);
        cacheContainer.put(authToken, cookie);
    }

    public String getSSOCookieFromCache(String authtoken) {

        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return null;
        }
        Cache.ValueWrapper valueWrapper = cacheManager.getCache(cacheName).get(authtoken);
        if (valueWrapper == null) {
            log.debug("Unable to find SSOCookie for authtoken \"{}\" in cache : \"{}\"", authtoken, cacheName);
            return null;
        }
        log.debug("Reading from cache : \"{}\" using authtoken \"{}\"", cacheName, authtoken);
        if (valueWrapper.get() == null) {
            log.debug("Cached value is null.");
            return null;
        }
        return valueWrapper.get().toString();
    }

    public void deleteSSOCookieFromCache(String authToken) {
        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return;
        }
        log.debug("Removing from cache : \"{}\" using authtoken \"{}\"", cacheName, authToken);
        cacheContainer.evict(authToken);
    }
}