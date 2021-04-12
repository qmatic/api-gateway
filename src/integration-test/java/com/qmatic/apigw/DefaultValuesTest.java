package com.qmatic.apigw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@SpringBootTest
public class DefaultValuesTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private StandardEnvironment env;

    @Test
    public void defaultCacheIsUniquePerQueryParameter() {
        String cacheUniquePerQueryParameter = env.getProperty("cache.defaultCacheUniquePerQueryParameter");

        assertNotNull(cacheUniquePerQueryParameter);
        assertEquals(cacheUniquePerQueryParameter, "true");
    }
}