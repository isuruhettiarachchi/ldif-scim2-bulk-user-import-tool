# WSO2 LDIF-SCIM2 Bulk User Import Tool

This guide provides instructions on configuring and using the Bulk User Import Tool to import users into an WSO2 IS.

## Prerequisites

1. Ensure Java 8 or higher is installed on your system.

2. Obtain an access token with the scope `internal_user_mgt_create`.

## Setting up the tool

### General configurations

1. Open the `org.wso2.ldif.scim-<version>/config/configurations.toml` file.

2. Provide the server configurations as follows under the `[server]` section:

    ```toml
    [server]
    host="localhost:9443"
    ```

    | Configuration  | Description                               | Sample value     |
    |----------------|-------------------------------------------|------------------|
    | `host`         | Host address of the WSO2 Identity Server. | `localhost:9443` |

3. Provide the user store configurations as follows under the `[user_store]` section

    ```toml
   [user_store]
    name="MYSQL"
    remove_alg_from_password=true
    password_alg="{SHA512}"
    ```

   | Configuration              | Description                                                                                                                  | Sample value |
   |----------------------------|------------------------------------------------------------------------------------------------------------------------------|--------------|
   | `name`                     | User store domain name.                                                                                                      | `SECONDARY`  |
   | `remove_alg_from_password` | If the algorithm is defined the password text and whether to remove it or not. e.g. {SHA256}fvJi2e79aTLY5an8t3ZbmA==         | `true`       |
   | `password_alg`             | String used in the password text to define the hashed algorithm. This part will be removed from the password when processing | `{SHA512}    |

4. Provide the ldap configurations as follows under `[ldap]` section:

    ```toml
    [ldap]
    user_object="InetOrgPerson"
    ```
  | Configuration  | Description                            | Sample value    |
  |----------------|----------------------------------------|-----------------|
  | `user_object`  | LDAP Object to defined to create users | `InetOrgPerson` |

5. Provide the system configurations as follows under the `[system]` section:

    ```toml
    [system]
    ldif_file_path="/Users/isuruhe/wso2/CCT/Staples-POC/export_users_SHA512.ldif"
    continue_on_error=true
    ```

    | Configuration                    | Description                                                                                                                                                                                | Sample value                   |
    |----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
    | `ldif_file_path`                 | Path to the input LDIF file. This file should contain the user details to be imported.                                                                                                     | `path/to/the/users/.ldif/file` |
    | `continue_on_error`              | Whether to continue importing rest of the users if an error occurs.                                                                                                                        | `true`                         |

### Configure user attributes

The import tool supports a set of SCIM attributes for user data. The attributes are defined in the `configurations.toml` file under the `[attribute_mapping]` section. Mapping should be provided for each required attribute in the form `attribute_name = "ldap_attribute_name"`. Only the attributes defined here will be imported.

```toml
[attribute_mapping]
userName="cn"
firstName="givenName"
lastName="familyName"
mobilePhoneNumber="mobile"
primaryEmail="mail"
password="userPassword"
```

Following SCIM attributes are supported by the tool:

#### Core attributes (core and core:2.0:User schemas):

- accountLocked
- externalId
- userName
- firstName
- lastName
- middleName
- formattedName
- displayName
- nickName
- profileUrl
- locale
- photoThumbnailUrl
- mobilePhoneNumber
- primaryEmail
- active
- homeAddressLocality
- homeAddressPostalCode
- homeAddressRegion
- homeAddressStreetAddress

Additionally following attributes can also be configured by adding custom schema mappings in the WSO2 IS.

- honorificPrefix, honorificSuffix
- homeEmail, workEmail, otherEmail
- homePhoneNumber, workPhoneNumber, otherPhoneNumber, pagerPhoneNumber, faxPhoneNumber
- homeAddressCountry, homeAddressFormatted
- workAddressLocality, workAddressPostalCode, workAddressRegion, workAddressStreetAddress, workAddressCountry, workAddressFormatted
- otherAddressLocality, otherAddressPostalCode, otherAddressRegion, otherAddressStreetAddress, otherAddressCountry, otherAddressFormatted
- photoUrl, userType, title, preferredLanguage, timezone
- imsAIM, imsGTalk, imsICQ, imsXMPP, imsMSN, imsSkype, imsQQ, imsYahoo, imsOther

#### Enterprise attributes (enterprise:2.0:User schema):

- managerDisplayName

Additionally following attributes can also be configured by adding custom schema mappings in the WSO2 IS organization.

- managerValue, managerRef
- employeeNumber, costCenter, organization, division, department


### User data LDIF

User data should be provided in a LDIF file. The attributes of the LDIF should correspond to the attribute mappings defined in the `configurations.toml` file.

A sample input file is given at the `org.wso2.ldif.scim-<version>/export_users_SHA512.ldif` file.

## Running the tool

1. Place your user data LDIF file at the location specified in the configuration file.

2. Navigate to the `org.wso2.ldif.scim-<version>/bin` directory.

3. Open a terminal and run the following command to start the tool:

    ```bash
    bash start.sh
    ```

### Note

- The tool will import the users and generate a summary report in the `org.wso2.ldif.scim-<version>/output` directory.
