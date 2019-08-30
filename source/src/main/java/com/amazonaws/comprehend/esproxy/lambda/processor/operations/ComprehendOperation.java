package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.services.comprehend.AmazonComprehend;
import lombok.NonNull;

import java.util.List;

/**
 * Interface for ComprehendOperations, contains methods of sending Singular requests and Batch requests
 */
public interface ComprehendOperation {

    // Get Visualization Name List for the operation. The name list was used for uploading kibana dashboard
    String[] getVisualizationNameList();

    // Send out singular request for the operation, return SingularResponse that stores the comprehend client responses
    SingularResponse sendSingularRequest(@NonNull final String fieldNameAndOperation,
                                         @NonNull final String content,
                                         @NonNull final LanguageCode languageCode,
                                         @NonNull final AmazonComprehend comprehendClient);


    //Send out batch request for the operation, return BatchResponse that stores the comprehend client responses
    BatchResponse sendBatchRequest(@NonNull final List<BatchFieldLocator> fieldLocatorList,
                                   @NonNull final List<String> contentList,
                                   @NonNull final LanguageCode languageCode,
                                   @NonNull final AmazonComprehend comprehendClient);
}
