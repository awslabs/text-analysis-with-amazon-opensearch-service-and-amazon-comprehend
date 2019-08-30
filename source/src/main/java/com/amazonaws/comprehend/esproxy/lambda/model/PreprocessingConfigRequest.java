package com.amazonaws.comprehend.esproxy.lambda.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * Stores the input PreprocessingConfigRequest, the ComprehendConfigurations will be stored in a list
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
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PreprocessingConfigRequest {
    @NonNull
    private List<ComprehendConfiguration> comprehendConfigurations;
}
