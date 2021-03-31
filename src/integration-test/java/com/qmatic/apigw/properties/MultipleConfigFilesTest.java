package com.qmatic.apigw.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@TestPropertySource(properties = {
        "qmatic.api.gateway.additional.config.dir.locator=com.qmatic.apigw.properties.TestConfigurationFileLocator"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MultipleConfigFilesTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private StandardEnvironment env;

    @Autowired
    private OrchestraProperties orchestraProperties;

    @Test
    public void defaultValuesArePresent() {
        String enableChecksum = env.getProperty("cache.allowCacheMetricsFromAllHosts");

        assertNotNull(enableChecksum);
        assertEquals(enableChecksum, "false");
    }

    @Test
    public void valuesFromOverrideFilesAreReadIntoConfiguration() {
        String overriddenUrl = env.getProperty("orchestra.central.url");

        assertEquals(overriddenUrl, "http://127.0.0.1:8080");
    }

    @Test
    public void valueInApplicationPropertyHasPrecedence() {

        String hystrixThreshold = env.getProperty("hystrix.command.allVisitsOnBranch.circuitBreaker.requestVolumeThreshold");

        assertEquals(hystrixThreshold, "51");
    }

    @Test
    public void apiKeysAreOverridden() {
        assertNotNull(orchestraProperties.getCredentials("aaaaaa-bbbb-dddd-eeee-fffffffffffff"));
        assertNotNull(orchestraProperties.getCredentials("gggggg-hhhh-iiii-jjjj-kkkkkkkkkkkkk"));
    }
}