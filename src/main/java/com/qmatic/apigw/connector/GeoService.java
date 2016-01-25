package com.qmatic.apigw.connector;

import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.caching.BranchCacheManager;
import com.qmatic.apigw.properties.OrchestraProperties;
import com.qmatic.common.geo.Branch;
import com.qmatic.common.geo.BranchGeoCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public final class GeoService extends GatewayService {

    private static final String ROOT_PATH = "/geo";

    private BranchGeoCalculator geoCalculator = new BranchGeoCalculator();
    private BranchCacheManager branchCache;
    private OrchestraProperties orchestraProperties;

    @Autowired
    public void setOrchestraProperties(OrchestraProperties orchestraProperties) {
        this.orchestraProperties = orchestraProperties;
    }

    @Autowired
    public void setBranchCacheManager(BranchCacheManager branchCache) {
        this.branchCache = branchCache;
    }

    @RequestMapping(
        value = ROOT_PATH + "/branches",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBranchesAtLocation(
        @RequestParam("longitude") Double longitude,
        @RequestParam("latitude") Double latitude,
        @RequestParam("radius") Integer radius,
        @RequestHeader(GatewayConstants.AUTH_TOKEN) String authToken) {

        OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(authToken);
        if (userCredentials == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Branch> allBranches = branchCache.listAllBranches(userCredentials);
        determineCurrentOpenCloseStatus(allBranches);
        List<Branch> sortedBranches = geoCalculator.sortBranchesByDistance(allBranches, longitude, latitude, radius, 0);
        return new ResponseEntity<>(sortedBranches, HttpStatus.OK);
    }

    @RequestMapping(
        value = ROOT_PATH + "/nearestbranches",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNearestBranches(
        @RequestParam("longitude") Double longitude,
        @RequestParam("latitude") Double latitude,
        @RequestParam("maxNrOfBranches") Integer maxNrOfBranches,
        @RequestHeader(GatewayConstants.AUTH_TOKEN) String authToken) {

        OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(authToken);
        if (userCredentials == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Branch> allBranches = branchCache.listAllBranches(userCredentials);
        determineCurrentOpenCloseStatus(allBranches);
        List<Branch> sortedBranches = geoCalculator.sortBranchesByDistance(allBranches, longitude, latitude, 0, maxNrOfBranches);
        return new ResponseEntity<>(sortedBranches, HttpStatus.OK);
    }

    @RequestMapping(
        value = ROOT_PATH + "/services/{serviceId}/branches",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBranchesForServiceAndLocation(
        @PathVariable("serviceId") Long serviceId,
        @RequestParam("longitude") Double longitude,
        @RequestParam("latitude") Double latitude,
        @RequestParam("radius") Integer radius,
        @RequestHeader(GatewayConstants.AUTH_TOKEN) String authToken) {

        OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(authToken);
        if (userCredentials == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Branch> serviceBranches = branchCache.getBranchesForService(serviceId, userCredentials);
        determineCurrentOpenCloseStatus(serviceBranches);
        List<Branch> branchesWithinRange = geoCalculator.cullBranchesOutOfRange(serviceBranches, longitude, latitude, radius);
        return new ResponseEntity<>(branchesWithinRange, HttpStatus.OK);
    }

    @RequestMapping(
        value = ROOT_PATH + "/services/{serviceId}/nearestbranches",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNearestBranchesForServiceAndLocation(
        @PathVariable("serviceId") Long serviceId,
        @RequestParam("longitude") Double longitude,
        @RequestParam("latitude") Double latitude,
        @RequestParam("maxNrOfBranches") Integer maxNrOfBranches,
        @RequestHeader(GatewayConstants.AUTH_TOKEN) String authToken) {

        OrchestraProperties.UserCredentials userCredentials = orchestraProperties.getCredentials(authToken);
        if (userCredentials == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Branch> serviceBranches = branchCache.getBranchesForService(serviceId, userCredentials);
        determineCurrentOpenCloseStatus(serviceBranches);
        List<Branch> nearestBranches = geoCalculator.sortBranchesByDistance(serviceBranches, longitude, latitude, 0, maxNrOfBranches);
        return new ResponseEntity<>(nearestBranches, HttpStatus.OK);
    }

    private void determineCurrentOpenCloseStatus(List<Branch> allBranches) {
        // The branches might have stayed in cache for a long time so we need to update
        // the open/close flags since they may be invalid at this point.
        if (allBranches != null) {
            for (Branch branch : allBranches) {
                geoCalculator.determineOpenCloseFlags(branch);
            }
        }
    }

}
