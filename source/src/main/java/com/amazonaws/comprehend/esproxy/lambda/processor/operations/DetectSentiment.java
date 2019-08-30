package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectSentimentRequest;
import com.amazonaws.services.comprehend.model.BatchDetectSentimentResult;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.json.JSONObject;

import java.util.List;

@AllArgsConstructor
public class DetectSentiment implements ComprehendOperation {
    private static final String[] VISUALIZATION_NAME_LIST =
            {"sentiment-neg-table", "sentiment-pos-table", "sentiment-time", "sentiment-type-count"};

    @Override
    public String[] getVisualizationNameList() {
        return VISUALIZATION_NAME_LIST;
    }

    /**
     * Send out DetectSentimentRequest, return SingularResponse that stores DetectSentimentResult
     * Results are not flattened
     */
    @Override
    public SingularResponse sendSingularRequest(@NonNull final String fieldNameAndOperation,
                                                @NonNull final String content,
                                                @NonNull final LanguageCode languageCode,
                                                @NonNull final AmazonComprehend comprehendClient) {
        DetectSentimentRequest request = new DetectSentimentRequest().withText(content)
                .withLanguageCode(languageCode.toString());
        DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(request);

        return new SingularResponse(fieldNameAndOperation, new JSONObject(detectSentimentResult), null);

    }

    /**
     * Send out BatchDetectSentimentRequest, return BatchResponse that stores BatchDetectSentimentResult
     * Results are not flattened
     */
    @Override
    public BatchResponse sendBatchRequest(@NonNull final List<BatchFieldLocator> fieldLocatorList,
                                          @NonNull final List<String> contentList,
                                          @NonNull final LanguageCode languageCode,
                                          @NonNull final AmazonComprehend comprehendClient) {
        BatchDetectSentimentRequest request
                = new BatchDetectSentimentRequest().withTextList(contentList).withLanguageCode(languageCode.toString());
        BatchDetectSentimentResult batchDetectSentimentResult = comprehendClient.batchDetectSentiment(request);

        return new BatchResponse<>(
                fieldLocatorList,
                batchDetectSentimentResult.getResultList(),
                batchDetectSentimentResult.getErrorList(), null);
    }
}
