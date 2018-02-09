package com.qmatic.apigw;

import com.qmatic.apigw.model.CorsPathsMultipleProps;
import com.qmatic.apigw.model.CorsPathsSingleProps;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

public class CorsSpringConfigTest {

    @InjectMocks
    private CorsSpringConfig testee = new CorsSpringConfig();

    @Mock
    private CorsPathsSingleProps single;

    @Mock
    private CorsPathsMultipleProps multiple;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void filterDisabledIfNoCorsPathsDefined() {
        when(multiple.getPaths()).thenReturn(new ArrayList<>());

        FilterRegistrationBean corsFilterRegistration = testee.corsFilterRegistration();

        assertFalse(corsFilterRegistration.isEnabled());
    }
}