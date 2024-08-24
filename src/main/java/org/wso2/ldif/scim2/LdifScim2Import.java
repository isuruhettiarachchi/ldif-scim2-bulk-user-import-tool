package org.wso2.ldif.scim2;

import org.json.simple.JSONObject;
import org.wso2.ldif.scim2.utils.Utils;
import org.wso2.ldif.scim2.exceptons.BulkImportException;
import org.wso2.ldif.scim2.handlers.ImportHandler;

import java.util.Map;

import static org.wso2.ldif.scim2.utils.JsonUtils.getFilteredJsonObject;

public class LdifScim2Import {

    public static void main(String[] args) {

        String accessToken;

        if (args.length < 1) {
            System.out.println("Invalid arguments! Please provide a valid access token.");
            return;
        }
        accessToken = args[0];

        try {

            JSONObject schema = Utils.getAttributeSchema();
            JSONObject mappings = Utils.getInternalAttributeMapping();


            Map<String, String> userAttributeMappings = Utils.getAttributeMappingWithSysAttributes();

            JSONObject filteredSchemaObject = getFilteredJsonObject(schema, mappings, userAttributeMappings);
            ImportHandler.importUsers(filteredSchemaObject,userAttributeMappings, mappings, accessToken);

        } catch (BulkImportException e) {
            e.printStackTrace();
        }

    }

}
