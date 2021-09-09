package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
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
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HTTPTransformer.class})
public class IndexProcessorTest {
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
    private SingularResponse mockSingularResponse;

    @Mock
    private JSONObject mockLanguageResult;

    private IndexProcessor test;

    @Before
    public void setup() throws IOException {
        mockStatic(HTTPTransformer.class);
        when(mockESClient.performRequest(any(Request.class))).thenReturn(mockESResponse);
        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockESResponse.getEntity()).thenReturn(new NStringEntity(TestConstants.DUMMY_RESPONSE));
        when(mockConfigRetriever.retrieveStoredConfig()).thenReturn(TestConstants.getListConfigMap());

        ingestionSerializer = new IngestionSerializer(new ObjectMapper());
        test = new IndexProcessor(ingestionSerializer, mockComprehendClient,
                mockESClient, mockConfigRetriever, mockExecutorService);
    }

    @Test
    public void succeedPassThroughWhenPayloadIsEmpty() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.EMPTY_STRING);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.EMPTY_STRING);

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
    }

    @Test
    public void succeedPassThroughWhenPayloadDoNotContainConfigKey() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NO_KEYWORD);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NO_KEYWORD);

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
    }

    @Test
    public void succeedProcessWhenPayloadContainsConfigKeyOnTheFirstLevel()
            throws ExecutionException, InterruptedException {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_INGESTION_PAYLOAD);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_INGESTION_PAYLOAD);

        List<Future<SingularResponse>> executorResultList = new ArrayList<>();
        Future<SingularResponse> responseObjectFuture = mock(Future.class);
        executorResultList.add(responseObjectFuture);
        List<Callable<SingularResponse>> executorList = new ArrayList<>();

        when(responseObjectFuture.get()).thenReturn(mockSingularResponse);
        when(mockSingularResponse.getFieldNameAndOperation()).thenReturn(TestConstants.FIELD_NAME);
        when(mockSingularResponse.getComprehendResult()).thenReturn(mockLanguageResult);
        when(mockExecutorService.invokeAll(any(executorList.getClass()))).thenReturn(executorResultList);

        test.processRequest(request, mockLogger);
        verify(mockLogger, times(1)).log("Ingest Comprehend enriched results to OpenSearchService");
    }

    @Test
    public void succeedProcessWhenPayloadContainsNestedConfigKey()
            throws ExecutionException, InterruptedException {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NESTED);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NESTED);

        List<Future<SingularResponse>> executorResultList = new ArrayList<>();
        Future<SingularResponse> responseObjectFuture1 = mock(Future.class);
        Future<SingularResponse> responseObjectFuture2 = mock(Future.class);
        executorResultList.add(responseObjectFuture1);
        executorResultList.add(responseObjectFuture2);
        List<Callable<SingularResponse>> executorList = new ArrayList<>();

        when(responseObjectFuture1.get()).thenReturn(mockSingularResponse);
        when(responseObjectFuture2.get()).thenReturn(mockSingularResponse);
        when(mockSingularResponse.getFieldNameAndOperation()).thenReturn(TestConstants.FIELD_NAME);
        when(mockSingularResponse.getComprehendResult()).thenReturn(mockLanguageResult);
        when(mockExecutorService.invokeAll(any(executorList.getClass()))).thenReturn(executorResultList);

        test.processRequest(request, mockLogger);
        verify(mockLogger, times(1)).log("Ingest Comprehend enriched results to OpenSearchService");
    }

    @Test
    public void succeedProcessWhenSomeResultThrowError()
            throws ExecutionException, InterruptedException {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NESTED);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NESTED);

        List<Future<SingularResponse>> executorResultList = new ArrayList<>();
        Future<SingularResponse> responseObjectFuture1 = mock(Future.class);
        Future<SingularResponse> responseObjectFuture2 = mock(Future.class);
        executorResultList.add(responseObjectFuture1);
        executorResultList.add(responseObjectFuture2);
        List<Callable<SingularResponse>> executorList = new ArrayList<>();

        when(responseObjectFuture1.get()).thenReturn(mockSingularResponse);
        when(responseObjectFuture2.get())
                .thenThrow(new ExecutionException(new Throwable(TestConstants.DUMMY_ERROR_MESSAGE)));
        when(mockSingularResponse.getFieldNameAndOperation()).thenReturn(TestConstants.FIELD_NAME);
        when(mockSingularResponse.getComprehendResult()).thenReturn(mockLanguageResult);
        when(mockExecutorService.invokeAll(any(executorList.getClass()))).thenReturn(executorResultList);

        test.processRequest(request, mockLogger);
        verify(mockLogger, times(1)).log("Ingest Comprehend enriched results to OpenSearchService");
    }

    @Test(expected = InternalErrorException.class)
    public void failedProcessWhenSerializerThrowError() {
        Request request = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        request.setJsonEntity(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NESTED);

        IngestionSerializer mockBadSerializer = mock(IngestionSerializer.class);
        test = new IndexProcessor(mockBadSerializer, mockComprehendClient,
                mockESClient, mockConfigRetriever, mockExecutorService);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class)))
                .thenReturn(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NESTED);
        when(mockBadSerializer.deserialize(anyString()))
                .thenThrow(new InternalErrorException(TestConstants.DUMMY_ERROR_MESSAGE));

        test.processRequest(request, mockLogger);
        verifyZeroInteractions(mockExecutorService);
    }

}
