package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectDominantLanguageRequest;
import com.amazonaws.services.comprehend.model.BatchDetectDominantLanguageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectDominantLanguageTest {
    @Mock
    private AmazonComprehend mockComprehendClient;

    @Mock
    BatchDetectDominantLanguageResult mockBatchDetectLanguageResult;

    private DetectDominantLanguage test = new DetectDominantLanguage();

    @Test
    public void successDetectDominantLanguageCall() {
        when(mockComprehendClient.detectDominantLanguage(any())).thenReturn(TestConstants.getDetectDominantLanguageResult());
        SingularResponse result = test.sendSingularRequest(TestConstants.FIELD_NAME,
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient);

        assertEquals(TestConstants.FIELD_NAME, result.getFieldNameAndOperation());
    }

    @Test
    public void successBatchDetectDominantLanguageCall() {
        when(mockComprehendClient.batchDetectDominantLanguage(any(BatchDetectDominantLanguageRequest.class)))
                .thenReturn(mockBatchDetectLanguageResult);
        BatchResponse result = test.sendBatchRequest(
                TestConstants.FIELD_LOCATOR_LIST,
                TestConstants.CONTENT_LIST,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient);

        assertEquals(TestConstants.FIELD_LOCATOR_LIST, result.getLocatorList());
    }

}
