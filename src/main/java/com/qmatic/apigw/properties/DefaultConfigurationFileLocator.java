package com.qmatic.apigw.properties;

import java.io.File;

public class DefaultConfigurationFileLocator implements ConfigurationFileLocator {
    static final String OS_NAME_LINUX = "linux";
    static final String OS_NAME_WINDOWS = "windows";
    private static final String DEFAULT_PATH_LINUX = "/etc/apigw.d";
    static final String DEFAULT_PATH_WINDOWS = "c:\\qmatic\\apigw.conf";
    static final File DEFAULT_FILE_LINUX = new File(DEFAULT_PATH_LINUX);
    static final File DEFAULT_FILE_WINDOWS = new File(DEFAULT_PATH_WINDOWS);
    static final String SYSTEM_CONFIG_PATH_PROPERTY = "api.gw.system.config.path";
    static final String SYSTEM_CONFIG_PATH_ENV = "API_GW_SYSTEM_CONFIG_PATH";
    static final String APPLICATION_CONFIG_PATH_PROPERTY = "api.gw.application.config.path";
    static final String APPLICATION_CONFIG_PATH_ENV = "API_GW_APPLICATION_CONFIG_PATH";

    public File getSystemConfigurationFileDirectory() {
        if (System.getenv(SYSTEM_CONFIG_PATH_ENV) != null) {
            return new File(System.getenv(SYSTEM_CONFIG_PATH_ENV));
        }
        if (System.getProperty(SYSTEM_CONFIG_PATH_PROPERTY) != null) {
            return new File(System.getProperty(SYSTEM_CONFIG_PATH_PROPERTY));
        }
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains(OS_NAME_WINDOWS)) {
            return DEFAULT_FILE_WINDOWS;
        }
        return DEFAULT_FILE_LINUX;
    }

    @Override
    public File getApplicationConfigurationFileDirectory() {
        if (System.getenv(APPLICATION_CONFIG_PATH_ENV) != null) {
            return new File(System.getenv(APPLICATION_CONFIG_PATH_ENV));
        }
        if (System.getProperty(APPLICATION_CONFIG_PATH_PROPERTY) != null) {
            return new File(System.getProperty(APPLICATION_CONFIG_PATH_PROPERTY));
        }
        return null;
    }
}