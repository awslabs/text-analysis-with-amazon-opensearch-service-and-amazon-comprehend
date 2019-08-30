package com.amazonaws.comprehend.esproxy.lambda.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Help to attach the comprehend result back to the bulk content
 * The contentRowNum records where to put back the fieldNameAndOperation in a batch request
 * i.e.:
 * fieldNameAndOperation = "message_DetectKeyPhrases"
 * contentRowNum = 4
 */
@AllArgsConstructor
@Getter
public class BatchFieldLocator {
    @Setter
    private String fieldNameAndOperation;

    private final int contentRowNum;
}