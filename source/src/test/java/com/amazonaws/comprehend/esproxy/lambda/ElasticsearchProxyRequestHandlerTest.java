package com.amazonaws.comprehend.esproxy.lambda;

import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.processor.ElasticsearchProcessor;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.comprehend.esproxy.lambda.utils.HTTPTransformer;
import com.amazonaws.comprehend.esproxy.lambda.modules.ModuleConstants;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.amazonaws.comprehend.esproxy.lambda.utils.RequestIdentifier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestIdentifier.class, HTTPTransformer.class})
public class ElasticsearchProxyRequestHandlerTest {

    @Mock
    private ElasticsearchProcessor mockConfigProcessor;

    @Mock
    private ElasticsearchProcessor mockIndexProcessor;

    @Mock
    private ElasticsearchProcessor mockBulkProcessor;

    @Mock
    private ElasticsearchProcessor mockDefaultProcessor;

    @Mock
    private APIGatewayProxyRequestEvent mockRequestEvent;

    @Mock
    private APIGatewayProxyResponseEvent mockGWResponse;

    @Mock
    private Response mockESResponse;

    @Mock
    private Context mockContext;

    @Mock
    private LambdaLogger mockLogger;

    private Request esRequest;

    private ElasticsearchProxyRequestHandler test;

    @Before
    public void setup() {
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(ElasticsearchProcessor.class)
                        .annotatedWith(Names.named(ModuleConstants.PREPROCESSING_CONFIG_PROCESSOR)).toInstance(mockConfigProcessor);
                bind(ElasticsearchProcessor.class)
                        .annotatedWith(Names.named(ModuleConstants.INDEX_PROCESSOR)).toInstance(mockIndexProcessor);
                bind(ElasticsearchProcessor.class)
                        .annotatedWith(Names.named(ModuleConstants.BULK_PROCESSOR)).toInstance(mockBulkProcessor);
                bind(ElasticsearchProcessor.class)
                        .annotatedWith(Names.named(ModuleConstants.DEFAULT_PROCESSOR)).toInstance(mockDefaultProcessor);
            }
        };
        Injector injector = Guice.createInjector(module);

        mockStatic(RequestIdentifier.class);
        mockStatic(HTTPTransformer.class);
        esRequest = new Request(HttpGet.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        when(HTTPTransformer.apiGatewayRequestToESRequest(mockRequestEvent)).thenReturn(esRequest);
        when(mockContext.getLogger()).thenReturn(mockLogger);

        test = new ElasticsearchProxyRequestHandler(injector);
    }

    @Test
    public void successEnd2endRequests() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isIndexRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isBulkRequest(esRequest)).thenReturn(false);

        when(mockDefaultProcessor.processRequest(esRequest, mockLogger)).thenReturn(mockESResponse);

        test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockConfigProcessor);
        verifyZeroInteractions(mockIndexProcessor);
        verifyZeroInteractions(mockBulkProcessor);
    }

    @Test
    public void successConfigRequests() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(true);

        when(mockConfigProcessor.processRequest(esRequest, mockLogger)).thenReturn(mockESResponse);
        when(HTTPTransformer.esResponseToAPIGatewayResponse(mockESResponse)).thenReturn(mockGWResponse);

        test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockDefaultProcessor);
        verifyZeroInteractions(mockIndexProcessor);
        verifyZeroInteractions(mockBulkProcessor);
    }

    @Test
    public void successIndexRequests() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isMutationRequest(esRequest)).thenReturn(true);
        when(RequestIdentifier.isIndexRequest(esRequest)).thenReturn(true);

        when(mockIndexProcessor.processRequest(esRequest, mockLogger)).thenReturn(mockESResponse);
        when(HTTPTransformer.esResponseToAPIGatewayResponse(mockESResponse)).thenReturn(mockGWResponse);

        test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockDefaultProcessor);
        verifyZeroInteractions(mockConfigProcessor);
        verifyZeroInteractions(mockBulkProcessor);
    }


    // End2end related failures
    @Test
    public void failedElasticsearchClientPerformRequest() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isMutationRequest(esRequest)).thenReturn(false);

        when(mockDefaultProcessor.processRequest(esRequest, mockLogger)).thenThrow(InternalErrorException.class);

        APIGatewayProxyResponseEvent result = test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockConfigProcessor);
        verifyZeroInteractions(mockIndexProcessor);
        verifyZeroInteractions(mockBulkProcessor);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode().intValue());
    }

    @Test
    public void failedTransformEsResponseToAPIGatewayResponse() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isMutationRequest(esRequest)).thenReturn(false);

        when(mockDefaultProcessor.processRequest(esRequest, mockLogger)).thenReturn(mockESResponse);
        when(HTTPTransformer.esResponseToAPIGatewayResponse(mockESResponse))
                .thenThrow(InternalErrorException.class);

        APIGatewayProxyResponseEvent result = test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockConfigProcessor);
        verifyZeroInteractions(mockIndexProcessor);
        verifyZeroInteractions(mockBulkProcessor);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode().intValue());
    }

    @Test
    public void failedProcessConfigRequest() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(true);
        when(mockConfigProcessor.processRequest(esRequest, mockLogger)).thenThrow(InvalidRequestException.class);

        APIGatewayProxyResponseEvent result = test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockDefaultProcessor);
        verifyZeroInteractions(mockIndexProcessor);
        verifyZeroInteractions(mockBulkProcessor);
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getStatusCode().intValue());
    }

    @Test
    public void failedProcessIndexRequest() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isMutationRequest(esRequest)).thenReturn(true);
        when(RequestIdentifier.isIndexRequest(esRequest)).thenReturn(true);

        when(mockIndexProcessor.processRequest(esRequest, mockLogger)).thenThrow(InternalErrorException.class);

        APIGatewayProxyResponseEvent result = test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockConfigProcessor);
        verifyZeroInteractions(mockBulkProcessor);
        verifyZeroInteractions(mockDefaultProcessor);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode().intValue());
    }

    @Test
    public void failedProcessBulkRequest() {
        when(RequestIdentifier.isConfigRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isMutationRequest(esRequest)).thenReturn(true);
        when(RequestIdentifier.isIndexRequest(esRequest)).thenReturn(false);
        when(RequestIdentifier.isBulkRequest(esRequest)).thenReturn(true);

        when(mockBulkProcessor.processRequest(esRequest, mockLogger)).thenThrow(InternalErrorException.class);

        APIGatewayProxyResponseEvent result = test.handleRequest(mockRequestEvent, mockContext);
        verifyZeroInteractions(mockConfigProcessor);
        verifyZeroInteractions(mockIndexProcessor);
        verifyZeroInteractions(mockDefaultProcessor);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode().intValue());
    }

}
