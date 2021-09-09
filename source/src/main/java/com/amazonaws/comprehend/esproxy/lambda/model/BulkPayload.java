// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License").
// You may not use this file except in compliance with the License.
// A copy of the License is located at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// or in the "license" file accompanying this file. This file is distributed
// on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the License for the specific language governing
// permissions and limitations under the License.

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
