package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectSentimentRequest;
import com.amazonaws.services.comprehend.model.BatchDetectSentimentResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectSentimentTest {
    @Mock
    private AmazonComprehend mockComprehendClient;

    @Mock
    BatchDetectSentimentResult mockBatchDetectSentimentResult;

    private DetectSentiment test = new DetectSentiment();

    @Test
    public void successDetectSentimentCall() {
        when(mockComprehendClient.detectSentiment(any())).thenReturn(TestConstants.getDetectSentimentResult());
        SingularResponse result = test.sendSingularRequest(TestConstants.FIELD_NAME,
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient);

        assertEquals(TestConstants.FIELD_NAME, result.getFieldNameAndOperation());
    }

    @Test
    public void successBatchDetectSentimentCall() {
        when(mockComprehendClient.batchDetectSentiment(any(BatchDetectSentimentRequest.class)))
                .thenReturn(mockBatchDetectSentimentResult);
        BatchResponse result = test.sendBatchRequest(
                TestConstants.FIELD_LOCATOR_LIST,
                TestConstants.CONTENT_LIST,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient);

        assertEquals(TestConstants.FIELD_LOCATOR_LIST, result.getLocatorList());
    }
}
