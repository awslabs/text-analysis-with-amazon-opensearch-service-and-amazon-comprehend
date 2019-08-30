package com.amazonaws.comprehend.esproxy.lambda.utils;

import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaHelper;
import com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaUploader;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.client.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KibanaUploaderTest {
    @Mock
    private ElasticsearchClient mockESClient;

    @Mock
    private LambdaLogger mockLogger;

    private ExecutorService executorService;

    private KibanaUploader test;

    @Before
    public void setup() {
        executorService = Executors.newFixedThreadPool(Constants.MAX_THREAD);
        test = new KibanaUploader(mockESClient, executorService);
    }

    @Test
    public void happyTestWithConfigList() {
        test.uploadKibanaDashboard(TestConstants.getListConfigMap(), mockLogger);

        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_MARKDOWN_LOG);
        verify(mockLogger, times(2)).log(TestConstants.UPLOAD_INDEX_PATTERN_LOG);
        verify(mockLogger, times(2)).log(TestConstants.UPLOAD_VISUALIZATION_LOG);
        verify(mockLogger, times(2)).log(TestConstants.UPLOAD_DASHBOARD_LOG);
    }

    @Test
    public void happyMappingTestWithConfigList() {
        StatusLine mockStatusLine = mock(StatusLine.class);
        Response mockESResponse = mock(Response.class);

        when(mockESClient.performRequest(eq(HttpGet.METHOD_NAME), anyString())).thenReturn(mockESResponse);
        when(mockESResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(TestConstants.STATUS_CODE_OK);

        test.uploadMapping(TestConstants.getListConfigMap(), mockLogger);

        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_MAPPING_LOG);
        verify(mockESClient, times(1))
                .performRequest(HttpPut.METHOD_NAME, String.format("/%s/_mapping/_doc", TestConstants.INDEX_NAME),
                        TestConstants.MAPPING_JSON.replaceAll(KibanaHelper.FIELD_NAME_KEY, TestConstants.FIELD_NAME));
        verify(mockESClient, times(1))
                .performRequest(HttpPut.METHOD_NAME, String.format("/%s/_mapping/_doc", TestConstants.NEW_INDEX_NAME),
                        TestConstants.MAPPING_JSON.replaceAll(KibanaHelper.FIELD_NAME_KEY, TestConstants.NEW_FIELD_NAME));
    }

    // Since the dashboard upload is best effort
    // we don't fail the config request even if dashboard upload fails
    @Test
    public void succeedWhenClientFailed() {
        when(mockESClient.performRequest(anyString(), anyString(), anyString(), anyMap()))
                .thenThrow(InternalErrorException.class);
        test.uploadKibanaDashboard(TestConstants.getConfigMap(), mockLogger);

        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_MARKDOWN_LOG);
        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_INDEX_PATTERN_LOG);
        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_VISUALIZATION_LOG);
        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_DASHBOARD_LOG);
        verify(mockLogger, times(1)).log(TestConstants.UPLOAD_FAILED_LOG);
    }
}
