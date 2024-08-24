package org.wso2.ldif.scim2.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String LDIF_FILE_PATH = "system.ldif_file_path";
    public static final String CONTINUE_ON_ERROR = "system.continue_on_error";
    public static final String HOST = "server.host";

    // User store configs
    public static final String USER_STORE = "user_store.name";
    public static final String REMOVE_ALG_FROM_PASSWORD = "user_store.remove_alg_from_password";
    public static final String PASSWORD_ALG = "user_store.password_alg";

    public static final String LDAP_USER_OBJECT = "ldap.user_object";

    public static final String USER_STORE_SEPARATOR = "/";

    // SCIM object construct
    public static final String CORE_ATTRIBUTE_KEY = "core";
    public static final String SCHEMA_FILE_LIST_SEPARATOR = "#";
    public static final String SCHEMA_FILE_OBJECT_SEPARATOR = ".";
    public static final String TYPE_KEY = "type";

    // File related config
    public static final String CHARSET = "UTF-8";

    public static final String RESPONSE_DETAIL = "detail";
    public static final String PATH_SEPARATOR = "/";

    public static final String ATTRIBUTE_MAPPING_KEY = "attribute_mapping";

    public static final String USER_DIRECTORY_SYSTEM_VARIABLE = "user.dir";
    public static final String CONFIG_DIRECTORY = "config";
    public static final String CONFIGURATION_FILE_NAME = "configurations.toml";
    public static final String ATTRIBUTE_MAPPING_FILE_NAME = "internal-attribute-mappings.json";
    public static final String ATTRIBUTE_SCHEMA_FILE_NAME = "attribute-schema.json";
    public static final String OUTPUT_DIRECTORY = "output";
    public static final String IMPORT_SUMMARY_FILE_NAME = "bulk-import-summary.txt";

    public static class Attributes {

        public static final String USERNAME = "userName";
        public static final String PASSWORD = "password";
        public static final String PRIMARY = "primary";
        public static final String EMAILS = "emails";
        public static final String PRIMARY_EMAIL = "primaryEmail";

        public static final List<String> SYSTEM_ATTRIBUTES = new ArrayList<>(
                Arrays.asList("primaryEmail", "password"));
    }
}
