package com.amazonaws.comprehend.esproxy.lambda.processor.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.json.JSONObject;

/**
 * Response list from Comprehend singular API call
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SingularResponse {
    @NonNull
    private String fieldNameAndOperation;

    @NonNull
    private JSONObject comprehendResult;

    // Null if the operation results do not require flattening. i.e. DetectSentimentResult
    private JSONObject flattenedResult;
}
