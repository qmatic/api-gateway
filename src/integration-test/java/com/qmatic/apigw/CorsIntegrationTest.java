package com.qmatic.apigw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.Filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertNotNull;

@SpringBootTest
@Import(CorsTestSpringConfig.class)
@ActiveProfiles("cors")
public class CorsIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private FilterRegistrationBean[] filterRegistrationBeans;

    private MockMvc mockMvc;

    @BeforeMethod
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(ctx);
        addCorsFilterToMockMvc(builder);
        mockMvc = builder.build();
    }

    private void addCorsFilterToMockMvc(DefaultMockMvcBuilder builder) {
        assertNotNull(filterRegistrationBeans);
        Filter corsFilter = null;
        for (FilterRegistrationBean filterRegistrationBean : filterRegistrationBeans) {
            Filter filter = filterRegistrationBean.getFilter();
            if (filter instanceof CorsFilter) {
                corsFilter = filterRegistrationBean.getFilter();
            }
        }
        assertNotNull(corsFilter, "CORS filter should have been configured via " + CorsSpringConfig.class);
        builder.addFilters(corsFilter);
    }

    @Test
    public void validCorsPath() throws Exception {
        MockHttpServletRequestBuilder req = get("/foo").header(HttpHeaders.ORIGIN, "http://foo.com");
        mockMvc.perform(req)
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://foo.com"));
    }

    @Test
    public void forbiddenAccessToCorsPath() throws Exception {
        MockHttpServletRequestBuilder req = get("/foo").header(HttpHeaders.ORIGIN, "http://SOME_OTHER_ORIGIN.com");
        mockMvc.perform(req)
                .andExpect(status().isForbidden());
    }

    @Test
    public void nonCorsPath() throws Exception {
        MockHttpServletRequestBuilder req = get("/bar");
        mockMvc.perform(req)
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

}