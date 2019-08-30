package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/**
 * The DefaultProcessor that process Comprehend Elasticsearch proxy requests without modifying the content
 */
@RequiredArgsConstructor
public class DefaultProcessor implements ElasticsearchProcessor {
    @NonNull
    private final ElasticsearchClient esClient;

    /**
     * Process Comprehend Elasticsearch proxy requests without modifying the content
     *
     * @param request The received Elasticsearch client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return Elasticsearch service response
     */
    @Override
    public Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger) {
        logger.log("Default Elasticsearch requests detected");
        return esClient.performRequest(request);
    }
}
