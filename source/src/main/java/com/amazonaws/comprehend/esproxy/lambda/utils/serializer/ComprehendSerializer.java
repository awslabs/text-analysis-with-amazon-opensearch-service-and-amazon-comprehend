package com.amazonaws.comprehend.esproxy.lambda.utils.serializer;

import lombok.NonNull;

/**
 * {@code ComprehendSerializer} is the interface of all serializers that
 * serialize/deserialize type T to String
 */
public interface ComprehendSerializer<T> {

    /**
     * Deserialize the received input String to type T
     *
     * @param input The received input String
     * @return The deserialized T
     */
    T deserialize(@NonNull final String input);

    /**
     * Serialize the input type T to String
     *
     * @param input The type T input to be serialized
     * @return The serialized String
     */
    String serialize(@NonNull final T input);
}
