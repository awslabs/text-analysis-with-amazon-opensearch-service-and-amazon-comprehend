// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License").
// You may not use this file except in compliance with the License.
// A copy of the License is located at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// or in the "license" file accompanying this file. This file is distributed
// on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the License for the specific language governing
// permissions and limitations under the License.

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
