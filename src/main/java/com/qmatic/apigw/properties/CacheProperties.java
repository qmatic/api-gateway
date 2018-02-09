package com.qmatic.apigw.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("cache")
public class CacheProperties {

    private Map<String, CacheRoute> cacheRoutes = new LinkedHashMap<>();

    private String defaultCacheName;
    private Boolean useDefaultCache;
    private Boolean defaultCacheUniquePerQueryParameter;

    public String getDefaultCacheName() {
        return defaultCacheName;
    }

    public Boolean isUseDefaultCache() {
        return useDefaultCache;
    }

    public Boolean isDefaultCacheUniquePerQueryParameter() {
        return defaultCacheUniquePerQueryParameter;
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, CacheRoute> entry : this.cacheRoutes.entrySet()) {
            CacheRoute value = entry.getValue();
            value.id = entry.getKey();
            value.setupListOfRoutes();
        }
    }

    public Map<String, CacheRoute> getRoutes() {
        return cacheRoutes;
    }

    public List<CacheRoute> getRoutesForProxy(String proxy) {
        List<CacheRoute> routes = new ArrayList<>();
        for (Map.Entry<String, CacheRoute> entry : this.cacheRoutes.entrySet()) {
            CacheRoute cacheRouteItem = entry.getValue();
            if(cacheRouteItem.getRoutes().contains(proxy)) {
                routes.add(cacheRouteItem);
            }
        }
        return routes;
    }

    public CacheRoute getDefaultCacheRoute() {
        CacheRoute cacheRoute = new CacheRoute();
        cacheRoute.cacheName = defaultCacheName;
        cacheRoute.uniquePerQueryParameter = defaultCacheUniquePerQueryParameter;
        cacheRoute.defaultCache = true;
        return cacheRoute;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CacheRoute {

        private String id;
        private String cacheName;
        private String match;
        private String routes;
        private Boolean uniquePerQueryParameter = false;
        private Boolean defaultCache = false;

        private List<String> listOfRoutes;

        public void setupListOfRoutes() {
            if(routes == null) {
                return;
            }
            String routesTrimmed = routes.replaceAll(", *", ",");
            listOfRoutes = Arrays.asList(routesTrimmed.split(","));
        }

        public String getId() {
            return id;
        }

        public String getCacheName() {
            return cacheName;
        }

        public String getMatch() {
            return match;
        }

        public List<String> getRoutes() {
            return listOfRoutes;
        }

        public Boolean isUniquePerQueryParameter() {
            return uniquePerQueryParameter;
        }

        public Boolean isDefaultCache() {
            return defaultCache;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }

        public void setMatch(String match) {
            this.match = match;
        }

        public void setRoutes(String routes) {
            this.routes = routes;
        }

        public void setDefaultCache(Boolean defaultCache) {
            this.defaultCache = defaultCache;
        }

        public void setUniquePerQueryParameter(Boolean uniquePerQueryParameter) {
            this.uniquePerQueryParameter = uniquePerQueryParameter;
        }
    }
}
