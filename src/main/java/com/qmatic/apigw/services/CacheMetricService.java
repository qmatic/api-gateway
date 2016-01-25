package com.qmatic.apigw.services;

import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

@RestController
public class CacheMetricService {

    public static final String GATEWAY_EHCACHE_NAME = "gateway";

    public static final String METRICS_PATH = "/metrics/cache";

    private static final Logger log = LoggerFactory.getLogger(CacheMetricService.class);

    @Value("${cache.allowCacheMetricsFromAllHosts}")
    private boolean allowCacheMetricsFromAllHosts;

    @RequestMapping(value = METRICS_PATH, method = RequestMethod.GET, produces = "application/json")
    public Map<String, Map<String, Long>> cacheMetrics(HttpServletRequest request, HttpServletResponse response) {
        String clientIP = request.getRemoteAddr();
        if(!allowAccessToMetrics(clientIP)) {
            log.info("Disallowing access from IP : " + clientIP);
            sendForbidden(response);
            return null;
        }
        CacheManager cm = CacheManager.getCacheManager(GATEWAY_EHCACHE_NAME);
        Map<String, Map<String, Long>> result = new CacheMetricGenerator(cm).getStatistics();
        return result;
    }

    private void sendForbidden(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access not allowed");
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }

    private boolean allowAccessToMetrics(String clientIP) {
        if(allowCacheMetricsFromAllHosts) {
            return true;
        }
        return isRequestFromLocalHost(clientIP);
    }

    private boolean isRequestFromLocalHost(String clientIP) {
        try {
            InetAddress hostAddress = InetAddress.getByName(clientIP);
            if (hostAddress.isAnyLocalAddress() || hostAddress.isLoopbackAddress()) {
                return true;
            }
            return NetworkInterface.getByInetAddress(hostAddress) != null;
        } catch (UnknownHostException | SocketException e) {}
        return false;
    }
}
