package com.qmatic.apigw.caching;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.filters.FilterConstants;
import com.qmatic.apigw.filters.util.RequestContextUtil;
import com.qmatic.apigw.properties.OrchestraProperties;
import com.qmatic.apigw.rest.CentralRestClient;
import com.qmatic.apigw.rest.VisitStatus;
import com.qmatic.apigw.rest.VisitStatusMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Component
public class VisitCacheManager {

    private static final Logger log = LoggerFactory.getLogger(VisitCacheManager.class);

    private CacheManager cacheManager;

    private ConcurrentHashMap<Long, Semaphore> throttleSemaphores = new ConcurrentHashMap();
    private static final Integer MAX_CALLS_PER_BRANCH = 1;

    @Value("${orchestra.central.throttleVisitOnBranches:false}")
    Boolean throttleVisitOnBranches;

    @Autowired
    OrchestraProperties orchestraProperties;

    @Autowired
    CentralRestClient centralRestClient;

    @Autowired
    public VisitCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private void cacheVisits(Long branchId, VisitStatusMap visitsOnBranch) {
        Cache visitsOnBranchCache = cacheManager.getCache(GatewayConstants.VISITS_ON_BRANCH_CACHE);
        visitsOnBranchCache.put(branchId, visitsOnBranch);
    }

    public VisitStatus getVisit(Long branchId, Long visitId) {
        VisitStatus visit = null;
        Cache visitsOnBranchCache = cacheManager.getCache(GatewayConstants.VISITS_ON_BRANCH_CACHE);
        if (visitsOnBranchCache != null) {
            VisitStatusMap visitsOnBranch = getVisitStatusMap(branchId, visitsOnBranchCache);
            visit = visitsOnBranch.get(visitId);
        } else {
            logCacheError(GatewayConstants.VISITS_ON_BRANCH_CACHE);
        }
        return visit;

    }

    private VisitStatusMap getVisitStatusMap(Long branchId, Cache visitsOnBranchCache) {
        VisitStatusMap visitsOnBranch = visitsOnBranchCache.get(branchId, VisitStatusMap.class);
        if (visitsOnBranch == null) {
            if (throttleVisitOnBranches) {
                visitsOnBranch = getVisitsOnBranchIfNotInCache(branchId, visitsOnBranchCache);
            } else {
                visitsOnBranch = cacheAndGetVisitsOnBranch(branchId);
            }
        }
        return visitsOnBranch;
    }

    private VisitStatusMap getVisitsOnBranchIfNotInCache(Long branchId, Cache visitsOnBranchCache) {
        Semaphore branchSemaphore = getSemaphore(branchId);
        VisitStatusMap visitsOnBranch = null;
        boolean aquired = false;
        try {
            log.debug("Aquiring semaphore for branch {}", branchId);
            branchSemaphore.acquire();
            aquired = true;
            log.debug("Aquired semaphore for branch {}", branchId);
            visitsOnBranch = visitsOnBranchCache.get(branchId, VisitStatusMap.class);
            if (visitsOnBranch == null) {
                log.debug("Visit data not found in cache for branch {}", branchId);
                visitsOnBranch = cacheAndGetVisitsOnBranch(branchId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (aquired) {
                branchSemaphore.release();
            }
        }

        log.debug("Returning visits on branch {}", branchId);
        return visitsOnBranch;
    }

    private Semaphore getSemaphore(Long branchId) {
        if (!throttleSemaphores.containsKey(branchId)) {
            throttleSemaphores.putIfAbsent(branchId, new Semaphore(MAX_CALLS_PER_BRANCH));
        }
        return throttleSemaphores.get(branchId);
    }

    private VisitStatusMap cacheAndGetVisitsOnBranch(Long branchId) {
        OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(getAuthToken());
        VisitStatusMap visitsOnBranch = centralRestClient.getAllVisitsOnBranch(branchId, userCredentials);
        cacheVisits(branchId, visitsOnBranch);
        return visitsOnBranch;
    }

    private String getAuthToken() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getRequest().getHeader(GatewayConstants.AUTH_TOKEN);
    }

    private void logCacheError(String cacheName) {
        log.debug("No cache found for {}. Broken or erroneous configuration?", cacheName);
    }

    public String getChecksum(Long branchId, Long visitId) {
        String checksum = null;
        Cache visitsOnBranchCache = cacheManager.getCache(GatewayConstants.VISITS_ON_BRANCH_CACHE);
        if (visitsOnBranchCache != null) {
            VisitStatusMap visitsOnBranch = getVisitStatusMap(branchId, visitsOnBranchCache);
            VisitStatus visit = visitsOnBranch.get(visitId);
            if (visit != null) {
                checksum = visit.getChecksum();
            }
        } else {
            logCacheError(GatewayConstants.VISITS_ON_BRANCH_CACHE);
        }
        return checksum;
    }

    public void createVisitNotFoundResponse(RequestContext ctx) {
        Long branchId = getBranchIdFromRequest(ctx);
        Long visitId = getVisitIdFromRequest(ctx);

        if(isNewVisit(branchId, visitId)) {
            RequestContextUtil.setResponseNotFoundCacheNotUpdate(ctx, createCacheNotUpdatedMessage());
        } else {
            RequestContextUtil.setResponseNotFound(ctx);
        }
    }

    private String createCacheNotUpdatedMessage() {
        Long cacheTimeToLiveSeconds = 1L;

        try {
            cacheTimeToLiveSeconds = getTimeToLive();
        } catch(Exception e) {
            log.error("Could not read cache property timeToLiveSeconds for "+ GatewayConstants.VISITS_ON_BRANCH_CACHE);
        }

        JSONObject obj = new JSONObject("{\"message\":\"New visits are not available until visitsOnBranchCache is refreshed\"," +
                "\"refreshRate\":"+cacheTimeToLiveSeconds+"}");

        return obj.toString();
    }

    private Boolean isNewVisit(Long branchId, Long visitId) {
        return visitId > getHighestVisitIdOnBranch(branchId);
    }

    private Long getHighestVisitIdOnBranch(Long branchId) {
        Long highestVisitIdOnBranch = -1L;
        Cache visitsOnBranchCache = cacheManager.getCache(GatewayConstants.VISITS_ON_BRANCH_CACHE);
        if (visitsOnBranchCache != null) {
            VisitStatusMap visitsOnBranch = getVisitStatusMap(branchId, visitsOnBranchCache);
            for(Long visitKey : visitsOnBranch.keySet()) {
                Long visitId = visitsOnBranch.get(visitKey).getVisitId();
                highestVisitIdOnBranch =  visitId > highestVisitIdOnBranch ? visitId : highestVisitIdOnBranch;
            }
        }
        return highestVisitIdOnBranch;
    }

    private Long getTimeToLive() throws Exception {
            EhCacheCacheManager ehCacheManager = (EhCacheCacheManager) cacheManager;
            EhCacheCache cache = (EhCacheCache) ehCacheManager.getCache(GatewayConstants.VISITS_ON_BRANCH_CACHE);
            return cache.getNativeCache().getCacheConfiguration().getTimeToLiveSeconds();
    }

    private Long getVisitIdFromRequest(RequestContext ctx) {
        OrchestraProperties.ChecksumRoute checksumRoute = orchestraProperties.getChecksumRoute((String) ctx.get(FilterConstants.PROXY));
        return Long.valueOf(RequestContextUtil.getPathParameter(checksumRoute.getParameter(), ctx));
    }

    private Long getBranchIdFromRequest(RequestContext ctx) {
        return Long.valueOf(RequestContextUtil.getPathParameter(FilterConstants.BRANCHES, ctx));
    }
}
