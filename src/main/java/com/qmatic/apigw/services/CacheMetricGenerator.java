package com.qmatic.apigw.services;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;

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
        StatisticsGateway cacheStatistics = cache.getStatistics();

        Map<String, Long> cacheData = new LinkedHashMap<>();

        cacheData.put("size", cacheStatistics.getSize());
        cacheData.put("cacheHits", cacheStatistics.cacheHitCount());
        cacheData.put("cacheMisses", cacheStatistics.cacheMissCount());
        cacheData.put("putCount", cacheStatistics.cachePutCount());
        cacheData.put("evictionCount", cacheStatistics.cacheEvictedCount());

        cacheData.put("averageSearchTime", cacheStatistics.cacheSearchOperation().latency().average().value().longValue());
        cacheData.put("inMemoryHits", cacheStatistics.localHeapHitCount());
        cacheData.put("inMemoryMisses", cacheStatistics.localHeapMissCount());
//        cacheData.put("objectCount", cacheStatistics.
        cacheData.put("offHeapHits", cacheStatistics.localOffHeapHitCount());
        cacheData.put("offHeapMisses", cacheStatistics.localOffHeapMissCount());
        cacheData.put("onDiskHits", cacheStatistics.localDiskHitCount());
        cacheData.put("onDiskMisses", cacheStatistics.localDiskMissCount());
        cacheData.put("searchesPerSecond", cacheStatistics.cacheSearchOperation().rate().value().longValue());

        cacheData.put("expiredCount", cacheStatistics.cacheExpiredCount());
        cacheData.put("localHeapSize", cacheStatistics.getLocalHeapSize());
        cacheData.put("localOffHeapSize", cacheStatistics.getLocalOffHeapSize());
        cacheData.put("removedCount", cacheStatistics.cacheRemoveCount());
        cacheData.put("updateCount", cacheStatistics.cachePutUpdatedCount());

        cacheData.put("maxGetTimeMillis", cacheStatistics.cacheSearchOperation().latency().maximum().value() / 1000000L);
        return cacheData;
    }
}
