package com.qmatic.apigw.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by davtol on 2015-12-15.
 */

@Data
@ConfigurationProperties("orchestra")
public class OrchestraProperties {

    private Map<String, UserCredentials> api_tokens = new LinkedHashMap<>();

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
}
