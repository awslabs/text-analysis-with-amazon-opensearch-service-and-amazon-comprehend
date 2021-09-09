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

package com.amazonaws.comprehend.esproxy.lambda.utils;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ComprehendSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ConfigSerializer;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.client.Response;

import java.util.Collections;
import java.util.Map;

/**
 * To retrieve the stored Comprehend OpenSearch Service Preprocessing Configuration
 */
@AllArgsConstructor
public class ConfigRetriever {
    private final ComprehendSerializer<PreprocessingConfigRequest> configSerializer;

    private final OpenSearchServiceClient esClient;

    /**
     * Retrieve the stored Comprehend OpenSearch Service Preprocessing Configuration
     *
     * @return Preprocessing configuration as a Map(indexName_fieldName pair, ComprehendConfiguration)
     */
    public Map<String, ComprehendConfiguration> retrieveStoredConfig() {
        Response esConfigResponse = esClient.performRequest(HttpGet.METHOD_NAME,
                String.format("%s%s", Constants.CONFIG_PATH, "/_source"));

        if (esConfigResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            // If no comprehend config was found, we will treat all requests as pass through
            return Collections.emptyMap();
        }
        String retrievedConfig = HTTPTransformer.transformHttpEntityToString(esConfigResponse.getEntity());
        PreprocessingConfigRequest retrievedConfigRequest = configSerializer.deserialize(retrievedConfig);

        return ConfigSerializer.transformConfigRequestToConfigMap(retrievedConfigRequest);
    }

}
