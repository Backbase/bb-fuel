package com.backbase.ct.bbfuel.util;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.backbase.ct.bbfuel.data.CommonConstants.CUSTOM_PROPERTIES_PATH;
import static com.backbase.ct.bbfuel.data.CommonConstants.ENVIRONMENT_PROPERTIES_FILE_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTIES_FILE_NAME;

/**
 * This is the single thread-safe source of retrieving properties.
 * <p>
 * Usage example:
 * <pre>
 * GlobalProperties globalProperties = GlobalProperties.getInstance();
 * String aUrl = globalProperties.getString("url");
 * </pre>
 * <p>
 * note: GlobalProperties is a thread-safe singleton - it maintains a single instance within execution
 * <p>
 * You can also use it as a property of a class;
 * <pre>
 *  private GlobalProperties globalProperties = GlobalProperties.getInstance();
 * </pre>
 */
public class GlobalProperties {

    private static GlobalProperties instance;
    private CompositeConfiguration configuration;

    private GlobalProperties() {
        configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());
        configuration.addConfiguration(new EnvironmentConfiguration());

        String effectivePropertiesPath = configuration.containsKey(CUSTOM_PROPERTIES_PATH) ?
            configuration.getString(CUSTOM_PROPERTIES_PATH) :
            PROPERTIES_FILE_NAME;

        try {
            configuration.addConfiguration(new PropertiesConfiguration(effectivePropertiesPath));
            configuration.addConfiguration(new PropertiesConfiguration(ENVIRONMENT_PROPERTIES_FILE_NAME));
        } catch (ConfigurationException ignored) {
        }
    }

    public synchronized static GlobalProperties getInstance() {
        if (instance == null) {
            instance = new GlobalProperties();
        }
        return instance;
    }

    public String getString(String key) {
        return configuration.getString(key);
    }

    public List<String> getList(String key) {
        String propertyValue = getString(key);
        return Arrays.asList(propertyValue.split(","))
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
    }

    public int getInt(String key) {
        return configuration.getInt(key);
    }

    public long getLong(String key) {
        return configuration.getLong(key);
    }

    public boolean getBoolean(String key) {
        return configuration.getBoolean(key);
    }

    public synchronized String syncGet(String key) {
        return configuration.getString(key);
    }

    public <T> void setProperty(String key, T value) {
        configuration.setProperty(key, value);
    }

}
