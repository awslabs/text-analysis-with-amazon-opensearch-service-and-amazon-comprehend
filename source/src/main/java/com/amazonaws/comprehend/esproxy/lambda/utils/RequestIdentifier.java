package com.amazonaws.comprehend.esproxy.lambda.utils;

import lombok.NonNull;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.client.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Identify the type of input request
 */
public class RequestIdentifier {
    // Whether a request is Preprocessing Configuration request
    public static boolean isConfigRequest(@NonNull Request esRequest) {
        return Constants.CONFIG_PATH_EXPOSED_TO_CUSTOMER.equalsIgnoreCase(esRequest.getEndpoint()) ||
                isConfigUpdateRequest(esRequest);
    }

    // Whether a request is a Preprocessing Configuration update request
    public static boolean isConfigUpdateRequest(@NonNull Request esRequest) {
        String updateConfigPath = String.format("%s/%s",
                Constants.CONFIG_PATH_EXPOSED_TO_CUSTOMER, Constants.UPDATE_REQUEST);
        return updateConfigPath.equalsIgnoreCase(esRequest.getEndpoint());
    }

    // Whether the request create or update new resources
    public static boolean isMutationRequest(@NonNull Request esRequest) {
        String httpMethod = esRequest.getMethod();

        return HttpPut.METHOD_NAME.equalsIgnoreCase(httpMethod) ||
                HttpPost.METHOD_NAME.equalsIgnoreCase(httpMethod);
    }

    // Whether a request is index request
    public static boolean isIndexRequest(@NonNull Request esRequest) {
        // the path should contain /index/type/ or /index/type/id
        // type shouldn't be _search
        String[] pathFields = esRequest.getEndpoint().substring(1).split("/");

        return (pathFields.length == Constants.INDEX_PATH_LENGTH
                || pathFields.length == Constants.INDEX_PATH_LENGTH_WITHOUT_ID)
                && !Constants.SEARCH_REQUEST.equals(pathFields[1])
                && pathFields[0].matches(Constants.INDEX_REGEX);
    }

    // Whether a request is bulk request
    public static boolean isBulkRequest(@NonNull Request esRequest) {
        return Constants.BULK_REQUEST.equals(esRequest.getEndpoint());
    }

    // Get the indexName from a singular index request
    public static String getIndexName(@NonNull Request esRequest) {
        String[] pathFields = esRequest.getEndpoint().substring(1).split("/");
        return pathFields[0];
    }

    // Extract the indexName from a bulk payloadLine
    public static Optional<String> getBulkIndexName(@NonNull String payloadLine) {
        String payloadLineWithoutSpace = payloadLine.replaceAll(" +", "");
        Optional<String> indexName = Optional.empty();
        try {
            JSONObject payloadJson = new JSONObject(payloadLineWithoutSpace);

            if (payloadJson.has(Constants.BULK_INDEX_ACTION_KEY)) {
                return Optional.ofNullable(payloadJson.getJSONObject(Constants.BULK_INDEX_ACTION_KEY)
                        .getString(Constants.BULK_INDEX_ACTION_KEY_NAME));
            }
            if (payloadJson.has(Constants.BULK_CREATE_ACTION_KEY)) {
                return Optional.ofNullable(payloadJson.getJSONObject(Constants.BULK_CREATE_ACTION_KEY)
                        .getString(Constants.BULK_INDEX_ACTION_KEY_NAME));
            }
            // The payloadLine does not contain a index action
            return indexName;
        } catch (JSONException e) {
            return indexName;
        }
    }

}
