package com.amazonaws.comprehend.esproxy.lambda.utils;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ComprehendSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ConfigSerializer;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HTTPTransformer.class, ConfigSerializer.class})
public class ConfigRetrieverTest {
    @Mock
    private ComprehendSerializer<PreprocessingConfigRequest> mockConfigSerializer;

    @Mock
    private OpenSearchServiceClient mockESClient;

    @Mock
    private Response mockESResponse;

    @Mock
    private StatusLine mockStatusLine;

    private ConfigRetriever test;

    @Before
    public void setup() throws IOException {
        mockStatic(HTTPTransformer.class);
        mockStatic(ConfigSerializer.class);
        when(mockESClient.performRequest(anyString(), anyString())).thenReturn(mockESResponse);
        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockESResponse.getEntity()).thenReturn(new NStringEntity(TestConstants.DUMMY_RESPONSE));

        test = new ConfigRetriever(mockConfigSerializer, mockESClient);
    }

    @Test
    public void succeedRetrieveStoredConfig() {
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(HTTPTransformer.transformHttpEntityToString(any())).thenReturn(TestConstants.CONFIG);
        when(mockConfigSerializer.deserialize(TestConstants.CONFIG)).thenReturn(TestConstants.getConfigRequest());
        when(ConfigSerializer.transformConfigRequestToConfigMap(any())).thenReturn(TestConstants.getConfigMap());
        Map<String, ComprehendConfiguration> configMap = test.retrieveStoredConfig();

        assertEquals(TestConstants.getConfigMap(), configMap);
    }

    @Test
    public void failedToRetrieveStoredConfig() {
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Map<String, ComprehendConfiguration> configMap = test.retrieveStoredConfig();

        verifyZeroInteractions(mockConfigSerializer);
        assertEquals(Collections.EMPTY_MAP, configMap);
    }

    @Test(expected = InternalErrorException.class)
    public void failedToPerformRequest() {
        OpenSearchServiceClient mockBadESClient = mock(OpenSearchServiceClient.class);
        when(mockBadESClient.performRequest(anyString(), anyString())).thenThrow(new InternalErrorException(""));

        test = new ConfigRetriever(mockConfigSerializer, mockBadESClient);
        test.retrieveStoredConfig();
    }

    @Test(expected = InvalidRequestException.class)
    public void failedToDeserializeRetrievedConfig() {
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(HTTPTransformer.transformHttpEntityToString(any())).thenReturn(TestConstants.CONFIG);
        when(mockConfigSerializer.deserialize(TestConstants.CONFIG))
                .thenThrow(new InvalidRequestException(TestConstants.DUMMY_ERROR_MESSAGE));

        test.retrieveStoredConfig();
    }

}
