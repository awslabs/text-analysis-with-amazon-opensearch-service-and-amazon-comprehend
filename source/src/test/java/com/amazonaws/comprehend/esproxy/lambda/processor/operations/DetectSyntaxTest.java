package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectSyntaxRequest;
import com.amazonaws.services.comprehend.model.BatchDetectSyntaxResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetectSyntaxTest {
    @Mock
    private AmazonComprehend mockComprehendClient;

    @Mock
    BatchDetectSyntaxResult mockBatchDetectSyntaxResult;

    private DetectSyntax test = new DetectSyntax();

    @Test
    public void successDetectSyntaxCall() {
        when(mockComprehendClient.detectSyntax(any())).thenReturn(TestConstants.getDetectSyntaxResult());
        SingularResponse result = test.sendSingularRequest(TestConstants.FIELD_NAME,
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient);

        assertEquals(TestConstants.FIELD_NAME, result.getFieldNameAndOperation());
    }

    @Test
    public void successBatchDetectSyntaxCall() {
        when(mockComprehendClient.batchDetectSyntax(any(BatchDetectSyntaxRequest.class)))
                .thenReturn(mockBatchDetectSyntaxResult);
        BatchResponse result = test.sendBatchRequest(
                TestConstants.FIELD_LOCATOR_LIST,
                TestConstants.CONTENT_LIST,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient);

        assertEquals(TestConstants.FIELD_LOCATOR_LIST, result.getLocatorList());
    }
}
