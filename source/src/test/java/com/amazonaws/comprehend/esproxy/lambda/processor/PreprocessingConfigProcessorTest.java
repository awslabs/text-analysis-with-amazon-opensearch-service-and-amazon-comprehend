package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.amazonaws.comprehend.esproxy.lambda.utils.ConfigRetriever;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.comprehend.esproxy.lambda.utils.HTTPTransformer;
import com.amazonaws.comprehend.esproxy.lambda.utils.RequestIdentifier;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaUploader;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ComprehendSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ConfigSerializer;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestIdentifier.class, HTTPTransformer.class, ConfigSerializer.class})
public class PreprocessingConfigProcessorTest {
    @Mock
    private ComprehendSerializer<PreprocessingConfigRequest> mockConfigSerializer;

    @Mock
    private ElasticsearchClient mockESClient;

    @Mock
    private ConfigRetriever mockConfigRetriever;

    @Mock
    private KibanaUploader mockKibanaUploader;

    @Mock
    private Response mockESResponse;

    @Mock
    private StatusLine mockStatusLine;

    @Mock
    private LambdaLogger mockLogger;

    private PreprocessingConfigProcessor test;

    @Before
    public void setup() throws IOException {
        mockStatic(RequestIdentifier.class);
        mockStatic(HTTPTransformer.class);
        mockStatic(ConfigSerializer.class);
        when(mockESClient.performRequest(anyString(), anyString(), anyString())).thenReturn(mockESResponse);
        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockESResponse.getEntity()).thenReturn(new NStringEntity(TestConstants.DUMMY_RESPONSE));

