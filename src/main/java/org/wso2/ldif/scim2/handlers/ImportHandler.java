package org.wso2.ldif.scim2.handlers;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFReader;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.ldif.scim2.constants.Constants;
import org.wso2.ldif.scim2.exceptons.BulkImportException;
import org.wso2.ldif.scim2.utils.ConfigurationsUtils;
import org.wso2.ldif.scim2.utils.JsonUtils;
import org.wso2.ldif.scim2.utils.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ImportHandler {

    public static void importUsers(JSONObject filteredSchema,
                                   Map<String, String> userAttributeMappings, JSONObject internalMappings,
                                   String accessToken)
            throws BulkImportException {

        LDIFReader ldifReader;

        HashMap<String, String> summary = new HashMap<>();

        boolean continueOnError = ConfigurationsUtils.getInstance().getBooleanConfig(Constants.CONTINUE_ON_ERROR);
        String host = ConfigurationsUtils.getInstance().getConfig(Constants.HOST);

        String ldifFilePath = Paths.get(ConfigurationsUtils.getInstance().getConfig(Constants.LDIF_FILE_PATH)).toString();
        String userStore = ConfigurationsUtils.getInstance().getConfig(Constants.USER_STORE);
        boolean removeAlgFromPassword = ConfigurationsUtils.getInstance().getBooleanConfig(Constants.REMOVE_ALG_FROM_PASSWORD);
        String passwordAlg = ConfigurationsUtils.getInstance().getConfig(Constants.PASSWORD_ALG);
        String LDAP_USER_OBJECT = ConfigurationsUtils.getInstance().getConfig(Constants.LDAP_USER_OBJECT);

        try (FileInputStream inputStream = new FileInputStream(ldifFilePath)) {
            ldifReader = new LDIFReader(inputStream);
            Entry entry;
            while ((entry = ldifReader.readEntry()) != null) {

                if (entry.hasObjectClass(LDAP_USER_OBJECT)) {
                    Map<String, String> attributesMap = new HashMap<>();
                    for (Attribute attribute : entry.getAttributes()) {
                        attributesMap.put(attribute.getName(), attribute.getValue());
                    }

                    String jsonObjString = JsonUtils.getUserJSONString(filteredSchema, attributesMap, userStore,
                            userAttributeMappings, Utils.getInternalAttributeMap(internalMappings), removeAlgFromPassword,
                            passwordAlg);

                    ApiHandler apiHandler = new ApiHandler();

                    HttpResponse response = apiHandler.addUser(jsonObjString, host, accessToken);

                    boolean continueFlow = handleAPIResponse(response, entry.getDN(), summary, continueOnError);
                    if (!continueFlow) {
                        break;
                    }
                    System.out.println("Processed DN: " + entry.getDN());
                }
            }
        } catch (IOException e) {
            throw new BulkImportException("Error while reading the ldif file", e);
        } catch (LDIFException e) {
            throw new RuntimeException(e);
        }

        if (MapUtils.isNotEmpty(summary)) {
            System.out.println("User import completed");
            Utils.createImportSummary(summary);
        }
    }

    private static boolean handleAPIResponse(HttpResponse response, String DN, HashMap<String, String> summary,
                                             boolean continueOnError) throws BulkImportException {

        int statusCode = response.getStatusLine().getStatusCode();
        String status = "";
        String stringResponse;

        try {
            stringResponse = EntityUtils.toString(response.getEntity(), Constants.CHARSET);
            if (StringUtils.isNotBlank(stringResponse)) {
                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(stringResponse);
                if (responseJson.containsKey(Constants.RESPONSE_DETAIL)) {
                    status = (String) responseJson.get(Constants.RESPONSE_DETAIL);
                }
            }
        } catch (IOException | ParseException e) {
            throw new BulkImportException("Error occurred while importing users.", e);
        }

        if (HttpStatus.SC_CREATED == statusCode) {
            summary.put(DN, "Successfully created");
        } else if (HttpStatus.SC_CONFLICT == statusCode) {
            if (StringUtils.isBlank(status)) {
                status = "Already exists in the organization.";
            }
            summary.put(DN, status);
            return continueOnError;
        } else if (HttpStatus.SC_BAD_REQUEST == statusCode) {
            if (StringUtils.isBlank(status)) {
                status = "Invalid values in the user attributes.";
            }
            summary.put(DN, status);
            return continueOnError;
        } else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == statusCode) {
            if (StringUtils.isBlank(status)) {
                status = "An internal server error occurred.";
            }
            summary.put(DN, status);
            return continueOnError;
        } else if (HttpStatus.SC_UNAUTHORIZED == statusCode) {
            return false;
        } else {
            summary.put(DN, "Unexpected error while importing users.");
            return continueOnError;
        }

        return true;
    }
}
