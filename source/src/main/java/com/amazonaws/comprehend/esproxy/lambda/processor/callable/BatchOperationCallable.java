package com.amazonaws.comprehend.esproxy.lambda.processor.callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchItemError;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A Comprehend batch operation task that returns a Comprehend Client response
 */
@RequiredArgsConstructor
public class BatchOperationCallable implements Callable<BatchResponse> {
    @NonNull
    private final ComprehendOperationEnum comprehendOperation;

    @NonNull
    private final LanguageCode languageCode;

    @NonNull
    private final AmazonComprehend comprehendClient;

    @Getter
    @NonNull
    private final List<BatchFieldLocator> fieldLocatorList;

    @Getter
    @NonNull
    private final List<String> contentList;

    @Override
    public BatchResponse call() {
        try {
            return comprehendOperation.getComprehendOperation()
                    .sendBatchRequest(fieldLocatorList, contentList, languageCode, comprehendClient);
        } catch (AmazonServiceException e) {
            return new BatchResponse<>(fieldLocatorList, Collections.emptyList(),
                    getBatchItemErrors(e.getErrorCode(), e.getErrorMessage()), null);
        } catch (AmazonClientException e) {
            return new BatchResponse<>(fieldLocatorList, Collections.emptyList(),
                    getBatchItemErrors(Constants.CLIENT_EXCEPTION_ERROR_CODE, e.getMessage()), null);
        }
    }

    private List<BatchItemError> getBatchItemErrors(String errorCode, String errorMessage) {
        // Set the response key to "fieldName_operation_Error"
        fieldLocatorList.forEach(locator ->
                locator.setFieldNameAndOperation(String.format("%s_Error", locator.getFieldNameAndOperation())));
        List<BatchItemError> errorList = new ArrayList<>();

        // Add errorMessage to each request
        for (int index = 0; index < contentList.size(); index++) {
            BatchItemError batchItemError = new BatchItemError();
            batchItemError.setIndex(index);
            batchItemError.setErrorCode(errorCode);
            batchItemError.setErrorMessage(errorMessage);
            errorList.add(batchItemError);
        }
        return errorList;
    }

}
