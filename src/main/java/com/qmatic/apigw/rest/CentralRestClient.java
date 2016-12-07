package com.qmatic.apigw.rest;

import com.qmatic.apigw.GatewayConstants;
import com.qmatic.apigw.properties.OrchestraProperties;
import com.qmatic.common.geo.Branch;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public final class CentralRestClient {

    private static final Logger log = LoggerFactory.getLogger(CentralRestClient.class);
    private static final String PATH_SERVICE_ID = "{serviceId}";
    private static final String PATH_BRANCH_ID = "{branchId}";

    @Value("${geoService.branches_url}")
    private String mobileBranchesUrl;
    @Value("${geoService.service_branches_url}")
    private String mobileServiceBranchesUrl;
    @Value("${checksumService.checksum_url}")
    private String checksumServiceChecksumUrl;
    private CentralHttpErrorHandler centralErrorHandler;
    private RestTemplate restTemplate;
    private OrchestraProperties orchestraProperties;

    @Autowired
    public void setOrchestraProperties(OrchestraProperties orchestraProperties) {
        this.orchestraProperties = orchestraProperties;
    }

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
        String convertedUrl = "";
        try {
            convertedUrl = mobileServiceBranchesUrl.replace(PATH_SERVICE_ID, Long.toString(serviceId));
            ResponseEntity<Branch[]> allBranches = restTemplate.exchange(convertedUrl, HttpMethod.GET,
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

    /*
    public Map<String, String> getChecksumsForBranch(Long branchId) {
        log.info("Getting checksum from Orchestra....eventually");
        String convertedUrl = "";
        try {
            convertedUrl = checksumServiceChecksumUrl.replace(PATH_BRANCH_ID, Long.toString(branchId));
            ResponseEntity<Map<String, String>> checksums = restTemplate.exchange(convertedUrl, HttpMethod.GET,
                    new HttpEntity<>(createAuthorizationHeader(userCredentials)), Map.class, new Object[]{});
            return checksums.getBody();
        } catch (IllegalArgumentException e) {
            log.debug("Could not replace " + PATH_SERVICE_ID + " in: " + mobileServiceBranchesUrl);
        }
        return new HashMap<String, String>();
    }
    */
}
