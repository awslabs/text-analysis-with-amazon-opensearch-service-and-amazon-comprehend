package com.amazonaws.comprehend.esproxy.lambda.processor.callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.InternalServerException;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingularOperationCallableTest {
    @Mock
    private AmazonComprehend mockComprehendClient;

    private SingularOperationCallable test;

    @Test
    public void successSingularOperationCall() {
        test = new SingularOperationCallable(TestConstants.FIELD_NAME,
                ComprehendOperationEnum.DetectSentiment,
                TestConstants.LANGUAGE_CODE,
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT,
                mockComprehendClient);
        when(mockComprehendClient.detectSentiment(any())).thenReturn(TestConstants.getDetectSentimentResult());

        assertEquals(TestConstants.FIELD_NAME, test.call().getFieldNameAndOperation());
    }

    @Test
    public void failedWithAmazonServiceException() throws JSONException {
        InternalServerException internalServerException = new InternalServerException(TestConstants.DUMMY_ERROR_MESSAGE);
        internalServerException.setErrorCode(TestConstants.ERROR_CODE);
        internalServerException.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        internalServerException.setRequestId(TestConstants.DUMMY_REQUEST_ID);
        internalServerException.setServiceName(ComprehendOperationEnum.DetectSentiment.toString());
        internalServerException.setErrorMessage(CustomerMessage.INTERNAL_ERROR);

        test = new SingularOperationCallable(TestConstants.FIELD_NAME,
                ComprehendOperationEnum.DetectDominantLanguage,
                TestConstants.LANGUAGE_CODE,
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT,
                mockComprehendClient);
        when(mockComprehendClient.detectDominantLanguage(any()))
                .thenThrow(internalServerException);

        SingularResponse response = test.call();
        JSONObject result = response.getComprehendResult();

        assertEquals(String.format("%s_Error", TestConstants.FIELD_NAME), response.getFieldNameAndOperation());
        assertEquals(TestConstants.ERROR_CODE, result.get(TestConstants.ERROR_CODE_KEY));
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.get(TestConstants.STATUS_CODE_KEY));
        assertEquals(TestConstants.DUMMY_REQUEST_ID, result.get(TestConstants.REQUEST_ID_KEY));
        assertEquals(CustomerMessage.INTERNAL_ERROR, result.get(TestConstants.ERROR_MESSAGE_KEY));
    }

    @Test
    public void failedWithAmazonClientException() throws JSONException {
        AmazonClientException clientException = new AmazonClientException(TestConstants.DUMMY_ERROR_MESSAGE);

        test = new SingularOperationCallable(TestConstants.FIELD_NAME,
                ComprehendOperationEnum.DetectDominantLanguage,
                TestConstants.LANGUAGE_CODE,
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT,
                mockComprehendClient);
        when(mockComprehendClient.detectDominantLanguage(any()))
                .thenThrow(clientException);

        SingularResponse response = test.call();
        JSONObject result = response.getComprehendResult();

        assertEquals(String.format("%s_Error", TestConstants.FIELD_NAME), response.getFieldNameAndOperation());
        assertEquals(TestConstants.DUMMY_ERROR_MESSAGE, result.get(TestConstants.ERROR_MESSAGE_KEY));
        assertEquals(Constants.CLIENT_EXCEPTION_ERROR_CODE, result.get(TestConstants.ERROR_CODE_KEY));
    }
}
