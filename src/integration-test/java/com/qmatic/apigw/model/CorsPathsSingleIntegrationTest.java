package com.qmatic.apigw.model;

import com.qmatic.apigw.CorsTestSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@SpringBootTest
@Import(CorsTestSpringConfig.class)
@ActiveProfiles("cors-single")
public class CorsPathsSingleIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CorsPathsSingleProps corsPathsSingle;

    @Test
    public void readMultipleCorsPaths() {

        assertEquals(corsPathsSingle.getPath(), "/test");
    }
}