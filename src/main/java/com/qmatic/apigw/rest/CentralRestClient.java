package com.qmatic.apigw.rest;

import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.filters.FilterConstants;
import com.qmatic.apigw.properties.OrchestraProperties;
import com.qmatic.common.geo.Branch;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Component
public final class CentralRestClient {

    private static final Logger log = LoggerFactory.getLogger(CentralRestClient.class);
    private static final String PATH_SERVICE_ID = "{serviceId}";

    @Value("${geoService.branches_url}")
    private String mobileBranchesUrl;
    @Value("${geoService.service_branches_url}")
    private String mobileServiceBranchesUrl;
    @Value("${currentStatus.visits_on_branch_url}")
    private String visitsOnBranchUrl;
    private CentralHttpErrorHandler centralErrorHandler;
    private RestTemplate restTemplate;

    @PostConstruct
    protected void init() {
        centralErrorHandler = new CentralHttpErrorHandler();
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(centralErrorHandler);
    }

    /**
     * @throws com.qmatic.apigw.exception.CentralCommunicationException Thrown upon received error from central
     */
    public Branch[] getAllBranchesFromCentral(OrchestraProperties.UserCredentials userCredentials) {
        log.debug("Retrieving all branches from central");
        ResponseEntity<Branch[]> allBranches = restTemplate.exchange(mobileBranchesUrl, HttpMethod.GET,
            new HttpEntity<>(createAuthorizationHeader(userCredentials)), Branch[].class, new Object[]{});
        return allBranches.getBody();
    }

    /**
     * @throws com.qmatic.apigw.exception.CentralCommunicationException Thrown upon received error from central
     */
    public Branch[] getBranchesForServiceFromCentral(Long serviceId, OrchestraProperties.UserCredentials userCredentials) {
        log.debug("Retrieving branches for service {} from central", serviceId);
        try {
            String url = mobileServiceBranchesUrl.replace(PATH_SERVICE_ID, Long.toString(serviceId));
            ResponseEntity<Branch[]> allBranches = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(userCredentials)), Branch[].class, new Object[]{});
            return allBranches.getBody();
        } catch (IllegalArgumentException e) {
            log.debug("Could not replace " + PATH_SERVICE_ID + " in: " + mobileServiceBranchesUrl);
        }
        return new Branch[] {};
    }

    private HttpHeaders createAuthorizationHeader(final OrchestraProperties.UserCredentials userCredentials) {
        return new HttpHeaders() {
            {
                String auth = userCredentials.getUser() + ":" + userCredentials.getPasswd();
                byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(GatewayConstants.UTF8_CHARSET));
                String authHeader = "Basic " + new String(encodedAuth, Charset.forName("US-ASCII"));
                set("Authorization", authHeader);
            }
        };
    }

    public VisitStatusMap getAllVisitsOnBranch(Long branchId, OrchestraProperties.UserCredentials userCredentials) {
        log.debug("Retrieving visits on branch {} from central", branchId);
        try {
            String url = visitsOnBranchUrl.replace("{" + FilterConstants.BRANCH_ID + "}", Long.toString(branchId));
            ResponseEntity<VisitStatusMap> allVisitsOnBranch = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(createAuthorizationHeader(userCredentials)), VisitStatusMap.class, new Object[]{});
            return allVisitsOnBranch.getBody();
        } catch (IllegalArgumentException e) {
            log.debug("Could not fetch visits for branch {} from central", branchId);
        }
        return new VisitStatusMap();
    }

}
