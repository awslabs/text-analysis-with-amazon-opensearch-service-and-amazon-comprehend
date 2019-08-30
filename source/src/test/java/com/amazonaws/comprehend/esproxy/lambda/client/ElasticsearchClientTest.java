package com.amazonaws.comprehend.esproxy.lambda.client;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchClientTest {
    @Mock
    RestClient mockRestClient;

    @Mock
    Response mockResponse;

    private final Request esRequest = new Request(HttpGet.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);

    private ElasticsearchClient test;

    @Before
    public void setup() throws IOException {
        test = new ElasticsearchClient(mockRestClient);
        when(mockRestClient.performRequest(esRequest)).thenReturn(mockResponse);
    }

    // Test performRequest(Request request)
    @Test
    public void succeedPerformBasicRequest() {
        test.performRequest(esRequest);
    }

    // Test performRequest(Request request, String endpoint)
    @Test
    public void succeedWithNewEndpoint() {
        test.performRequest(esRequest, TestConstants.DUMMY_ENDPOINT);
    }

    // Test performRequest(String method, String endpoint)
    @Test
    public void succeedWithMethodEndpoint() {
        test.performRequest(esRequest, TestConstants.DUMMY_ENDPOINT);
    }

    // Test performRequest(String method, String endpoint, String payload)
    @Test
    public void succeedWithMethodEndpointPayload() {
        test.performRequest(TestConstants.DUMMY_METHOD, TestConstants.DUMMY_ENDPOINT, TestConstants.DUMMY_PAYLOAD);
    }

    // Test performRequest(String method, String endpoint, String payload, Map<String, String> headerMap)
    @Test
    public void succeedWithMethodEndpointPayloadHeaders() {
        test.performRequest(TestConstants.DUMMY_METHOD, TestConstants.DUMMY_ENDPOINT,
                TestConstants.DUMMY_PAYLOAD, TestConstants.DUMMY_HEADER_MAP);
    }

    @Test
    public void succeedPerformBasicRequestWithResponseException() throws IOException {
        when(mockRestClient.performRequest(esRequest)).thenThrow(ResponseException.class);
        test.performRequest(esRequest);
    }

    @Test
    public void failedPerformBasicRequestWithIOException() throws IOException {
        when(mockRestClient.performRequest(esRequest)).thenThrow(IOException.class);

        try {
            test.performRequest(esRequest);
        } catch (InternalErrorException e) {
            Assert.assertEquals(CustomerMessage.INTERNAL_ERROR, e.getMessage());
        }
    }

}
