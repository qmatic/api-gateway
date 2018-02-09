package com.qmatic.apigw.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CorsPathTest {

    private CorsPath testee;

    @BeforeMethod
    public void init() {
        testee = new CorsPath();
    }

    @Test
    public void correctValueParsing() {
        testee.setAllowedHeaders("foo-h, bar-h");
        testee.setExposedHeaders("foo-e, bar-e");
        testee.setAllowCredentials(true);
        testee.setAllowedMethods("foo-m, bar-m");
        testee.setAllowedOrigins("foo-o, bar-o");
        testee.setMaxAge(1234L);
        testee.setPath("foo-p");

        assertEquals(testee.getAllowedHeaders(), asList("foo-h", "bar-h"));
        assertEquals(testee.getExposedHeaders(), asList("foo-e", "bar-e"));
        assertTrue(testee.getAllowCredentials());
        assertEquals(testee.getAllowedMethods(), asList("foo-m", "bar-m"));
        assertEquals(testee.getAllowedOrigins(), asList("foo-o", "bar-o"));
        assertEquals(testee.getMaxAge(), new Long(1234L));
        assertEquals(testee.getPath(), "foo-p");
        assertTrue(testee.toString().contains("foo-h"));
        assertTrue(testee.toString().contains("foo-e"));
        assertTrue(testee.toString().contains("foo-m"));
        assertTrue(testee.toString().contains("1234"));
        assertTrue(testee.toString().contains("true"));
    }

    @Test
    public void sameHashCode() {
        testee.setPath("foo-p");
        CorsPath compare = new CorsPath();
        compare.setPath("foo-p");

        assertEquals(testee.hashCode(), compare.hashCode());
    }

    @Test
    public void equals() {
        testee.setPath("foo-p");
        CorsPath compare = new CorsPath();
        compare.setPath("foo-p");

        assertTrue(testee.equals(compare));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingOrigin() {
        testee.setPath("foo-p");

        testee.validate();

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingPath() {
        testee.setAllowedOrigins("foo-o");

        testee.validate();

    }

    @Test
    public void valid() {
        testee.setPath("foo-p");
        testee.setAllowedOrigins("foo-o");

        testee.validate();

    }

    @Test
    public void initiatedViaPath() {
        testee.setPath("foo-p");

        assertTrue(testee.isInitiated());
    }

    @Test
    public void initiatedViaOrigin() {
        testee.setAllowedOrigins("foo-o");

        assertTrue(testee.isInitiated());
    }
}