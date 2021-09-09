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

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * Serializer to transform between ingestion payload String and JsonNode
 */
@RequiredArgsConstructor
public class IngestionSerializer implements ComprehendSerializer<JsonNode> {
    @NonNull
    private final ObjectMapper mapper;

    public JsonNode deserialize(@NonNull final String input){
        try {
            return mapper.readTree(input);
        } catch (IOException e) {
            throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
        }
    }

    public String serialize(@NonNull final JsonNode input){
        try {
            return mapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
        }
    }
}
