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
import com.amazonaws.services.comprehend.model.BatchDetectSyntaxItemResult;
import com.amazonaws.services.comprehend.model.BatchDetectSyntaxRequest;
import com.amazonaws.services.comprehend.model.BatchDetectSyntaxResult;
import com.amazonaws.services.comprehend.model.DetectSyntaxRequest;
import com.amazonaws.services.comprehend.model.DetectSyntaxResult;
import com.amazonaws.services.comprehend.model.SyntaxToken;
import lombok.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetectSyntax implements ComprehendOperation {
    private static final String[] VISUALIZATION_NAME_LIST = {"syntax-noun", "syntax-verb"};

    @Override
    public String[] getVisualizationNameList() {
        return VISUALIZATION_NAME_LIST;
    }

    /**
     * Send out DetectSyntaxRequest, return SingularResponse that stores DetectSyntaxResult
     * Results are flattened for Kibana visualization
     */
    @Override
    public SingularResponse sendSingularRequest(@NonNull final String fieldNameAndOperation,
                                                @NonNull final String content,
                                                @NonNull final LanguageCode languageCode,
                                                @NonNull final AmazonComprehend comprehendClient) {
        DetectSyntaxRequest request = new DetectSyntaxRequest().withText(content)
                .withLanguageCode(languageCode.toString());
        DetectSyntaxResult detectSyntaxResult = comprehendClient.detectSyntax(request);

        return new SingularResponse(fieldNameAndOperation, new JSONObject(detectSyntaxResult),
                flattenSyntaxList(detectSyntaxResult.getSyntaxTokens()));
    }

    /**
     * Send out BatchDetectSyntaxRequest, return BatchResponse that stores BatchDetectSyntaxResult
     * Results are flattened for Kibana visualization
     */
    @Override
    public BatchResponse sendBatchRequest(@NonNull final List<BatchFieldLocator> fieldLocatorList,
                                          @NonNull final List<String> contentList,
                                          @NonNull final LanguageCode languageCode,
                                          @NonNull final AmazonComprehend comprehendClient) {
        BatchDetectSyntaxRequest request
                = new BatchDetectSyntaxRequest().withTextList(contentList).withLanguageCode(languageCode.toString());
        BatchDetectSyntaxResult batchDetectSyntaxResult = comprehendClient.batchDetectSyntax(request);

        List<JSONObject> flattenedSyntaxList = new ArrayList<>();
        for (BatchDetectSyntaxItemResult resultItem : batchDetectSyntaxResult.getResultList()) {
            flattenedSyntaxList.add(flattenSyntaxList(resultItem.getSyntaxTokens()));
        }

        return new BatchResponse<>(
                fieldLocatorList,
                batchDetectSyntaxResult.getResultList(),
                batchDetectSyntaxResult.getErrorList(), flattenedSyntaxList);
    }

    /**
     * Flatten the syntaxList to JSONObject
     *
     * @param syntaxList The received syntaxList from Comprehend response
     * @return JSONObject that contains flattened SyntaxList as JSONArray
     */
    private static JSONObject flattenSyntaxList(@NonNull final List<SyntaxToken> syntaxList) {
        JSONObject flattenedSyntaxObject = new JSONObject();
        for (SyntaxToken syntax : syntaxList) {
            try {
                // If the syntax type exists, add the new syntax to the JSONArray, otherwise create a new JSONObject
                if (flattenedSyntaxObject.has(syntax.getPartOfSpeech().getTag())) {
                    flattenedSyntaxObject.getJSONArray(syntax.getPartOfSpeech().getTag()).put(new JSONObject(syntax));
                } else {
                    flattenedSyntaxObject.put(syntax.getPartOfSpeech().getTag(),
                            Collections.singletonList((new JSONObject(syntax))));
                }
            } catch (JSONException e) {
                return null;
            }
        }
        return flattenedSyntaxObject;
    }
}
