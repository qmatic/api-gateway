package com.qmatic.apigw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

@Component
public class SslCertificateManager {

    @Value("${orchestra.disableSslCertificateChecks:false}")
    private void setDisableSslCertificateChecks (boolean disableSslCertificateChecks) {
        if (disableSslCertificateChecks) {
            installAllTrustingTrustManager();
        }
    }

    @Value("${orchestra.trustStorePass:changeit}")
    private void setSslTrustStorePassword(String trustStorePassword) {
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    @Value("${orchestra.trustStore:conf/truststore.jks}")
    private void setSslTrustStore (String trustStore) {
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("app.home") + File.separator + trustStore);
    }

    private static final Logger log = LoggerFactory.getLogger(SslCertificateManager.class);

    private static TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                }
            }
    };

    private static void installAllTrustingTrustManager() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
            log.error("Error disabling ssl certificate checks, ", e);
        }
    }
}
