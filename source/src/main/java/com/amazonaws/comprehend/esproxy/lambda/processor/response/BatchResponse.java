package com.amazonaws.comprehend.esproxy.lambda.processor.response;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.services.comprehend.model.BatchItemError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Response list from Comprehend batch API call
 *
 * @param <T> Comprehend batch item result type. i.e. BatchDetectKeyPhrasesItemResult, BatchDetectEntitiesItemResult
 */
@AllArgsConstructor
public class BatchResponse<T> {
    @Getter
    @NonNull
    private List<BatchFieldLocator> locatorList;

    @Getter
    @NonNull
    private List<T> batchResultItemList;

    @NonNull
    private List<BatchItemError> batchResultErrorList;

    // Null if the operation results do not require flattening. i.e. BatchDetectSentimentResult
    @Getter
    private List<JSONObject> batchFlattenedResultList;

    public List<JSONObject> getBatchResultList() {
        // Combine and sort the results by index
        List<JSONObject> combinedResultList =
                Stream.concat(batchResultItemList.stream().map(JSONObject::new),
                        batchResultErrorList.stream().map(JSONObject::new))
                        .sorted((JSONObject a, JSONObject b) -> {
                            try {
                                return a.getInt(Constants.BULK_INDEX_KEY_NAME) - b.getInt(Constants.BULK_INDEX_KEY_NAME);
                            } catch (JSONException e) {
                                throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
                            }
                        }).collect(Collectors.toList());

        // Remove Index field from the result
        combinedResultList.forEach(result -> result.remove(Constants.BULK_INDEX_KEY_NAME));
        return combinedResultList;
    }
}