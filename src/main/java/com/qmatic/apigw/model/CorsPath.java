package com.qmatic.apigw.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

import static com.qmatic.apigw.util.Assert.isFalse;
import static com.qmatic.apigw.util.Assert.notBlank;

public class CorsPath {
    private static final String COMMA = ",";
    private String path;
    private Boolean allowCredentials;
    private Long maxAge;
    private final Collection<String> allowedOrigins = new LinkedHashSet<>();
    private final Collection<String> allowedHeaders = new LinkedHashSet<>();
    private final Collection<String> exposedHeaders = new LinkedHashSet<>();
    private final Collection<String> allowedMethods = new LinkedHashSet<>();

    public void validate() {
        notBlank(path, "Path must be set in CORS configuration. Example: path: '/**'");
        isFalse(allowedOrigins.isEmpty(), "Allowed origins is mandatory. Example: allowedOrigins: http://foo.com, http://bar.com");
    }

    public boolean isInitiated() {
        return path != null || !allowedOrigins.isEmpty();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Collection<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        notBlank(allowedOrigins,
                "Allowed origins is mandatory. Example: allowedOrigins: http://foo.com, http://bar.com");
        appendCommaSeparated(allowedOrigins, this.allowedOrigins);
    }

    public Collection<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        appendCommaSeparated(allowedHeaders, this.allowedHeaders);
    }

    public Collection<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(String exposedHeaders) {
        appendCommaSeparated(exposedHeaders, this.exposedHeaders);
    }

    public Collection<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(String allowedMethods) {
        appendCommaSeparated(allowedMethods, this.allowedMethods);
    }

    private void appendCommaSeparated(String commaSeparated, Collection<String> appendTo) {
        if (StringUtils.isNotBlank(commaSeparated)) {
            String[] vals = commaSeparated.split(COMMA);
            for (String val : vals) {
                appendTo.add(val.trim());
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + ((path == null) ? 0 : path.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CorsPath other = (CorsPath) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CorsPath [path=" + path + ", allowCredentials=" + allowCredentials + ", maxAge=" + maxAge
                + ", allowedOrigins=" + allowedOrigins + ", allowedHeaders=" + allowedHeaders + ", exposedHeaders="
                + exposedHeaders + ", allowedMethods=" + allowedMethods + "]";
    }

}