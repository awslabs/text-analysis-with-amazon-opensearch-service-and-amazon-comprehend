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

package com.amazonaws.comprehend.esproxy.lambda.utils.serializer;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializer to transform between input String and PreprocessingConfigRequest
 */
@RequiredArgsConstructor
public class ConfigSerializer implements ComprehendSerializer<PreprocessingConfigRequest> {
    @NonNull
    private final ObjectMapper mapper;

    /**
     * Deserialize customer input Config, i.e.:
     * {
     *    "comprehendConfigurations":[{
     *         "indexName": "tweeter",
     *         "fieldName": "message",
     *         "comprehendOperations": ["DetectSentiment"],
     *         "languageCode": "en"
     *       },
     *       {
     *         "indexName": "facebook",
     *         "fieldName": "text",
     *         "comprehendOperations": ["DetectSentiment", "DetectEntities"],
     *         "languageCode": "es"
     *       }]
     * }
     */
    // Json String -> PreprocessingConfigRequest
    public PreprocessingConfigRequest deserialize(@NonNull final String input) throws InvalidRequestException {
        if (input.isEmpty()) {
            throw new InvalidRequestException(CustomerMessage.INPUT_NULL_OR_EMPTY_ERROR);
        }
        ObjectReader objectReader = mapper.readerFor(new TypeReference<PreprocessingConfigRequest>() {});
        try {
            JsonNode configJson = mapper.readTree(input);
            return objectReader.readValue(configJson);
        } catch (IOException e) {
            throw new InvalidRequestException(CustomerMessage.CONFIG_MALFORMED_ERROR, e);
        }
    }

    // PreprocessingConfigRequest -> Json String
    public String serialize(@NonNull PreprocessingConfigRequest configRequest) throws InvalidRequestException {
        try {
            return mapper.writeValueAsString(configRequest);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException(CustomerMessage.CONFIG_MALFORMED_ERROR, e);
        }
    }

    // PreprocessingConfigRequest -> Config Map
    public static Map<String, ComprehendConfiguration> transformConfigRequestToConfigMap(
            @NonNull PreprocessingConfigRequest configRequest) {
        List<ComprehendConfiguration> configList = configRequest.getComprehendConfigurations();

        // all key/value shall exist
        try {
            Map<String, ComprehendConfiguration> configMap = configList.stream()
                    .filter(config -> !config.configIsInvalid())
                    .collect(Collectors.toMap(
                            value -> String.format("%s_%s", value.getIndexName(), value.getFieldName()),
                            value -> value));
            // all configs should be valid
            if (configMap.size() != configList.size()) {
                throw new InvalidRequestException(CustomerMessage.CONFIG_FIELD_MISSING_OR_EMPTY_ERROR);
            }
            return configMap;
        } catch (IllegalStateException e) {
            // the exception will be thrown when the list tried to put duplicated indexName_fieldName pair into the map
            // the indexName_fieldName pair should be unique
            throw new InvalidRequestException(CustomerMessage.CONFIG_FIELD_NAME_DUPLICATED_ERROR);
        }
    }

    // Config Map -> PreprocessingConfigRequest
    public static PreprocessingConfigRequest transformConfigMapToConfigRequest(
            @NonNull Map<String, ComprehendConfiguration> configMap) {
        List<ComprehendConfiguration> configList = new ArrayList<>(configMap.values());
        return new PreprocessingConfigRequest(configList);
    }

}
