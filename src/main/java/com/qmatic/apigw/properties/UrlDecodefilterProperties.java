package com.qmatic.apigw.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Data
@ConfigurationProperties("urlDecodeFilter")
public class UrlDecodeFilterProperties {

    private static final Logger log = LoggerFactory.getLogger(UrlDecodeFilterProperties.class);

    private String enabled;
    private boolean isEnabled = false;
    private String encryptionKey;
    private SecretKeySpec secretKeySpec;

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isEnabled(){
        return isEnabled;
    }

    public SecretKeySpec getSecretKeySpec() {
        return secretKeySpec;
    }

    @PostConstruct
    public void init() {
        if(getEnabled() == null){
            return;
        }
        constructSecretKeySpec();
        try {
            isEnabled = Boolean.parseBoolean(getEnabled());
        }catch (Exception e){
            log.warn("Failed to parse value for urlDecodeFilter, enabled. Defaulting to false. " + e.getMessage());
        }
    }

    private void constructSecretKeySpec() {
        try {
            String encryptionKey = getEncryptionKey();
            byte[] key = (encryptionKey).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            // the key for encryption
            secretKeySpec = new SecretKeySpec(key, "AES");
       }catch(UnsupportedEncodingException e){
            log.error("Failed to get the value for the encryptionKey. " + e.getMessage());
        }catch(NoSuchAlgorithmException e){
            log.error("Failed to instatiate MessageDigester. " + e.getMessage());
        }
    }

}
