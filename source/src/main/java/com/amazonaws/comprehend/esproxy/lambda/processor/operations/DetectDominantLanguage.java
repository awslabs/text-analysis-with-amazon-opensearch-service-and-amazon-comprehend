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

package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectDominantLanguageRequest;
import com.amazonaws.services.comprehend.model.BatchDetectDominantLanguageResult;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageRequest;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageResult;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.json.JSONObject;

import java.util.List;

@NoArgsConstructor
public class DetectDominantLanguage implements ComprehendOperation {
    private static final String[] VISUALIZATION_NAME_LIST = {};

    @Override
    public String[] getVisualizationNameList() {
        return VISUALIZATION_NAME_LIST;
    }

    /**
     * Send out DetectDominantLanguageRequest, return SingularResponse that stores DetectDominantLanguageResult
     * Results are not flattened
     */
    @Override
    public SingularResponse sendSingularRequest(@NonNull final String fieldNameAndOperation,
                                                @NonNull final String content,
                                                @NonNull final LanguageCode languageCode,
                                                @NonNull final AmazonComprehend comprehendClient) {
        DetectDominantLanguageRequest request
                = new DetectDominantLanguageRequest().withText(content);
        DetectDominantLanguageResult detectDominantLanguageResult = comprehendClient.detectDominantLanguage(request);

        return new SingularResponse(fieldNameAndOperation, new JSONObject(detectDominantLanguageResult), null);
    }

    /**
     * Send out BatchDetectDominantLanguageRequest, return BatchResponse that stores BatchDetectDominantLanguageResult
     * Results are not flattened
     */
    @Override
    public BatchResponse sendBatchRequest(@NonNull final List<BatchFieldLocator> fieldLocatorList,
                                          @NonNull final List<String> contentList,
                                          @NonNull final LanguageCode languageCode,
                                          @NonNull final AmazonComprehend comprehendClient) {
        BatchDetectDominantLanguageRequest request
                = new BatchDetectDominantLanguageRequest().withTextList(contentList);
        BatchDetectDominantLanguageResult batchDetectDominantLanguageResult =
                comprehendClient.batchDetectDominantLanguage(request);

        return new BatchResponse<>(
                fieldLocatorList,
                batchDetectDominantLanguageResult.getResultList(),
                batchDetectDominantLanguageResult.getErrorList(), null);
    }

}
