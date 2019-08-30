package com.amazonaws.comprehend.esproxy.lambda.utils;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntityUtils.class})
public class HTTPTransformerTest {
    @Mock
    Response mockESResponse;

    @Mock
    APIGatewayProxyRequestEvent mockGWRequest;

    @Test
    public void correctRequestTransformFromGWToES() throws IOException {
        when(mockGWRequest.getHttpMethod()).thenReturn(TestConstants.GET_METHOD);
        when(mockGWRequest.getPath()).thenReturn(Constants.CONFIG_PATH);
        when(mockGWRequest.getBody()).thenReturn(TestConstants.DUMMY_MESSAGE);

        Request esRequest = HTTPTransformer.apiGatewayRequestToESRequest(mockGWRequest);

        assertEquals(TestConstants.GET_METHOD, esRequest.getMethod());
        assertEquals(Constants.CONFIG_PATH, esRequest.getEndpoint());
        assertEquals(TestConstants.DUMMY_MESSAGE, EntityUtils.toString(esRequest.getEntity()));
    }

    @Test
    public void correctRequestTransformFromESToGW() throws IOException {
        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(TestConstants.STATUS_CODE_OK);
        when(mockESResponse.getEntity()).thenReturn(new NStringEntity(TestConstants.DUMMY_MESSAGE));

        APIGatewayProxyResponseEvent apiGWResponse = HTTPTransformer.esResponseToAPIGatewayResponse(mockESResponse);

        assertEquals(TestConstants.STATUS_CODE_OK, apiGWResponse.getStatusCode().intValue());
        assertEquals(TestConstants.DUMMY_MESSAGE, apiGWResponse.getBody());
    }

    @Test
    public void correctRequestTransformFromGWToESWithEmptyBody() {
        when(mockGWRequest.getHttpMethod()).thenReturn(TestConstants.GET_METHOD);
        when(mockGWRequest.getPath()).thenReturn(Constants.CONFIG_PATH);
        when(mockGWRequest.getBody()).thenReturn(null);

        Request esRequest = HTTPTransformer.apiGatewayRequestToESRequest(mockGWRequest);

        assertEquals(TestConstants.GET_METHOD, esRequest.getMethod());
        assertEquals(Constants.CONFIG_PATH, esRequest.getEndpoint());
        assertNull(esRequest.getEntity());
    }

    @Test
    public void correctRequestTransformFromESToGWWithEmptyBody() {
        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(TestConstants.STATUS_CODE_OK);
        when(mockESResponse.getEntity()).thenReturn(null);

        APIGatewayProxyResponseEvent apiGWResponse = HTTPTransformer.esResponseToAPIGatewayResponse(mockESResponse);

        assertEquals(TestConstants.STATUS_CODE_OK, apiGWResponse.getStatusCode().intValue());
        assertNull(apiGWResponse.getBody());
    }

    @Test
    public void correctTransformFromHttpEntityToString()
            throws InternalErrorException, UnsupportedEncodingException {
        HttpEntity entity = new NStringEntity(TestConstants.DUMMY_MESSAGE);
        String result = HTTPTransformer.transformHttpEntityToString(entity);
        assertEquals(TestConstants.DUMMY_MESSAGE, result);
    }

    @Test
    public void failedTransformFromHttpEntityToStringEntityNull() {
        try {
            HTTPTransformer.transformHttpEntityToString(null);
        } catch (InternalErrorException e) {
            Assert.assertEquals(CustomerMessage.INPUT_NULL_OR_EMPTY_ERROR, e.getMessage());
        }
    }

    @Test
    public void failedTransformFromHttpEntityToString() throws IOException {
        mockStatic(EntityUtils.class);
        when(EntityUtils.toString(any(HttpEntity.class))).thenThrow(IOException.class);
        HttpEntity entity = new NStringEntity(TestConstants.DUMMY_MESSAGE);

        try {
            HTTPTransformer.transformHttpEntityToString(entity);
        } catch (InternalErrorException e) {
            assertEquals(CustomerMessage.INTERNAL_ERROR, e.getMessage());
        }
    }

}
