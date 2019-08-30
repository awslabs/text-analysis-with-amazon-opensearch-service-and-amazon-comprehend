package com.amazonaws.comprehend.esproxy.lambda.exception;

import lombok.Builder;
import lombok.Getter;

/**
 * AmazonClientException thrown from SingularOperationCallable
 */
@Builder
@Getter
public class ClientExceptionMessage {
    private String errorCode;
    private String errorMessage;
}
