package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.BulkPayload;
import com.amazonaws.comprehend.esproxy.lambda.model.BulkRequest;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.callable.BatchOperationCallable;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.ConfigRetriever;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.comprehend.esproxy.lambda.utils.HTTPTransformer;
import com.amazonaws.comprehend.esproxy.lambda.utils.RequestIdentifier;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ComprehendSerializer;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Processor to process Comprehend Elasticsearch Bulk requests
 */
@RequiredArgsConstructor
public class BulkProcessor implements ElasticsearchProcessor {
    @NonNull
    private final ComprehendSerializer<JsonNode> ingestionSerializer;

    @NonNull
    private final AmazonComprehend comprehendClient;

    @NonNull
    private final ElasticsearchClient esClient;

    @NonNull
    private final ConfigRetriever configRetriever;

    @NonNull
    private final ExecutorService executorService;

    /**
     * Process Comprehend Elasticsearch Bulk requests
     *
     * @param request The received Elasticsearch client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return Elasticsearch service response
     */
    @Override
    public Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger) {
        logger.log("Bulk requests detected");
        String payloadStr = HTTPTransformer.transformHttpEntityToString(request.getEntity());
        Map<String, ComprehendConfiguration> configMap = configRetriever.retrieveStoredConfig();

        if (Strings.isNullOrEmpty(payloadStr) || configMap.isEmpty()) {
            logger.log("Payload or Comprehend config is empty, return pass through requests");
            return esClient.performRequest(request);
        }
        // Transfer the payload to array list, split by lineSeparator
        List<String> payloadList = Arrays.asList(payloadStr.split(System.lineSeparator()));

        // Stores where the ingestion Payload was detected: Map<contentRowNum, ingestionPayload>
        Map<Integer, BulkPayload> payloadMap = getIngestionPayloadMap(payloadList);
        if (payloadMap.isEmpty()) {
            logger.log("No ingestion requests detected, return pass through requests");
            return esClient.performRequest(request);
        }

        // The batchObject list that needs to be comprehend
        List<BulkRequest> bulkRequestList = getBatchObjectList(payloadMap, configMap);
        if (bulkRequestList.isEmpty()) {
            logger.log("No config field was detected in the bulk request, return pass through requests");
            return esClient.performRequest(request);
        }

        // Create the batch requests list
        List<Callable<BatchResponse>> callableList = createCallableList(bulkRequestList);
        try {
            List<Future<BatchResponse>> executionResult = executorService.invokeAll(callableList,
                    Constants.BULK_EXECUTOR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            for (Future<BatchResponse> result : executionResult) {
                try {
                    // For each executionResult, attach the comprehend response
                    attachComprehendResponse(payloadList, result, logger);
                    logger.log("Extended original payload with Comprehend result");
                } catch (RuntimeException e) {
                    throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR);
                }
            }
            // Send the enriched request to ES
            Request transformedRequest = new Request(request.getMethod(), request.getEndpoint());
            transformedRequest.setJsonEntity(String.join(System.lineSeparator(), payloadList) + System.lineSeparator());

            logger.log("Ingest Comprehend enriched bulk results to Elasticsearch");
            Response response = esClient.performRequest(transformedRequest);

            logger.log(String.format("Response of bulk request: statusCode = %d, reasonPhrase = %s",
                                     response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));

            return response;
        } catch (InterruptedException | JSONException e) {
            logger.log("InternalErrorException happened when trying to process bulk request. " + e);
            throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
        }

    }

    /**
     * Find the ingestion requests in the bulk, return contentRowNum and ingestionPayload
     * Payload presents on the next line of the action
     * { "index" : { "_index" : "test", "_type" : "_doc", "_id" : "1" } }
     * { "field1" : "value1" }
     * IndexName "test" and ingestion payload {"field1":"value1"} are stored in BulkPayload
     *
     * @param payloadList The bulk payload array list
     * @return Where the ingestion Payload was detected: Map<contentRowNum, bulkPayload>
     */
    private Map<Integer, BulkPayload> getIngestionPayloadMap(@NonNull final List<String> payloadList) {
        Map<Integer, BulkPayload> bulkPayloadMap = new HashMap<>();

        for (int rowNum = 0; rowNum < payloadList.size(); rowNum++) {
            Optional<String> indexName = RequestIdentifier.getBulkIndexName(payloadList.get(rowNum));
            if (indexName.isPresent() && (rowNum + 1 < payloadList.size())) {
                bulkPayloadMap.put(rowNum + 1,
                        new BulkPayload(indexName.get(), ingestionSerializer.deserialize(payloadList.get(rowNum + 1))));
                rowNum++; // Skip the checking for the next line
            }
        }
        return bulkPayloadMap;
    }

    // Match config map with ingestion payload, return the list of BulkRequest that needs to be comprehend
    private static List<BulkRequest> getBatchObjectList(@NonNull final Map<Integer, BulkPayload> payloadMap,
                                                        @NonNull final Map<String, ComprehendConfiguration> configMap) {
        List<BulkRequest> bulkRequestList = new ArrayList<>();

        payloadMap.forEach((locationRowNum, bulkPayload) -> configMap.forEach((key, config) -> {
            if (bulkPayload.getIndexName().equals(config.getIndexName())) {
                // For each config, find matched fields in the input
                Optional.ofNullable(bulkPayload.getPayloadJson().findValue(config.getFieldName())).ifPresent(fieldValue ->
                        // For each operation, add to the bulkRequestList
                        config.getComprehendOperations().forEach(operation ->
                                bulkRequestList.add(new BulkRequest(fieldValue.asText(), operation, config.getLanguageCode(),
                                        new BatchFieldLocator(
                                                String.format("%s_%s", config.getFieldName(), operation.toString()),
                                                locationRowNum)
                                ))
                        )
                );
            }
        }));
        return bulkRequestList;
    }

    // Combine the batch objects in the pack of 25, and generate the batch request
    private List<Callable<BatchResponse>> createCallableList(@NonNull final List<BulkRequest> bulkRequestList) {
        List<Callable<BatchResponse>> callableList = new ArrayList<>();
        // Map<Operation_LanguagePair, callable> stores unique callable for each Operation_Language pair
        Map<String, BatchOperationCallable> batchExecutorMap = new HashMap<>();

        bulkRequestList.forEach(
                bulkRequest -> {
                    ComprehendOperationEnum operation = bulkRequest.getComprehendOperation();
                    LanguageCode language = bulkRequest.getLanguageCode();
                    String content = bulkRequest.getFieldToBeComprehend();
                    BatchFieldLocator locator = bulkRequest.getLocator();
                    String operationLanguagePair = (operation == ComprehendOperationEnum.DetectDominantLanguage) ?
                            operation.toString() : String.format("%s_%s", operation.toString(), language.toString());

                    // If the callable exists, attach its fieldToBeComprehend to the content list
                    if (batchExecutorMap.containsKey(operationLanguagePair)) {
                        BatchOperationCallable batchOperationCallable = batchExecutorMap.get(operationLanguagePair);
                        batchOperationCallable.getContentList().add(content);
                        batchOperationCallable.getFieldLocatorList().add(locator);
                        // if the list is full, submit the pack to callableList and remove it from the map
                        if (batchOperationCallable.getContentList().size() >= Constants.MAX_BATCH_SIZE) {
                            callableList.add(batchOperationCallable);
                            batchExecutorMap.remove(operationLanguagePair);
                        }
                    } else {
                        // If the callable doesn't exist, create it and add to the callable map
                        List<BatchFieldLocator> locatorList = new ArrayList<>();
                        List<String> contentList = new ArrayList<>();
                        locatorList.add(locator);
                        contentList.add(content);
                        batchExecutorMap.put(operationLanguagePair, new BatchOperationCallable(operation,
                                language, comprehendClient, locatorList, contentList));
                    }
                }
        );
        // Add all the callable to the list (full or not)
        callableList.addAll(batchExecutorMap.values());
        return callableList;
    }

    // Attach Comprehend response to the original payload array list
    private static void attachComprehendResponse(@NonNull final List<String> payloadList,
                                                 @NonNull final Future<BatchResponse> result, LambdaLogger logger) {
        BatchResponse<?> responseObject = null;
        try {
            responseObject = result.get();
            logger.log(String.format("Successfully got Comprehend batch response for %d documents", payloadList.size()));
        } catch (Exception e) {
            logger.log("Got an exception while getting comprehend result. " + e);
            if (e instanceof ExecutionException) {
                logger.log("Got ExecutionException while retrieving results from ExecutorService: " + e.getCause());
            }
            throw new RuntimeException(e);
        }
        List<BatchFieldLocator> locatorList = responseObject.getLocatorList();
        List<JSONObject> resultList = responseObject.getBatchResultList();
        List<JSONObject> flattenedResultList = responseObject.getBatchFlattenedResultList();

        for (int i = 0; i < locatorList.size(); i++) {
            BatchFieldLocator locator = locatorList.get(i);
            int contentRow = locator.getContentRowNum();
            JSONObject content = new JSONObject(payloadList.get(contentRow));
            // Attach the comprehend result into the original content
            content.put(locator.getFieldNameAndOperation(), resultList.get(i));

            // Add flattened response for Kibana plotting
            if (flattenedResultList != null && flattenedResultList.get(i) != null) {
                content.put(String.format("%s_%s", locator.getFieldNameAndOperation(),
                        Constants.KIBANA_KEY_NAME), flattenedResultList.get(i));
            }
            // Add timestamp
            content.put(Constants.TIME_STAMP_KEY, Instant.now().toString());
            // Replace the value in the payload list
            payloadList.set(contentRow, content.toString());
        }
    }
}