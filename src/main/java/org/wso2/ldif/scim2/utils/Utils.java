package org.wso2.ldif.scim2.utils;

import com.unboundid.ldif.LDIFReader;
import org.json.simple.JSONObject;
import org.wso2.ldif.scim2.exceptons.BulkImportException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.ldif.scim2.constants.Constants.*;

public class Utils {

    public static String getKeyPathPlaceholder(String keyPath) {

        return "__" + keyPath + "__";
    }

    public static Map<String, String> parseJSONToMap(JSONObject jsonObject) {

        Map<String, String> map = new HashMap<>();
        Set<String> keys = jsonObject.keySet();

        for (String key : keys) {
            map.put(key, jsonObject.get(key).toString());
        }

        return map;
    }

    public static List<String> getUserDefinedListTypes(Map<String, String> internalAttributeMappingJSON,
                                                       Map<String, String> userAttributeMapping) {

        Map<String, String> completeListAttributes = internalAttributeMappingJSON.entrySet().stream()
                .filter(entry -> entry.getValue().contains("#"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return userAttributeMapping.keySet().stream()
                .filter(completeListAttributes::containsKey)
                .map(entry -> getTypeSubString(completeListAttributes.get(entry)))
                .collect(Collectors.toList());
    }

    public static String getTypeSubString(String keyPath) {

        return keyPath.substring(0, keyPath.contains(".")
                ? keyPath.lastIndexOf(".")
                : keyPath.length());
    }

    public static Map<String, String> getInvertedMap(Map<String, String> map) {

        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public static Map<String, String> getInternalAttributeMap(JSONObject internalAttributeMappingJSON) {

        Map<String, String> outputMap = new HashMap<>();

        for (Object key : internalAttributeMappingJSON.keySet()) {
            Object value = internalAttributeMappingJSON.get(key);
            if (value instanceof String) {
                outputMap.put((String) key, (String) value);
            } else if (value instanceof JSONObject) {
                outputMap.putAll(getInternalAttributeMap((JSONObject) value));
            }
        }

        return outputMap;
    }

    public static JSONObject getInternalAttributeMapping() throws BulkImportException {

        String filePath = Paths.get(System.getProperty(USER_DIRECTORY_SYSTEM_VARIABLE), CONFIG_DIRECTORY,
                ATTRIBUTE_MAPPING_FILE_NAME).toString();
        return JsonUtils.getParsedJSON(filePath);
    }

    public static JSONObject getAttributeSchema() throws BulkImportException {

        String filePath = Paths.get(System.getProperty(USER_DIRECTORY_SYSTEM_VARIABLE), CONFIG_DIRECTORY,
                ATTRIBUTE_SCHEMA_FILE_NAME).toString();
        return JsonUtils.getParsedJSON(filePath);
    }

    public static Map<String, String> getAttributeMappingWithSysAttributes() throws BulkImportException {

        Map<String, Object> attributeMapping = ConfigurationsUtils.getInstance()
                .getMapOfConfigs(ATTRIBUTE_MAPPING_KEY);

        return attributeMapping.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }

    public static LDIFReader getLdifFile() throws BulkImportException {
        LDIFReader ldifReader;

        String filePath = Paths.get(ConfigurationsUtils.getInstance().getConfig(LDIF_FILE_PATH)).toString();

        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            ldifReader = new LDIFReader(inputStream);
        } catch (IOException e) {
            throw new BulkImportException("Error while reading the ldif file", e);
        }

        return ldifReader;
    }

    public static void createImportSummary(HashMap<String, String> responseSummary) {

        Path directoryPath = Paths.get(System.getProperty(USER_DIRECTORY_SYSTEM_VARIABLE), OUTPUT_DIRECTORY);
        File outputFile = new File(Paths.get(directoryPath.toString(), IMPORT_SUMMARY_FILE_NAME).toString());

        // Delete the existing file.
        if (outputFile.exists() && !outputFile.isDirectory()) {
            boolean fileDeletionState = outputFile.delete();
            if (!fileDeletionState) {
                return;
            }
        }

        // Create the directory if not exists.
        File directory = new File(directoryPath.toString());
        if (!directory.exists()) {
            boolean directoryCreationState = directory.mkdir();
            if (!directoryCreationState) {
                return;
            }
        }

        // Create the new file.
        try {
            boolean fileCreationState = outputFile.createNewFile();
            if (!fileCreationState) {
                return;
            }
        } catch (IOException e) {
            return;
        }

        // Write the summary to the file.
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (Map.Entry<String, String> entry : responseSummary.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + System.lineSeparator());
            }
            System.out.println("HashMap has been written to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
