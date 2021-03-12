package com.qmatic.apigw.properties;

import java.io.File;

public class TestConfigurationFileLocator implements ConfigurationFileLocator {
    @Override
    public File getAdditionalConfigurationFileDirectory() {
        String resource = Thread.currentThread().getContextClassLoader().getResource("properties/10-override-host-name.yml").getFile();
        File resourceFile = new File(resource);
        return resourceFile.getParentFile();
    }
}
