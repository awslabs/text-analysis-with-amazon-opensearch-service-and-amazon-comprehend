package com.amazonaws.comprehend.esproxy.lambda.processor.callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectSentimentRequest;
import com.amazonaws.services.comprehend.model.BatchDetectSentimentResult;
import com.amazonaws.services.comprehend.model.InternalServerException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BatchOperationCallableTest {
    @Mock
    private AmazonComprehend mockComprehendClient;

    @Mock
    BatchDetectSentimentResult mockBatchDetectSentimentResult;

    private BatchOperationCallable test;

    @Test
    public void successBatchOperationCall() {
        test = new BatchOperationCallable(
                ComprehendOperationEnum.DetectSentiment,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient,
                TestConstants.FIELD_LOCATOR_LIST,
                TestConstants.CONTENT_LIST);
        when(mockComprehendClient.batchDetectSentiment(any(BatchDetectSentimentRequest.class)))
                .thenReturn(mockBatchDetectSentimentResult);

        assertEquals(1, test.call().getLocatorList().size());
    }

    @Test
    public void failedWithAmazonServiceException() throws JSONException {
        InternalServerException internalServerException = new InternalServerException(TestConstants.DUMMY_ERROR_MESSAGE);
        internalServerException.setErrorCode(TestConstants.ERROR_CODE);
        internalServerException.setErrorMessage(CustomerMessage.INTERNAL_ERROR);

        List<BatchFieldLocator> fieldLocatorList = Collections.singletonList(
                new BatchFieldLocator(String.format("%s_%s", TestConstants.FIELD_NAME, ComprehendOperationEnum.DetectSentiment), 1));

        test = new BatchOperationCallable(
                ComprehendOperationEnum.DetectSentiment,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient,
                fieldLocatorList,
                TestConstants.CONTENT_LIST);
        when(mockComprehendClient.batchDetectSentiment(any(BatchDetectSentimentRequest.class)))
                .thenThrow(internalServerException);

        BatchResponse response = test.call();
        List<BatchFieldLocator> locatorList = response.getLocatorList();
        List<JSONObject> resultList = response.getBatchResultList();

        assertEquals(1, locatorList.get(0).getContentRowNum());
        assertEquals(String.format("%s_%s_%s", TestConstants.FIELD_NAME, ComprehendOperationEnum.DetectSentiment, "Error"),
                locatorList.get(0).getFieldNameAndOperation());

        assertEquals(TestConstants.ERROR_CODE, resultList.get(0).get(TestConstants.ERROR_CODE_KEY));
        assertEquals(CustomerMessage.INTERNAL_ERROR, resultList.get(0).get(TestConstants.ERROR_MESSAGE_KEY));
    }

    @Test
    public void failedWithAmazonClientException() throws JSONException {
        AmazonClientException clientException = new AmazonClientException(TestConstants.DUMMY_ERROR_MESSAGE);

        List<BatchFieldLocator> fieldLocatorList = Collections.singletonList(
                new BatchFieldLocator(String.format("%s_%s", TestConstants.FIELD_NAME, ComprehendOperationEnum.DetectSentiment), 1));

        test = new BatchOperationCallable(
                ComprehendOperationEnum.DetectSentiment,
                TestConstants.LANGUAGE_CODE,
                mockComprehendClient,
                fieldLocatorList,
                TestConstants.CONTENT_LIST);
        when(mockComprehendClient.batchDetectSentiment(any(BatchDetectSentimentRequest.class)))
                .thenThrow(clientException);

        BatchResponse<?> response = test.call();
        List<BatchFieldLocator> locatorList = response.getLocatorList();
        List<JSONObject> resultList = response.getBatchResultList();

        assertEquals(1, locatorList.get(0).getContentRowNum());
        assertEquals(String.format("%s_%s_%s", TestConstants.FIELD_NAME, ComprehendOperationEnum.DetectSentiment, "Error"),
                locatorList.get(0).getFieldNameAndOperation());

        assertEquals(Constants.CLIENT_EXCEPTION_ERROR_CODE, resultList.get(0).get(TestConstants.ERROR_CODE_KEY));
        assertEquals(TestConstants.DUMMY_ERROR_MESSAGE, resultList.get(0).get(TestConstants.ERROR_MESSAGE_KEY));
    }

}
