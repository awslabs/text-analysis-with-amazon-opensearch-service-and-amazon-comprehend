package com.amazonaws.comprehend.esproxy.lambda.client;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Map;

/**
 * Elasticsearch Rest Client Wrapper class
 */
@RequiredArgsConstructor
public class ElasticsearchClient {
    private final RestClient restClient;

    /**
     * Perform elasticsearch request
     *
     * @param request Received Elasticsearch request
     * @return Elasticsearch service response
     */
    public Response performRequest(@NonNull Request request) {
        try {
            return restClient.performRequest(request);
        } catch (ResponseException responseException) {
            return responseException.getResponse();
        } catch (IOException e) {
            throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
        }
    }

    /**
     * Perform elasticsearch request with received request and new endpoint
     *
     * @param request  Received Elasticsearch request
     * @param endpoint New Elasticsearch Domain Endpoint
     * @return Elasticsearch service response
     */
    public Response performRequest(@NonNull Request request, @NonNull String endpoint) {
        Request newRequest = new Request(request.getMethod(), endpoint);
        newRequest.setEntity(request.getEntity());
        return performRequest(newRequest);
    }

    /**
     * Perform elasticsearch request with given method and endpoint (with no payload)
     *
     * @param method   Request method
     * @param endpoint Elasticsearch Domain Endpoint
     * @return Elasticsearch service response
     */
    public Response performRequest(@NonNull String method, @NonNull String endpoint) {
        Request request = new Request(method, endpoint);
        return performRequest(request);
    }

    /**
     * Perform elasticsearch request with given method, endpoint and payload
     *
     * @param method   Request method
     * @param endpoint Elasticsearch Domain Endpoint
     * @param payload  The new payload for the request
     * @return Elasticsearch service response
     */
    public Response performRequest(@NonNull String method, @NonNull String endpoint, @NonNull String payload) {
        Request request = new Request(method, endpoint);
        request.setJsonEntity(payload);
        return performRequest(request);
    }

    /**
     * Perform elasticsearch request with given method, endpoint, payload and new headers
     *
     * @param method    Request method
     * @param endpoint  Elasticsearch Domain Endpoint
     * @param payload   The new payload for the request
     * @param headerMap The map for the request headers
     * @return Elasticsearch service response
     */
    public Response performRequest(@NonNull String method, @NonNull String endpoint, @NonNull String payload,
                                   @NonNull Map<String, String> headerMap) {
        Request request = new Request(method, endpoint);
        RequestOptions.Builder options = request.getOptions().toBuilder();

        headerMap.forEach(options::addHeader);
        request.setOptions(options);
        request.setJsonEntity(payload);
        return performRequest(request);
    }

}
