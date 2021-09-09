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
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectKeyPhrasesItemResult;
import com.amazonaws.services.comprehend.model.BatchDetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.BatchDetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import lombok.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DetectKeyPhrases implements ComprehendOperation {
    private static final String[] VISUALIZATION_NAME_LIST = {"keyphrase-heat-map", "keyphrase-cloud"};

    @Override
    public String[] getVisualizationNameList() {
        return VISUALIZATION_NAME_LIST;
    }

    /**
     * Send out DetectKeyPhrasesRequest, return SingularResponse that stores DetectKeyPhrasesResult
     * Results are copied for Kibana visualization
     */
    @Override
    public SingularResponse sendSingularRequest(@NonNull final String fieldNameAndOperation,
                                                @NonNull final String content,
                                                @NonNull final LanguageCode languageCode,
                                                @NonNull final AmazonComprehend comprehendClient) {
        DetectKeyPhrasesRequest request = new DetectKeyPhrasesRequest().withText(content)
                .withLanguageCode(languageCode.toString());
        DetectKeyPhrasesResult detectKeyPhraseResult = comprehendClient.detectKeyPhrases(request);

        return new SingularResponse(fieldNameAndOperation, new JSONObject(detectKeyPhraseResult),
                new JSONObject(detectKeyPhraseResult));
    }

    /**
     * Send out BatchDetectKeyPhrasesRequest, return BatchResponse that stores BatchDetectKeyPhrasesResult
     * Results are copied for Kibana visualization
     */
    @Override
    public BatchResponse sendBatchRequest(@NonNull final List<BatchFieldLocator> fieldLocatorList,
                                          @NonNull final List<String> contentList,
                                          @NonNull final LanguageCode languageCode,
                                          @NonNull final AmazonComprehend comprehendClient) {
        BatchDetectKeyPhrasesRequest request
                = new BatchDetectKeyPhrasesRequest().withTextList(contentList).withLanguageCode(languageCode.toString());
        BatchDetectKeyPhrasesResult batchDetectKeyPhrasesResult = comprehendClient.batchDetectKeyPhrases(request);

        List<JSONObject> copiedKeyPhrasesList = new ArrayList<>();

        // Copy KeyPhrases result
        for (BatchDetectKeyPhrasesItemResult resultItem : batchDetectKeyPhrasesResult.getResultList()) {
            // Parse List<KeyPhrase> to List<JSONObject>
            List<JSONObject> keyPhraseJsonList = resultItem.getKeyPhrases()
                    .stream().map(JSONObject::new).collect(Collectors.toList());
            try {
                // Add the index key and JSONObject list to the copied list
                copiedKeyPhrasesList.add(new JSONObject().put(Constants.KEYPHRASES_KEY_NAME, keyPhraseJsonList));
            } catch (JSONException e) {
                copiedKeyPhrasesList.add(null);
            }
        }
        return new BatchResponse<>(
                fieldLocatorList,
                batchDetectKeyPhrasesResult.getResultList(),
                batchDetectKeyPhrasesResult.getErrorList(), copiedKeyPhrasesList);
    }
}
