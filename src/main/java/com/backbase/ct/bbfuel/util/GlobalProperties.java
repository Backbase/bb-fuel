package com.backbase.ct.bbfuel.util;

import static com.backbase.ct.bbfuel.data.CommonConstants.ADDITIONAL_PROPERTIES_PATH;
import static com.backbase.ct.bbfuel.data.CommonConstants.CUSTOM_PROPERTIES_PATH;
import static com.backbase.ct.bbfuel.data.CommonConstants.ENVIRONMENT_PROPERTIES_FILE_NAME;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTIES_FILE_NAME;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

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

        addAdditionalPropertiesConfiguration();

        String effectivePropertiesPath = configuration.containsKey(CUSTOM_PROPERTIES_PATH) ?
            configuration.getString(CUSTOM_PROPERTIES_PATH) :
            PROPERTIES_FILE_NAME;

        try {
            configuration.addConfiguration(new PropertiesConfiguration(effectivePropertiesPath));
            configuration.addConfiguration(new PropertiesConfiguration(ENVIRONMENT_PROPERTIES_FILE_NAME));
        } catch (ConfigurationException ignored) {
        }
    }

    private void addAdditionalPropertiesConfiguration() {
        if (configuration.containsKey(ADDITIONAL_PROPERTIES_PATH)) {
            String additionalPropertiesPath = configuration.getString(ADDITIONAL_PROPERTIES_PATH);
            try {
                configuration.addConfiguration(new PropertiesConfiguration(additionalPropertiesPath));
            } catch (ConfigurationException ignored) {
            }
        }
    }

    public synchronized static GlobalProperties getInstance() {
        if (instance == null) {
            instance = new GlobalProperties();
        }
        return instance;
    }

    public boolean containsKey(String key) {
        return configuration.containsKey(key);
    }

    public String getString(String key) {
        return configuration.getString(key);
    }

    public String[] getStringArray(String key) {
        return getStringArray(key, false);
    }

    /**
     * Gets string array by key from a property with {@link AbstractConfiguration#getDefaultListDelimiter()} separated
     * value.
     *
     * It is possible to control property source with <i>allPropertiesCombined</i> param.
     * If false - string array is got from a single most prioritized source (e.g. {@link SystemConfiguration}).
     * If true - string array is merged from all configurations.
     *
     * @param key property key
     * @param allPropertiesCombined controls whether values should be taken from single or all properties sources
     * @return string array of property values
     */
    public String[] getStringArray(String key, boolean allPropertiesCombined) {
        if (!allPropertiesCombined) {
            return configuration.getStringArray(key);
        }

        int numberOfConfigurations = configuration.getNumberOfConfigurations();
        return IntStream.range(0, numberOfConfigurations)
            .mapToObj(configuration::getConfiguration)
            .map(internalConfiguration -> internalConfiguration.getStringArray(key))
            .flatMap(Arrays::stream)
            .distinct()
            .toArray(String[]::new);
    }

    public List<String> getList(String key) {
        return getList(key, false);
    }

    public List<String> getList(String key, boolean allPropertiesCombined) {
        return Arrays.asList(getStringArray(key, allPropertiesCombined));
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
