package com.qmatic.apigw.properties;

import java.io.File;

public class DefaultConfigurationFileLocator implements ConfigurationFileLocator {
    static final String OS_NAME_LINUX = "linux";
    static final String OS_NAME_WINDOWS = "windows";
    private static final String DEFAULT_PATH_LINUX = "/etc/apigw.d";
    static final String DEFAULT_PATH_WINDOWS = "c:\\qmatic\\apigw.conf";
    static final File DEFAULT_FILE_LINUX = new File(DEFAULT_PATH_LINUX);
    static final File DEFAULT_FILE_WINDOWS = new File(DEFAULT_PATH_WINDOWS);
    static final String ADDITIONAL_CONFIG_PATH_PROPERTY = "api.gw.additional.config.path";
    static final String ADDITIONAL_CONFIG_PATH_ENV = "API_GW_ADDITIONAL_CONFIG_PATH";

    public File getAdditionalConfigurationFileDirectory() {
        if (System.getenv(ADDITIONAL_CONFIG_PATH_ENV) != null) {
            return new File(System.getenv(ADDITIONAL_CONFIG_PATH_ENV));
        }
        if (System.getProperty(ADDITIONAL_CONFIG_PATH_PROPERTY) != null) {
            return new File(System.getProperty(ADDITIONAL_CONFIG_PATH_PROPERTY));
        }
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains(OS_NAME_WINDOWS)) {
            return DEFAULT_FILE_WINDOWS;
        }
        return DEFAULT_FILE_LINUX;
    }
}