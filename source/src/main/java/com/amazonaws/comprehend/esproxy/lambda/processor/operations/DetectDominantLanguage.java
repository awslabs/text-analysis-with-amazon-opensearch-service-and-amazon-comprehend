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