        test = new PreprocessingConfigProcessor(mockConfigSerializer, mockESClient, mockConfigRetriever, mockKibanaUploader);
    }

    @Test
    public void succeedPutWhenConfigFormatIsCorrect() throws InvalidRequestException {
        Request inputESRequest = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        inputESRequest.setJsonEntity(TestConstants.CONFIG);

        when(RequestIdentifier.isMutationRequest(inputESRequest)).thenReturn(true);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.CONFIG);
        when(mockConfigSerializer.deserialize(anyString())).thenReturn(TestConstants.getConfigRequest());
        when(ConfigSerializer.transformConfigRequestToConfigMap(any())).thenReturn(TestConstants.getConfigMap());
        when(RequestIdentifier.isConfigUpdateRequest(inputESRequest)).thenReturn(false);
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        test.processRequest(inputESRequest, mockLogger);
        verify(mockConfigSerializer, times(0)).serialize(any());
    }

    @Test
    public void succeedUpdateWhenConfigFormatIsCorrect() throws InvalidRequestException {
        Request inputESRequest = new Request(HttpPut.METHOD_NAME,
                String.format("%s/%s", TestConstants.DUMMY_ENDPOINT, Constants.UPDATE_REQUEST));
        inputESRequest.setJsonEntity(TestConstants.NEW_CONFIG);

        when(RequestIdentifier.isMutationRequest(inputESRequest)).thenReturn(true);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.NEW_CONFIG);
        when(mockConfigSerializer.deserialize(anyString())).thenReturn(TestConstants.getNewConfigRequest());
        when(ConfigSerializer.transformConfigRequestToConfigMap(any(PreprocessingConfigRequest.class)))
                .thenReturn(TestConstants.getNewConfigMap());
        when(RequestIdentifier.isConfigUpdateRequest(inputESRequest)).thenReturn(true);

        when(mockConfigRetriever.retrieveStoredConfig()).thenReturn(TestConstants.getConfigMap());
        when(ConfigSerializer.transformConfigMapToConfigRequest(anyMap()))
                .thenReturn(TestConstants.getListConfigRequest());
        when(mockConfigSerializer.serialize(any())).thenReturn(TestConstants.CONFIG_LIST);
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        test.processRequest(inputESRequest, mockLogger);
        verify(mockConfigRetriever, times(1)).retrieveStoredConfig();
        verify(mockConfigSerializer, times(1)).serialize(any());
    }

    @Test
    public void succeedUpdateWhenNoConfigWasRetrieved() throws InvalidRequestException {
        Request inputESRequest = new Request(HttpPut.METHOD_NAME,
                String.format("%s/%s", TestConstants.DUMMY_ENDPOINT, Constants.UPDATE_REQUEST));
        inputESRequest.setJsonEntity(TestConstants.NEW_CONFIG);

        when(RequestIdentifier.isMutationRequest(inputESRequest)).thenReturn(true);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.NEW_CONFIG);
        when(mockConfigSerializer.deserialize(anyString())).thenReturn(TestConstants.getNewConfigRequest());
        when(ConfigSerializer.transformConfigRequestToConfigMap(any(PreprocessingConfigRequest.class)))
                .thenReturn(TestConstants.getConfigMap());
        when(RequestIdentifier.isConfigUpdateRequest(inputESRequest)).thenReturn(true);

        when(mockConfigRetriever.retrieveStoredConfig()).thenReturn(Collections.emptyMap());
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        test.processRequest(inputESRequest, mockLogger);
        verify(mockConfigSerializer, times(0)).serialize(any());
    }

    @Test(expected = InternalErrorException.class)
    public void failWhenConfigSaveFailed() {
        ElasticsearchClient mockBadClient = mock(ElasticsearchClient.class);
        when(mockBadClient.performRequest(anyString(), anyString(), anyString())).thenThrow(InternalErrorException.class);
        test = new PreprocessingConfigProcessor(mockConfigSerializer, mockBadClient, mockConfigRetriever, mockKibanaUploader);

        Request inputESRequest = new Request(HttpPut.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        inputESRequest.setJsonEntity(TestConstants.CONFIG);

        when(RequestIdentifier.isMutationRequest(inputESRequest)).thenReturn(true);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.CONFIG);
        when(mockConfigSerializer.deserialize(anyString())).thenReturn(TestConstants.getConfigRequest());
        when(ConfigSerializer.transformConfigRequestToConfigMap(any(PreprocessingConfigRequest.class)))
                .thenReturn(TestConstants.getConfigMap());
        when(RequestIdentifier.isConfigUpdateRequest(inputESRequest)).thenReturn(false);

        test.processRequest(inputESRequest, mockLogger);
        verify(mockConfigRetriever, times(0)).retrieveStoredConfig();
        verify(mockConfigSerializer, times(0)).serialize(any());
    }

    @Test(expected = InternalErrorException.class)
    public void failWhenClientUnableToRetrieveOldConfig() {
        Request inputESRequest = new Request(HttpPut.METHOD_NAME,
                String.format("%s/%s", TestConstants.DUMMY_ENDPOINT, Constants.UPDATE_REQUEST));
        inputESRequest.setJsonEntity(TestConstants.NEW_CONFIG);

        when(RequestIdentifier.isMutationRequest(inputESRequest)).thenReturn(true);
        when(HTTPTransformer.transformHttpEntityToString(any(HttpEntity.class))).thenReturn(TestConstants.NEW_CONFIG);
        when(mockConfigSerializer.deserialize(anyString())).thenReturn(TestConstants.getNewConfigRequest());
        when(ConfigSerializer.transformConfigRequestToConfigMap(any(PreprocessingConfigRequest.class)))
                .thenReturn(TestConstants.getNewConfigMap());
        when(RequestIdentifier.isConfigUpdateRequest(inputESRequest)).thenReturn(true);
        when(mockConfigRetriever.retrieveStoredConfig()).thenThrow(InternalErrorException.class);

        test.processRequest(inputESRequest, mockLogger);
        verify(mockConfigSerializer, times(0)).serialize(any());
        verify(mockESClient, times(0)).performRequest(any());
    }

    @Test
    public void successPassThroughConfig() throws InvalidRequestException {
        Request inputESRequest = new Request(HttpGet.METHOD_NAME, TestConstants.DUMMY_ENDPOINT);
        inputESRequest.setJsonEntity(TestConstants.CONFIG);
        when(RequestIdentifier.isMutationRequest(inputESRequest)).thenReturn(false);
        when(mockESClient.performRequest(any(Request.class))).thenReturn(mockESResponse);

        test.processRequest(inputESRequest, mockLogger);
        verify(mockConfigSerializer, times(0)).deserialize(any());
        verify(mockConfigRetriever, times(0)).retrieveStoredConfig();
        verify(mockConfigSerializer, times(0)).serialize(any());
    }

}

