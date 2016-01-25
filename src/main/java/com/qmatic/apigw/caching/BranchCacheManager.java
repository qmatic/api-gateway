package com.qmatic.apigw.caching;

import com.qmatic.apigw.properties.OrchestraProperties;
import com.qmatic.apigw.rest.CentralRestClient;
import com.qmatic.common.geo.Branch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BranchCacheManager {

    private static final Logger log = LoggerFactory.getLogger(BranchCacheManager.class);
    private static final String BRANCH_CACHE_NAME = "branches";
    private static final String ALL_BRANCHES_CACHE_KEY = "allBranches";
    private static final String SERVICE_BRANCHES_CACHE_KEY = "serviceBranches";

    private CacheManager cacheManager;
    private CentralRestClient centralClient;

    @Autowired
    public BranchCacheManager(CacheManager cacheManager, CentralRestClient centralClient) {
        this.cacheManager = cacheManager;
        this.centralClient = centralClient;
    }

    public List<Branch> listAllBranches(OrchestraProperties.UserCredentials userCredentials) {
        return new ArrayList<>(getAllBranches(userCredentials).values());
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Branch> getAllBranches(OrchestraProperties.UserCredentials userCredentials) {
        Map<Long, Branch> allBranches = null;
        Cache branchCache = cacheManager.getCache(BRANCH_CACHE_NAME);

        if (branchCache != null) {
            Cache.ValueWrapper cachedItem = branchCache.get(ALL_BRANCHES_CACHE_KEY);
            if (cachedItem != null) {
                allBranches = (Map<Long, Branch>) cachedItem.get();
            }

            if (allBranches == null) {
                allBranches = mapIdToBranch(centralClient.getAllBranchesFromCentral(userCredentials));
                branchCache.put(ALL_BRANCHES_CACHE_KEY, allBranches);
            }
        } else {
            logCacheError(BRANCH_CACHE_NAME);
            allBranches = mapIdToBranch(centralClient.getAllBranchesFromCentral(userCredentials));
        }

        return allBranches;
    }

    @SuppressWarnings("unchecked")
    public List<Branch> getBranchesForService(Long serviceId, OrchestraProperties.UserCredentials userCredentials) {
        Set<Long> serviceBranchIds = null;
        Cache serviceBranchCache = cacheManager.getCache(SERVICE_BRANCHES_CACHE_KEY);

        if (serviceBranchCache != null) {
            Cache.ValueWrapper cachedItem = serviceBranchCache.get(serviceId);
            if (cachedItem != null) {
                serviceBranchIds = (Set<Long>) cachedItem.get();
            }

            if (serviceBranchIds == null) {
                serviceBranchIds = mapBranchToId(centralClient.getBranchesForServiceFromCentral(serviceId, userCredentials));
                serviceBranchCache.put(serviceId, serviceBranchIds);
            }
        } else {
            logCacheError(SERVICE_BRANCHES_CACHE_KEY);
            serviceBranchIds = getBranchIds(getAllBranches(userCredentials));
        }

        return convertFromBranchIds(serviceBranchIds, userCredentials);
    }

    private Set<Long> mapBranchToId(Branch[] branches) {
        Set<Long> branchIds = new HashSet<>();
        if (branches != null) {
            for (Branch branch : branches) {
                branchIds.add(branch.getId());
            }
        }
        return branchIds;
    }

    private Map<Long, Branch> mapIdToBranch(Branch[] allBranches) {
        Map<Long, Branch> branchMap = new HashMap<>();
        if (allBranches != null) {
            for (Branch branch : allBranches) {
                branchMap.put(branch.getId(), branch);
            }
        }
        return branchMap;
    }

    private Set<Long> getBranchIds(Map<Long, Branch> allBranches) {
        Set<Long> branchIds = new HashSet<>();
        if (allBranches != null) {
            for (Branch branch : allBranches.values()) {
                branchIds.add(branch.getId());
            }
        }
        return branchIds;
    }

    private List<Branch> convertFromBranchIds(Set<Long> branchIds, OrchestraProperties.UserCredentials userCredentials) {
        List<Branch> retBranches = new ArrayList<>();
        Map<Long, Branch> allBranches = getAllBranches(userCredentials);
        for (Long branchId : branchIds) {
            Branch branch = allBranches.get(branchId);
            if (branch != null) {
                retBranches.add(branch);
            }
        }
        return retBranches;
    }

    private void logCacheError(String cacheName) {
        log.debug("No cache found for {}. Broken or erroneous configuration? Retrieving from central instead.", cacheName);
    }

}
