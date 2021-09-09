package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.ConfigRetriever;
import com.amazonaws.comprehend.esproxy.lambda.utils.HTTPTransformer;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ComprehendSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.IngestionSerializer;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HTTPTransformer.class})
public class BulkProcessorTest {
    private ComprehendSerializer<JsonNode> ingestionSerializer;
    @Mock
    private AmazonComprehend mockComprehendClient;
    @Mock
    private OpenSearchServiceClient mockESClient;
    @Mock
    private ConfigRetriever mockConfigRetriever;
    @Mock
    private ExecutorService mockExecutorService;

    @Mock
    private Response mockESResponse;
    @Mock
    private StatusLine mockStatusLine;
    @Mock
    private LambdaLogger mockLogger;
    @Mock
    private BatchResponse mockBatchResponse;

    private BulkProcessor test;

    @Before
    public void setup() throws IOException {
        mockStatic(HTTPTransformer.class);
        when(mockESClient.performRequest(any(Request.class))).thenReturn(mockESResponse);
        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockESResponse.getEntity()).thenReturn(new NStringEntity(TestConstants.DUMMY_RESPONSE));
        when(mockConfigRetriever.retrieveStoredConfig()).thenReturn(TestConstants.getListConfigMap());

        ingestionSerializer = new IngestionSerializer(new ObjectMapper());
        test = new BulkProcessor(ingestionSerializer, mockComprehendClient,
                mockESClient, mockConfigRetriever, mockExecutorService);
    }

    @Test
    public void succeedPassThroughWhenPayloadIsEmpty() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.EMPTY_STRING);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.EMPTY_STRING);

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
        verify(mockLogger, times(1))
                .log("Payload or Comprehend config is empty, return pass through requests");
    }

    @Test
    public void succeedPassThroughWhenPayloadDoNotContainIngestionRequest() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_PAYLOAD_NO_INGESTION_REQUEST);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_PAYLOAD_NO_INGESTION_REQUEST);

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
        verify(mockLogger, times(1))
                .log("No ingestion requests detected, return pass through requests");
    }

    @Test
    public void succeedPassThroughWhenPayloadDoNotContainConfigKey() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_PAYLOAD_NO_CONFIG_KEY_WORD);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_PAYLOAD_NO_CONFIG_KEY_WORD);

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
        verify(mockLogger, times(1))
                .log("No config field was detected in the bulk request, return pass through requests");
    }

    @Test
    public void succeedProcessWhenPayloadContainsConfigKey()
            throws ExecutionException, InterruptedException, JSONException {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD);

        List<Future<BatchResponse>> executorResultList = new ArrayList<>();
        Future<BatchResponse> responseObjectFuture1 = mock(Future.class);
        Future<BatchResponse> responseObjectFuture2 = mock(Future.class);
        executorResultList.add(responseObjectFuture1);
        executorResultList.add(responseObjectFuture2);
        List<Callable<BatchResponse>> executorList = new ArrayList<>();

        when(responseObjectFuture1.get()).thenReturn(mockBatchResponse);
        when(responseObjectFuture2.get()).thenReturn(mockBatchResponse);
        when(mockBatchResponse.getLocatorList())
                .thenReturn(Collections.singletonList(new BatchFieldLocator(TestConstants.FIELD_NAME, 1)));
        when(mockBatchResponse.getBatchResultList())
                .thenReturn(Collections.singletonList(new JSONObject(TestConstants.DUMMY_RESPONSE_JSON)));
        when(mockExecutorService.invokeAll(any(executorList.getClass()))).thenReturn(executorResultList);

        test.processRequest(request, mockLogger);
        verify(mockLogger, times(1))
                .log("Ingest Comprehend enriched bulk results to OpenSearchService");
    }

    @Test
    public void succeedProcessWhenSomeResultThrowError()
            throws ExecutionException, InterruptedException, JSONException {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD);

        List<Future<BatchResponse>> executorResultList = new ArrayList<>();
        Future<BatchResponse> responseObjectFuture1 = mock(Future.class);
        Future<BatchResponse> responseObjectFuture2 = mock(Future.class);
        executorResultList.add(responseObjectFuture1);
        executorResultList.add(responseObjectFuture2);
        List<Callable<BatchResponse>> executorList = new ArrayList<>();

        when(responseObjectFuture1.get()).thenReturn(mockBatchResponse);
        when(responseObjectFuture2.get()).thenThrow(new ExecutionException(new Throwable(TestConstants.DUMMY_ERROR_MESSAGE)));
        when(mockBatchResponse.getLocatorList())
                .thenReturn(Collections.singletonList(new BatchFieldLocator(TestConstants.FIELD_NAME, 1)));
        when(mockBatchResponse.getBatchResultList())
                .thenReturn(Collections.singletonList(new JSONObject(TestConstants.DUMMY_RESPONSE_JSON)));
        when(mockExecutorService.invokeAll(any(executorList.getClass()))).thenReturn(executorResultList);

        test.processRequest(request, mockLogger);
        verify(mockLogger, times(1))
                .log("Ingest Comprehend enriched bulk results to OpenSearchService");
    }

    @Test(expected = InternalErrorException.class)
    public void failedProcessWhenSerializerThrowError() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD);

        IngestionSerializer mockBadSerializer = mock(IngestionSerializer.class);
        test = new BulkProcessor(mockBadSerializer, mockComprehendClient,
                mockESClient, mockConfigRetriever, mockExecutorService);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD);
        when(mockBadSerializer.deserialize(anyString()))
                .thenThrow(new InternalErrorException(TestConstants.DUMMY_ERROR_MESSAGE));

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
    }

}
