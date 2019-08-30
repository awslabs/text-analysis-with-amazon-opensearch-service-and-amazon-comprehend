package com.amazonaws.comprehend.esproxy.lambda.model;

import com.amazonaws.comprehend.esproxy.lambda.processor.operations.ComprehendOperation;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectDominantLanguage;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectEntities;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectKeyPhrases;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectSentiment;
import com.amazonaws.comprehend.esproxy.lambda.processor.operations.DetectSyntax;
import lombok.NoArgsConstructor;

/**
 * The supported Comprehend operations
 * More can be found in: https://docs.aws.amazon.com/comprehend/latest/dg/API_Operations.html
 */
@NoArgsConstructor
public enum ComprehendOperationEnum {
    DetectDominantLanguage,
    DetectEntities,
    DetectKeyPhrases,
    DetectSentiment,
    DetectSyntax;

    /**
     * Get ComprehendOperation instance based on the Operation Enum passed from PreprocessingConfigRequest
     * @return ComprehendOperation instance
     */
    public ComprehendOperation getComprehendOperation() {
        switch (this) {
            case DetectDominantLanguage:
                return new DetectDominantLanguage();
            case DetectEntities:
                return new DetectEntities();
            case DetectSentiment:
                return new DetectSentiment();
            case DetectKeyPhrases:
                return new DetectKeyPhrases();
            case DetectSyntax:
                return new DetectSyntax();
            default:
                throw new IllegalArgumentException("Illegal ComprehendOperation");
        }
    }
}
