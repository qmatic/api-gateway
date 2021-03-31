package com.qmatic.apigw.properties;

import java.io.File;

public class TestConfigurationFileLocator implements ConfigurationFileLocator {
    @Override
    public File getSystemConfigurationFileDirectory() {
        String resource = Thread.currentThread().getContextClassLoader().getResource("system_properties/10-override-host-name.yml").getFile();
        File resourceFile = new File(resource);
        return resourceFile.getParentFile();
    }

    @Override
    public File getApplicationConfigurationFileDirectory() {
        String resource = Thread.currentThread().getContextClassLoader().getResource("application_properties/10-application-override-hystrix-threshold.yml").getFile();
        File resourceFile = new File(resource);
        return resourceFile.getParentFile();
    }
}
