package com.qmatic.apigw.services;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.statistics.LiveCacheStatistics;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheMetricGenerator {

    private CacheManager cacheManager;

    public CacheMetricGenerator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    public Map<String, Map<String, Long>> getStatistics() {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        for(String cacheName : cacheManager.getCacheNames()) {
            Map<String, Long> cacheResult = buildCacheResult(getCache(cacheName));
            result.put(cacheName, cacheResult);
        }
        return result;
    }

    private Map<String, Long> buildCacheResult(Cache cache) {
        Statistics cacheStatistics = cache.getStatistics();
        LiveCacheStatistics liveCacheStatistics = cache.getLiveCacheStatistics();

        Map<String, Long> cacheData = new LinkedHashMap<>();

        cacheData.put("size", liveCacheStatistics.getSize());
        cacheData.put("cacheHits", cacheStatistics.getCacheHits());
        cacheData.put("cacheMisses", cacheStatistics.getCacheMisses());
        cacheData.put("putCount", liveCacheStatistics.getPutCount());
        cacheData.put("evictionCount", cacheStatistics.getEvictionCount());

        cacheData.put("averageSearchTime", cacheStatistics.getAverageSearchTime());
        cacheData.put("inMemoryHits", cacheStatistics.getInMemoryHits());
        cacheData.put("inMemoryMisses", cacheStatistics.getInMemoryMisses());
        cacheData.put("objectCount", cacheStatistics.getObjectCount());
        cacheData.put("offHeapHits", cacheStatistics.getOffHeapHits());
        cacheData.put("offHeapMisses", cacheStatistics.getOffHeapMisses());
        cacheData.put("onDiskHits", cacheStatistics.getOnDiskHits());
        cacheData.put("onDiskMisses", cacheStatistics.getOnDiskMisses());
        cacheData.put("searchesPerSecond", cacheStatistics.getSearchesPerSecond());

        cacheData.put("expiredCount", liveCacheStatistics.getExpiredCount());
        cacheData.put("localHeapSize", liveCacheStatistics.getLocalHeapSize());
        cacheData.put("localOffHeapSize", liveCacheStatistics.getLocalOffHeapSize());
        cacheData.put("removedCount", liveCacheStatistics.getRemovedCount());
        cacheData.put("updateCount", liveCacheStatistics.getUpdateCount());

        cacheData.put("maxGetTimeMillis", liveCacheStatistics.getMaxGetTimeNanos() / 1000000L);
        return cacheData;
    }
}
