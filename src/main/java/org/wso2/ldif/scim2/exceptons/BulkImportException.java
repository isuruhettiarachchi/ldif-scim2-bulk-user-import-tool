package org.wso2.ldif.scim2.exceptons;

public class BulkImportException extends Exception {

    public BulkImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public BulkImportException(String message) {
        super(message);
    }
}
