package com.qmatic.apigw;

import com.qmatic.apigw.model.CorsPath;
import com.qmatic.apigw.model.CorsPathsMultipleProps;
import com.qmatic.apigw.model.CorsPathsSingleProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Setting up CORS for the gateway. The configuration is defined in
 * application.yml and read via property beans. The filter is only registered if
 * at least one CORS configuration is available. Configuration supports both
 * single path and multiple paths, therefore two different property beans.
 */
@Configuration
@EnableConfigurationProperties({CorsPathsSingleProps.class, CorsPathsMultipleProps.class})
public class CorsSpringConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CorsPathsSingleProps corsPathsSingle;

    @Autowired
    private CorsPathsMultipleProps corsPathsMultiple;

    @Bean
    public FilterRegistrationBean corsFilterRegistration() {
        FilterRegistrationBean frb = new FilterRegistrationBean();
        Collection<CorsPath> corsPaths = getCorsPaths();
        if (corsPaths.isEmpty()) {
            frb.setEnabled(false);
        }
        frb.setFilter(corsFilter(corsPaths));
        frb.setUrlPatterns(asList("/*"));
        return frb;
    }

    private CorsFilter corsFilter(Collection<CorsPath> corsPaths) {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        for (CorsPath corsPath : corsPaths) {
            corsPath.validate();

            CorsConfiguration config = new CorsConfiguration();

            config.setAllowCredentials(corsPath.getAllowCredentials());
            config.setMaxAge(corsPath.getMaxAge());

            config.setAllowedOrigins(toList(corsPath.getAllowedOrigins()));
            config.setAllowedHeaders(toList(corsPath.getAllowedHeaders()));
            config.setAllowedMethods(toList(corsPath.getAllowedMethods()));
            config.setExposedHeaders(toList(corsPath.getExposedHeaders()));

            source.registerCorsConfiguration(corsPath.getPath(), config);

            log.info("Registered CORS config: {}", corsPath);
        }
        return new CorsFilter(source);
    }

    private List<String> toList(Collection<String> col) {
        return new ArrayList<>(col);
    }

    private Collection<CorsPath> getCorsPaths() {
        if (corsPathsSingle.isInitiated()) {
            corsPathsMultiple.add(corsPathsSingle);
        }
        return corsPathsMultiple.getPaths();
    }
}