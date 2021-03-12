package com.qmatic.apigw.properties;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdditionalApplicationConfigFilesPostProcessor implements EnvironmentPostProcessor {
    private final Log log;
    public static final String YML = ".yml";
    public static final String YAML = ".yaml";
    public static final String ADDITIONAL_CONFIG_DIRECTORY_LOCATOR = "qmatic.api.gateway.additional.config.dir.locator";

    public AdditionalApplicationConfigFilesPostProcessor(DeferredLogFactory deferredLogFactory) {
        this.log = deferredLogFactory.getLog(AdditionalApplicationConfigFilesPostProcessor.class);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        ConfigurationFileLocator configurationFileLocator = getConfigurationFileLocator(environment);
        List<String> additionalConfigurationFiles = getAdditionalConfigurationFiles(configurationFileLocator);
        if (!additionalConfigurationFiles.isEmpty()) {
            String additionalLocations = constructFilePaths(additionalConfigurationFiles);
            System.setProperty("spring.config.additional-location", additionalLocations);
        }
    }

    private ConfigurationFileLocator getConfigurationFileLocator(ConfigurableEnvironment environment) {
        if (environment.containsProperty(ADDITIONAL_CONFIG_DIRECTORY_LOCATOR)) {
            String configurationFileLocatorClass = environment.getProperty(ADDITIONAL_CONFIG_DIRECTORY_LOCATOR);
            try {
                Class<?> aClass = Class.forName(configurationFileLocatorClass);
                return (ConfigurationFileLocator) aClass.newInstance();
            } catch (ClassNotFoundException|IllegalAccessException|InstantiationException|ClassCastException e) {
                log.error("Specified class for qmatic.api.gateway.additional.config.dir.locator cannot be loaded, default will be used. Exception: {}", e);
            }
        }
        return new DefaultConfigurationFileLocator();
    }

    private List<String> getAdditionalConfigurationFiles(ConfigurationFileLocator configurationFileLocator) {
        List<String> additionalConfigurationFiles = new ArrayList<>();
        File configDirectory = configurationFileLocator.getAdditionalConfigurationFileDirectory();
        if (configDirectory != null && configDirectory.exists() && configDirectory.isDirectory()) {
            log.info("Searching directory [" + configDirectory.getAbsolutePath() + "] to find suitable application configuration files.");
            additionalConfigurationFiles.addAll(findMatchingFiles(configDirectory));
        }
        Collections.sort(additionalConfigurationFiles);
        return additionalConfigurationFiles;
    }

    private List<String> findMatchingFiles(File directory) {
        List<String> matchingFiles = new ArrayList<>();
        File[] filesInDirectory = directory.listFiles();
        if (filesInDirectory == null) {
            return matchingFiles;
        }
        for (File file : filesInDirectory) {
            String fileName = file.getName().toLowerCase();
            if (isMatchingFile(file, fileName)) {
                matchingFiles.add(file.getAbsolutePath());
            }
        }
        return matchingFiles;
    }

    private boolean isMatchingFile(File file, String fileName) {
        return file.exists() && file.isFile() &&
                (fileName.endsWith(YML) || fileName.endsWith(YAML));
    }

    private String constructFilePaths(List<String> additionalConfigurationFiles) {
        StringBuilder additionalLocations = new StringBuilder();
        for (String file : additionalConfigurationFiles) {
            if (additionalLocations.length() > 0) {
                additionalLocations.append(",");
            }
            additionalLocations.append("optional:");
            additionalLocations.append(file);
            log.info("Appending file [" + file + "] to application configuration.");
        }
        return additionalLocations.toString();
    }
}