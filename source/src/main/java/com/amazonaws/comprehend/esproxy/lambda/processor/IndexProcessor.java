package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.processor.callable.SingularOperationCallable;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
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
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Processor to process Comprehend Elasticsearch Index requests
 */
@RequiredArgsConstructor
public class IndexProcessor implements ElasticsearchProcessor {

    public final static String SDK_RESPONSE_METADATA_KEY = "sdkResponseMetadata";

    public final static String SDK_HTTP_METADATA_KEY = "sdkHttpMetadata";

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
     * Process Comprehend Elasticsearch proxy Index requests
     *
     * @param request The received Elasticsearch client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return Elasticsearch service response
     */
    @Override
    public Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger) {
        logger.log("Index request detected");
        String payloadStr = HTTPTransformer.transformHttpEntityToString(request.getEntity());
        Map<String, ComprehendConfiguration> configMap = configRetriever.retrieveStoredConfig();
        String indexName = RequestIdentifier.getIndexName(request);

        if (Strings.isNullOrEmpty(payloadStr) || configMap.isEmpty()) {
            // If no payload or no comprehend config, pass through the request
            logger.log("The Payload or Comprehend config is empty, return pass through requests");
            return esClient.performRequest(request);
        }

        try {
            JsonNode inputJson = ingestionSerializer.deserialize(payloadStr);
            List<Callable<SingularResponse>> callableList = createCallableList(indexName, inputJson, configMap);

            if (callableList.isEmpty()) {
                // If no field matches, pass through the request
                logger.log("No config field detected, return pass through requests");
                return esClient.performRequest(request);
            }

            List<Future<SingularResponse>> executionResult = executorService.invokeAll(callableList,
                    Constants.INDEX_EXECUTOR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            JSONObject payloadJson = new JSONObject(payloadStr);

            // Attach the comprehend response
            for (Future<SingularResponse> result : executionResult) {
                try {
                    SingularResponse responseObject = result.get();
                    payloadJson.put(responseObject.getFieldNameAndOperation(),
                            removeResponseMetadata(responseObject.getComprehendResult()));

                    // Add flattened response for Kibana plotting
                    JSONObject flattenedResponse = responseObject.getFlattenedResult();
                    if (flattenedResponse != null) {
                        payloadJson.put(String.format("%s_%s", responseObject.getFieldNameAndOperation(),
                                Constants.KIBANA_KEY_NAME), removeResponseMetadata(flattenedResponse));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    logger.log("Got exception when retrieving the comprehend response: " + e);
                    throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR);
                }
            }
            payloadJson.put(Constants.TIME_STAMP_KEY, Instant.now().toString());

            // Send the enriched request to ES
            logger.log("Ingest Comprehend enriched results to Elasticsearch");
            return esClient.performRequest(request.getMethod(), request.getEndpoint(), payloadJson.toString());
        } catch (Exception e) {
            logger.log("Exceptions happen when trying to process index request. " + e);
            throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
        }

    }

    // Match config map with the ingestion payload, return the list of Callable that needs to be executed
    private List<Callable<SingularResponse>> createCallableList(@NonNull String indexName,
                                                                @NonNull JsonNode inputJson,
                                                                @NonNull Map<String, ComprehendConfiguration> configMap) {
        List<Callable<SingularResponse>> callableList = new ArrayList<>();

        configMap.forEach((key, config) -> {
            if (indexName.equals(config.getIndexName())) {
                // For each config, find matched fields in the input
                Optional.ofNullable(inputJson.findValue(config.getFieldName())).ifPresent(fieldValue ->
                        // For each ComprehendOperationEnum, add to the CallableList
                        config.getComprehendOperations().forEach(
                                operation -> callableList.add(
                                        new SingularOperationCallable(
                                                String.format("%s_%s", config.getFieldName(), operation.toString()),
                                                operation, config.getLanguageCode(), fieldValue.asText(), comprehendClient)))
                );
            }
        });
        return callableList;
    }

    // Remove SDK metadata from the response
    private JSONObject removeResponseMetadata(@NonNull JSONObject responseJson) {
        responseJson.remove(SDK_RESPONSE_METADATA_KEY);
        responseJson.remove(SDK_HTTP_METADATA_KEY);
        return responseJson;
    }

}
