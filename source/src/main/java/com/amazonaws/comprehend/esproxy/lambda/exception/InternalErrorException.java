package com.amazonaws.comprehend.esproxy.lambda.exception;

import lombok.NonNull;

/**
 * Thrown when the service encounters an internal error during processing the request.
 */
public class InternalErrorException extends RuntimeException {
    public InternalErrorException(@NonNull final String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    public InternalErrorException(@NonNull final String message) {
        super(message);
    }
}
