package com.amazonaws.comprehend.esproxy.lambda.exception;

import lombok.NonNull;

/**
 * Thrown when the service encounters a customer error during processing the request.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(@NonNull final String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    public InvalidRequestException(@NonNull final String message) {
        super(message);
    }
}
