package com.amazonaws.comprehend.esproxy.lambda.exception;

/**
 * Customer messages in API gateway response when exception happens
 */
public class CustomerMessage {
    // Internal error
    public final static String INTERNAL_ERROR =
            "[ERROR] Internal error happened when processing your requests, please try again later";

    // Customer error
    public final static String INPUT_NULL_OR_EMPTY_ERROR =
            "[ERROR] The input body cannot be null or empty";

    public final static String CONFIG_MALFORMED_ERROR =
            "[ERROR] The ComprehendConfig was malformed. Please make sure it matches the ComprehendConfig Schema";

    public final static String CONFIG_FIELD_NAME_DUPLICATED_ERROR =
            "[ERROR] The ComprehendConfig contains duplicated index-fieldName pair";

    public final static String CONFIG_FIELD_MISSING_OR_EMPTY_ERROR =
            "[ERROR] Some fields are missing or empty in the ComprehendConfig";

}
