package com.qmatic.apigw.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "http.cors", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class CorsPathsMultipleProps {

    private final List<CorsPath> paths = new ArrayList<>();

    public List<CorsPath> getPaths() {
        return paths;
    }

    public void add(CorsPath corsPath) {
        paths.add(corsPath);
    }

    @Override
    public String toString() {
        return "CorsPaths [paths=" + paths + "]";
    }

}