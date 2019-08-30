package com.amazonaws.comprehend.esproxy.lambda.exception;

import lombok.Builder;
import lombok.Getter;

/**
 * AmazonServiceException thrown from SingularOperationCallable
 */
@Builder
@Getter
public class ServiceExceptionMessage {
    private int statusCode;
    private String errorCode;
    private String requestId;
    private String errorMessage;
}
