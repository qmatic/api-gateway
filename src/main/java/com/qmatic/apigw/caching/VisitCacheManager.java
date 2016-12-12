package com.qmatic.apigw.caching;

import com.netflix.zuul.context.RequestContext;
import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.properties.OrchestraProperties;
import com.qmatic.apigw.rest.CentralRestClient;
import com.qmatic.apigw.rest.VisitStatus;
import com.qmatic.apigw.rest.VisitStatusMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class VisitCacheManager {

    private static final Logger log = LoggerFactory.getLogger(VisitCacheManager.class);
    private static final String VISITS_ON_BRANCH_CACHE = "visitsOnBranchCache";

    private CacheManager cacheManager;

    @Autowired
    OrchestraProperties orchestraProperties;
    
    @Autowired
    CentralRestClient centralRestClient;

    @Autowired
    public VisitCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private void cacheVisits(Long branchId, VisitStatusMap visitsOnBranch) {
        Cache visitsOnBranchCache = cacheManager.getCache(VISITS_ON_BRANCH_CACHE);
        visitsOnBranchCache.put(branchId, visitsOnBranch);
    }

    public VisitStatus getVisit(Long branchId, Long visitId) {
        VisitStatus visit = null;
        Cache visitsOnBranchCache = cacheManager.getCache(VISITS_ON_BRANCH_CACHE);
        if (visitsOnBranchCache != null) {
            VisitStatusMap visitsOnBranch = visitsOnBranchCache.get(branchId, VisitStatusMap.class);
            if (visitsOnBranch == null) {
                OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(getAuthToken());
                visitsOnBranch = centralRestClient.getAllVisitsOnBranch(branchId, userCredentials);
                cacheVisits(branchId, visitsOnBranch);
            }
            visit = visitsOnBranch.get(visitId);
        } else {
            logCacheError(VISITS_ON_BRANCH_CACHE);
        }
        return visit;

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
        Cache visitsOnBranchCache = cacheManager.getCache(VISITS_ON_BRANCH_CACHE);
        if (visitsOnBranchCache != null) {
            VisitStatusMap visitsOnBranch = visitsOnBranchCache.get(branchId, VisitStatusMap.class);
            if (visitsOnBranch == null) {
                OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(getAuthToken());
                visitsOnBranch = centralRestClient.getAllVisitsOnBranch(branchId, userCredentials);
                cacheVisits(branchId, visitsOnBranch);
            }
            VisitStatus visit = visitsOnBranch.get(visitId);
            if (visit != null) {
                checksum = visit.getChecksum();
            }
        } else {
            logCacheError(VISITS_ON_BRANCH_CACHE);
        }
        return checksum;
    }
}
