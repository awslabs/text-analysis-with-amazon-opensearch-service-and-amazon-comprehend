package com.amazonaws.comprehend.esproxy.lambda.utils;

import org.elasticsearch.client.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RequestIdentifierTest {
    private Request esRequest;

    @Test
    public void succeedIsConfigRequestWithConfigPath() {
        esRequest = new Request(TestConstants.PUT_METHOD, Constants.CONFIG_PATH_EXPOSED_TO_CUSTOMER);
        boolean result = RequestIdentifier.isConfigRequest(esRequest);
        assertTrue(result);
    }

    @Test
    public void succeedIsConfigRequestWithConfigUpdatePath() {
        esRequest = new Request(TestConstants.POST_METHOD,
                String.format("%s/%s", Constants.CONFIG_PATH_EXPOSED_TO_CUSTOMER, Constants.UPDATE_REQUEST));
        boolean result = RequestIdentifier.isConfigRequest(esRequest);
        assertTrue(result);
    }

    @Test
    public void failedIsConfigRequestWithWrongPath() {
        esRequest = new Request(TestConstants.PUT_METHOD, TestConstants.WRONG_PATH);
        boolean result = RequestIdentifier.isConfigRequest(esRequest);
        assertFalse(result);
    }

    @Test
    public void succeedIsCreateOrUpdateRequestWithCorrectMethod() {
        esRequest = new Request(TestConstants.POST_METHOD,
                String.format("%s/%s", Constants.CONFIG_PATH_EXPOSED_TO_CUSTOMER, Constants.UPDATE_REQUEST));
        boolean result = RequestIdentifier.isMutationRequest(esRequest);
        assertTrue(result);
    }

    @Test
    public void failedIsCreateOrUpdateRequestWithWrong_method() {
        esRequest = new Request(TestConstants.GET_METHOD, Constants.CONFIG_PATH_EXPOSED_TO_CUSTOMER);
        boolean result = RequestIdentifier.isMutationRequest(esRequest);
        assertFalse(result);
    }

    @Test
    public void succeedIsIndexRequestWithCorrectIndexPattern() {
        esRequest = new Request(TestConstants.POST_METHOD, TestConstants.CORRECT_INDEX_PATH);
        boolean result = RequestIdentifier.isIndexRequest(esRequest);
        assertTrue(result);
    }

    @Test
    public void failedIsIndexRequestWithWrongPath() {
        esRequest = new Request(TestConstants.PUT_METHOD, TestConstants.WRONG_INDEX_PATH);
        boolean result = RequestIdentifier.isIndexRequest(esRequest);
        assertFalse(result);
    }

    @Test
    public void failedIsIndexRequestWithImproperIndex() {
        esRequest = new Request(TestConstants.PUT_METHOD, TestConstants.WRONG_INDEX_PATTERN);
        boolean result = RequestIdentifier.isIndexRequest(esRequest);
        assertFalse(result);
    }

    @Test
    public void succeedIsBulkRequestWithCorrectBulkPattern() {
        esRequest = new Request(TestConstants.POST_METHOD, Constants.BULK_REQUEST);
        boolean result = RequestIdentifier.isBulkRequest(esRequest);
        assertTrue(result);
    }


    @Test
    public void failedIsBulkRequestWithWrongPattern() {
        esRequest = new Request(TestConstants.PUT_METHOD, TestConstants.CORRECT_INDEX_PATH);
        boolean result = RequestIdentifier.isBulkRequest(esRequest);
        assertFalse(result);
    }

    @Test
    public void succeedGetBulkIndexNameWithIndexBulkPattern() {
        Optional<String> result = RequestIdentifier.getBulkIndexName(TestConstants.BULK_INDEX_PAYLOAD);
        assertTrue(result.isPresent());
        assertEquals(TestConstants.INDEX_NAME, result.get());
    }

    @Test
    public void succeedGetBulkIndexNameWithCreateBulkPattern() {
        Optional<String> result = RequestIdentifier.getBulkIndexName(TestConstants.BULK_CREATE_PAYLOAD);
        assertTrue(result.isPresent());
        assertEquals(TestConstants.INDEX_NAME, result.get());
    }

    @Test
    public void failedGetBulkIndexNameWithWrongPattern() {
        Optional<String> result = RequestIdentifier.getBulkIndexName(TestConstants.WRONG_BULK_INDEX_PAYLOAD);
        assertFalse(result.isPresent());
    }

    @Test
    public void failedGetBulkIndexNameWithNoIndexKey() {
        Optional<String> result = RequestIdentifier.getBulkIndexName(TestConstants.BULK_PAYLOAD_NO_INDEX_KEY);
        assertFalse(result.isPresent());
    }

}
