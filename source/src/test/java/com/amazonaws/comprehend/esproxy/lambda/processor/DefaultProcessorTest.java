package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.client.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultProcessorTest {
    @Mock
    private OpenSearchServiceClient mockESClient;

    @Mock
    private LambdaLogger mockLogger;

    private DefaultProcessor test;

    @Before
    public void setup() {
        test = new DefaultProcessor(mockESClient);
    }

    @Test
    public void succeedProcessRequest() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        test.processRequest(request, mockLogger);
    }

    @Test(expected = InternalErrorException.class)
    public void failedProcessRequest() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);

        when(mockESClient.performRequest(any())).thenThrow(new InternalErrorException(TestConstants.DUMMY_ERROR_MESSAGE));
        test.processRequest(request, mockLogger);
    }

}
