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
