package org.wso2.ldif.scim2.utils;

import com.moandjiezana.toml.Toml;
import org.wso2.ldif.scim2.exceptons.BulkImportException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ldif.scim2.constants.Constants.*;

public class ConfigurationsUtils {

    private final Toml configuration;
    private static ConfigurationsUtils instance = null;

    private ConfigurationsUtils() throws BulkImportException {

        Path filePath = Paths.get(System.getProperty(USER_DIRECTORY_SYSTEM_VARIABLE), CONFIG_DIRECTORY,
                CONFIGURATION_FILE_NAME);
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath.toString()))) {
            configuration = new Toml().read(inputStream);
        } catch (IOException e) {
            throw new BulkImportException("configuration.toml file not found.", e);
        }
    }

    /**
     * Get an instance of the class.
     *
     * @return Instance of the ConfigurationsUtils.
     * @throws BulkImportException Error while getting the instance.
     */
    public static ConfigurationsUtils getInstance() throws BulkImportException {

        if (instance == null) {
            instance = new ConfigurationsUtils();
        }
        return instance;
    }

    /**
     * Get a string configuration.
     *
     * @param key Configuration key.
     * @return Configuration value.
     */
    public String getConfig(String key) {

        return this.configuration.getString(key);
    }

    /**
     * Get a boolean configuration.
     *
     * @param key Configuration key.
     * @return Configuration value.
     */
    public boolean getBooleanConfig(String key) {

        return this.configuration.getBoolean(key);
    }

    /**
     * Get an integer configuration.
     *
     * @param key Configuration key.
     * @return Configuration value.
     */
    public int getIntConfig(String key) {

        return this.configuration.getLong(key).intValue();
    }

    /**
     * Get a list configuration.
     *
     * @param key Configuration key.
     * @return List of configuration values.
     */
    public List<HashMap> getListOfConfigs(String key) {

        return this.configuration.getList(key);
    }

    /**
     * Get a map configuration.
     *
     * @param key Configuration key.
     * @return Map of configuration values.
     */
    public Map<String, Object> getMapOfConfigs(String key) {

        return this.configuration.getTable(key).toMap();
    }
}
