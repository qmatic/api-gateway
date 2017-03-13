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

    public void writeSSOCookieToCache(String authToken, Cookie cookie) {
        String key = constructKey(authToken, cookie.getName());

        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return;
        }
        log.debug("Writing to cache : \"{}\" with authtoken \"{}\" and cookie \"{}\"", cacheName, authToken, cookie.getName() + "=" + cookie.getValue());
        cacheContainer.put(key, cookie);
    }

    public Cookie getSSOCookieFromCache(String authToken, String cookieName) {
        Cache cacheContainer = cacheManager.getCache(cacheName);
        if (cacheContainer == null) {
            log.error("Unable to find cache : " + cacheName);
            return null;
        }
        String key = constructKey(authToken, cookieName);
        Cookie cookie = cacheManager.getCache(cacheName).get(key, Cookie.class);
        if (cookie == null) {
            log.debug("Unable to find {} for authtoken \"{}\" in cache : \"{}\"", cookieName, authToken, cacheName);
            return null;
        }
        log.debug("Reading {} from cache : \"{}\" using authtoken \"{}\"", cookieName, cacheName, authToken);
        return cookie;
    }

    private String constructKey(String authToken, String cookieName) {
        return authToken + cookieName;
    }

    public static class Cookie {
        private String name;
        private String value;

        public Cookie(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cookie cookie = (Cookie) o;

            if (name != null ? !name.equals(cookie.name) : cookie.name != null) return false;
            return value != null ? value.equals(cookie.value) : cookie.value == null;

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}