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

import com.amazonaws.comprehend.esproxy.lambda.processor.operations.ComprehendOperation;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectDominantLanguage;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectEntities;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectKeyPhrases;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectSentiment;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectSyntax;
import lombok.NoArgsConstructor;

/**
 * The supported Comprehend operations
 * More can be found in: https://docs.aws.amazon.com/comprehend/latest/dg/API_Operations.html
 */
@NoArgsConstructor
public enum ComprehendOperationEnum {
    DetectDominantLanguage,
    DetectEntities,
    DetectKeyPhrases,
    DetectSentiment,
    DetectSyntax;

    /**
     * Get ComprehendOperation instance based on the Operation Enum passed from PreprocessingConfigRequest
     * @return ComprehendOperation instance
     */
    public ComprehendOperation getComprehendOperation() {
        switch (this) {
            case DetectDominantLanguage:
                return new DetectDominantLanguage();
            case DetectEntities:
                return new DetectEntities();
            case DetectSentiment:
                return new DetectSentiment();
            case DetectKeyPhrases:
                return new DetectKeyPhrases();
            case DetectSyntax:
                return new DetectSyntax();
            default:
                throw new IllegalArgumentException("Illegal ComprehendOperation");
        }
    }
}
