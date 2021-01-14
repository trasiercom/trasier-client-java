package com.trasier.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public final class ProjectUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUtils.class);

    private static String projectVersion = "unknown";

    static {
        try {
            Properties properties = new Properties();
            properties.load(ProjectUtils.class.getClassLoader().getResourceAsStream("project.properties"));
            projectVersion = properties.getProperty("version");
        } catch(Exception e) {
            LOGGER.info("Could not read trasier's client version ", e);
        }
    }

    public static String getProjectVersion() {
        return projectVersion;
    }
}
