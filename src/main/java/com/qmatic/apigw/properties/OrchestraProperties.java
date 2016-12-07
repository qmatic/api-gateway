package com.qmatic.apigw.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by davtol on 2015-12-15.
 */

@Data
@ConfigurationProperties("orchestra")
public class OrchestraProperties {

    private Map<String, UserCredentials> api_tokens = new LinkedHashMap<>();
    private Map<String, VisitIdParameter> checksumRoutes = new LinkedHashMap<>();

    //public enum VisitIdParameterType{PATH_PARAMETER,MATRIX_PARAMETER,QUERY_PARAMETER};

    @PostConstruct
    public void init() {
        for (Map.Entry<String, UserCredentials> entry : this.api_tokens.entrySet()) {
            UserCredentials value = entry.getValue();
            value.setPasswd(new String(Base64.decodeBase64(value.getPasswd()), StandardCharsets.UTF_8));
        }
    }

    public UserCredentials getCredentials(String apiToken) {
        return api_tokens.get(apiToken) != null ? api_tokens.get(apiToken) : null;
    }


    public Set<String> getRoutes() {
        return checksumRoutes.keySet();
    }

    public VisitIdParameter getVisitIdParameter(String route) {
        return checksumRoutes.get(route) != null ? checksumRoutes.get(route) : null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserCredentials {
    // Please note: the innerclass representing list items must be declared static

        private String user;
        private String passwd;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VisitIdParameter {
        private String parameter;

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }
    }
}
