package com.qmatic.apigw.properties;

import java.io.File;

public interface ConfigurationFileLocator {
    File getSystemConfigurationFileDirectory();
    File getApplicationConfigurationFileDirectory();
}
