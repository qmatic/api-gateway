package com.qmatic.apigw;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@Configuration
public class CorsTestSpringConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return mock(CacheManager.class);
    }
}