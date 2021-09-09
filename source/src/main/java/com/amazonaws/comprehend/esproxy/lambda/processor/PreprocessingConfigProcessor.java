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

package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.amazonaws.comprehend.esproxy.lambda.utils.ConfigRetriever;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.comprehend.esproxy.lambda.utils.HTTPTransformer;
import com.amazonaws.comprehend.esproxy.lambda.utils.RequestIdentifier;
import com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaUploader;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ComprehendSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ConfigSerializer;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.http.HttpEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processor to process Comprehend OpenSearch Service Preprocessing Configuration requests
 */
@AllArgsConstructor
public class PreprocessingConfigProcessor implements OpenSearchServiceProcessor {
    @NonNull
    private final ComprehendSerializer<PreprocessingConfigRequest> configSerializer;

    @NonNull
    private final OpenSearchServiceClient esClient;

    @NonNull
    private final ConfigRetriever configRetriever;

    @NonNull
    private final KibanaUploader kibanaUploader;

    /**
     * Process Comprehend OpenSearch Service Preprocessing Configuration requests
     *
     * @param request The received OpenSearch Service client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return OpenSearch Service service response
     */
    @Override
    public Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger) {
        logger.log("Comprehend Configuration request detected");

        // If the request is a non create nor update config, replace the customer config path with the real config path
        if (!RequestIdentifier.isMutationRequest(request)) {
            return esClient.performRequest(request, Constants.CONFIG_PATH);
        }
        HttpEntity receivedEntity = request.getEntity();
        String inputConfig = HTTPTransformer.transformHttpEntityToString(receivedEntity);

        PreprocessingConfigRequest configRequest = configSerializer.deserialize(inputConfig);
        Map<String, ComprehendConfiguration> newConfigMap = ConfigSerializer
                .transformConfigRequestToConfigMap(configRequest);
        Map<String, ComprehendConfiguration> oldConfigMap = configRetriever.retrieveStoredConfig();

        // Find the difference between the two configs, and upload the mapping
        kibanaUploader.uploadMapping(
                newConfigMap.entrySet().stream().filter(entry -> !oldConfigMap.containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), logger);

        // If it is a update request, retrieve the old config, and attach the new config to it
        if (!oldConfigMap.isEmpty() && RequestIdentifier.isConfigUpdateRequest(request)) {
            oldConfigMap.putAll(newConfigMap);
            newConfigMap = oldConfigMap;
            PreprocessingConfigRequest newConfigRequest = ConfigSerializer
                    .transformConfigMapToConfigRequest(newConfigMap);
            inputConfig = configSerializer.serialize(newConfigRequest);
        }

        // Best effort to upload the dashboard
        kibanaUploader.uploadKibanaDashboard(newConfigMap, logger);

        // Save config
        return esClient.performRequest(request.getMethod(), Constants.CONFIG_PATH, inputConfig);
    }

}
