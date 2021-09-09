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
