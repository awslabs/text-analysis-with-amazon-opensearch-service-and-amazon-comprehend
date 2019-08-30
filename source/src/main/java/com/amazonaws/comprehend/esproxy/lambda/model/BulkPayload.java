package com.amazonaws.comprehend.esproxy.lambda.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BulkPayload for each ingestion request
 * i.e.
 * <p>
 * For the below bulk request will generate two BulkPayload objects
 * { "index" : { "_index" : "test", "_type" : "_doc", "_id" : "1" } }
 * { "text" : "value1","title":"value2" }
 * { "delete" : { "_index" : "tweeter", "_type" : "_doc", "_id" : "2" } }
 * { "index" : { "_index" : "facebook", "_type" : "_doc", "_id" : "1" } }
 * { "message" : "value1","title":"value2" }
 * </p>
 * <p>
 * BulkPayload object1:
 * indexName = "test"
 * payloadJson = "{ \"text\" : \"value1\",\"title\":\"value2\" }"
 * BulkPayload object2:
 * indexName = "facebook"
 * payloadJson = "{ \"message\" : \"value1\",\"title\":\"value2\" }"
 * </p>
 */
@AllArgsConstructor
@Getter
public class BulkPayload {
    private final String indexName;

    private final JsonNode payloadJson;
}
