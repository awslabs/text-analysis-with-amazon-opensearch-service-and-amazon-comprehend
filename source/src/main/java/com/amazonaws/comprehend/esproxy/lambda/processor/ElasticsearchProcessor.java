package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.NonNull;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/**
 * {@code ElasticsearchProcessor} is the interface of all processors that
 * processes elasticsearch proxy request.
 */
public interface ElasticsearchProcessor {
    // Process elasticsearch request and return elasticsearch response

    /**
     * Process the received Elasticsearch client request and return Elasticsearch client response
     *
     * @param request The received Elasticsearch client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return The received Elasticsearch client response from Amazon Elasticsearch service
     */
    Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger);
}
