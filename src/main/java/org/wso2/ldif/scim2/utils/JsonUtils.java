package org.wso2.ldif.scim2.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.ldif.scim2.constants.Constants;
import org.wso2.ldif.scim2.exceptons.BulkImportException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.wso2.ldif.scim2.constants.Constants.*;

public class JsonUtils {

    public static JSONObject getFilteredJsonObject(JSONObject schema, JSONObject mappings,
                                                   Map<String, String> userAttributeMappings) {

        JSONObject filteredSchemaJSON = new JSONObject();

        for (Object key : mappings.keySet()) {
            Map<String, String> categoryAttributeMappingJSON = Utils.parseJSONToMap(
                    (JSONObject) mappings.get(key));
            List<String> typesList = Utils.getUserDefinedListTypes(categoryAttributeMappingJSON, userAttributeMappings);

            JSONObject categorySchemaJSON = getFilteredAttributeJSON((JSONObject) schema.get(key),
                    "", userAttributeMappings, Utils.getInvertedMap(categoryAttributeMappingJSON), typesList);

            if (MapUtils.isEmpty(categorySchemaJSON)) {
                continue;
            }

            /*
             * If the category is core, add all the attributes to the filtered schema JSON as
             * first class attributes. Otherwise, add the sub schema JSON with the category.
             */
            if (CORE_ATTRIBUTE_KEY.equals(key)) {
                filteredSchemaJSON.putAll(categorySchemaJSON);
            } else {
                filteredSchemaJSON.put(key, categorySchemaJSON);
            }
        }

        return filteredSchemaJSON;
    }

    public static String getUserJSONString(JSONObject schemaJSON, Map<String, String> userAttributeValuesMap,
                                           String userStore, Map<String, String> attributeMapping,
                                           Map<String, String> internalAttributeMapping, boolean removeAlgFromPassword,
                                           String passwordAlg) throws BulkImportException {

        // Create a copy of the schema JSON to avoid modifying the original schema.
        JSONObject user = new JSONObject(schemaJSON);

        // Process password
        String password = userAttributeValuesMap.get(attributeMapping.get(Constants.Attributes.PASSWORD));
        if (removeAlgFromPassword) {
            password = password.replace(passwordAlg, "");
        }
        user.put(Constants.Attributes.PASSWORD, password);

        // Set the primary email.
        JSONArray emails = new JSONArray();
        if (schemaJSON.containsKey(Constants.Attributes.EMAILS)) {
            emails = (JSONArray) schemaJSON.get(Constants.Attributes.EMAILS);
        }

        JSONObject email = new JSONObject();
        if (StringUtils.isNotBlank(attributeMapping.get(Constants.Attributes.PRIMARY_EMAIL))) {
            email.put(Constants.Attributes.PRIMARY, true);
            email.put("value", userAttributeValuesMap.get(attributeMapping.get(Constants.Attributes.PRIMARY_EMAIL)));
            emails.add(email);
            user.put(Constants.Attributes.EMAILS, emails);
        }

        String userJSONString = user.toJSONString();

        // Iterate through attribute mapping.
        for (Map.Entry<String, String> entry : attributeMapping.entrySet()) {

            String attribute = entry.getKey();
            if (Constants.Attributes.SYSTEM_ATTRIBUTES.contains(attribute)) {
                continue;
            }

            String userFileKey = entry.getValue();
            String internalKey = internalAttributeMapping.get(attribute);
            String attributeValue = userAttributeValuesMap.get(userFileKey);

            if (Constants.Attributes.USERNAME.equals(attribute)) {
                attributeValue = userStore + USER_STORE_SEPARATOR + attributeValue;
            }

            if (attributeValue == null) {
                attributeValue = "";
            }
            userJSONString = userJSONString.replace(Utils.getKeyPathPlaceholder(internalKey), attributeValue);
        }

        return userJSONString;
    }

    private static JSONObject getFilteredAttributeJSON(JSONObject schema, String keyPath,
                                                       Map<String, String> userAttributeMapping,
                                                       Map<String, String> internalAttributeMappingJSON,
                                                       List<String> typesList) {

        JSONObject filteredJSON = new JSONObject();

        for (Object key : schema.keySet()) {
            String keyString = (String) key;
            Object value = schema.get(keyString);
            String keyPathString;

            // Construct the key path string by appending the current key.
            if (StringUtils.isNotBlank(keyPath)) {
                keyPathString = keyPath + SCHEMA_FILE_OBJECT_SEPARATOR + keyString;
            } else {
                keyPathString = keyString;
            }

            if (value instanceof JSONObject) {
                // If the value is a JSON object, recursively filter the object.
                JSONObject subFilteredJSON = getFilteredAttributeJSON((JSONObject) value, keyPathString,
                        userAttributeMapping, internalAttributeMappingJSON, typesList);
                if (MapUtils.isNotEmpty(subFilteredJSON)) {
                    filteredJSON.put(keyString, subFilteredJSON);
                }
            } else if (value instanceof JSONArray) {
                /*
                 * If the value is a JSON array, iterate through each element (object) and filter.
                 * It is assumed that each list element is an object and contains a type attribute.
                 */
                JSONArray filteredJSONArray = new JSONArray();

                for (Object object : (JSONArray) value) {
                    String arrayKeyPathString = keyPathString;

                    // Check if the object contains a type attribute. If not ignore the object.
                    if (((JSONObject) object).containsKey(TYPE_KEY)) {
                        String type = arrayKeyPathString + SCHEMA_FILE_LIST_SEPARATOR
                                + ((JSONObject) object).get(TYPE_KEY);

                        // Check if type is in the user defined attribute list. If not that means it is not configured.
                        if (typesList.contains(type)) {
                            arrayKeyPathString = type;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }

                    JSONObject subFilteredJSON = getFilteredAttributeJSON((JSONObject) object, arrayKeyPathString,
                            userAttributeMapping, internalAttributeMappingJSON, typesList);
                    if (MapUtils.isNotEmpty(subFilteredJSON)) {
                        filteredJSONArray.add(subFilteredJSON);
                    }
                }

                // If the filtered array is not empty, add it to the filtered JSON.
                if (CollectionUtils.isNotEmpty(filteredJSONArray)) {
                    filteredJSON.put(keyString, filteredJSONArray);
                }
            } else if (value instanceof String) {
                /*
                 * If the value is a string, first check if it is a type attribute.
                 * If it is a type attribute, check if it is in the user defined attribute list and if so add
                 * the type element with its value to the filtered JSON.
                 * If it is not a type attribute, check if it is in the user defined attribute list and if so add
                 * the element with the schema placeholder to the filtered JSON.
                 */
                if (TYPE_KEY.equals(keyString)) {
                    String type = Utils.getTypeSubString(keyPathString);
                    if (typesList.contains(type)) {
                        filteredJSON.put(keyString, value);
                    }
                } else {
                    if (StringUtils.isNotBlank(keyPath)) {
                        keyPathString = keyPath + SCHEMA_FILE_OBJECT_SEPARATOR + keyString;
                    } else {
                        keyPathString = keyString;
                    }

                    String internalKey = internalAttributeMappingJSON.get(keyPathString);
                    if (userAttributeMapping.containsKey(internalKey)) {
                        filteredJSON.put(keyString, Utils.getKeyPathPlaceholder(keyPathString));
                    }
                }
            }
        }

        return filteredJSON;
    }

    public static JSONObject getParsedJSON(String filePath) throws BulkImportException {

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            throw new BulkImportException("Error while parsing the json file", e);
        }

        return jsonObject;
    }
}
