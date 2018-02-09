package com.qmatic.apigw.model;

import com.qmatic.apigw.CorsTestSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@SpringBootTest
@Import(CorsTestSpringConfig.class)
@ActiveProfiles("cors-multiple")
public class CorsPathsMultipleIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CorsPathsSingleProps corsPathsSingle;

    @Autowired
    private CorsPathsMultipleProps corsPathsMultiple;

    @Test
    public void readMultipleCorsPaths() {

        List<CorsPath> actual = corsPathsMultiple.getPaths();

        assertEquals(actual.size(), 3);
        CorsPath corsPath = actual.get(0);
        assertEquals(corsPath.getPath(), "/test1");
        assertEquals(corsPath.getAllowedOrigins(), asList("http://foo.com", "http://bar.com"));
        assertEquals(corsPath.getAllowedHeaders(), asList("FOO-HEADER", "BAR-HEADER"));
        assertEquals(corsPath.getExposedHeaders(), asList("FOO-EXPOSED", "BAR-EXPOSED"));
        assertEquals(corsPath.getAllowedMethods(), asList("GET", "POST", "PUT"));
        assertTrue(corsPath.getAllowCredentials());
        assertEquals(corsPath.getMaxAge(), new Long(10_000L));
        assertEquals(actual.get(1).getPath(), "/test2");
        assertNull(corsPathsSingle.getPath());
    }
}