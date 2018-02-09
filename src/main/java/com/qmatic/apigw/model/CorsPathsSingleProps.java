package com.qmatic.apigw.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "http.cors.paths", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class CorsPathsSingleProps extends CorsPath {

}