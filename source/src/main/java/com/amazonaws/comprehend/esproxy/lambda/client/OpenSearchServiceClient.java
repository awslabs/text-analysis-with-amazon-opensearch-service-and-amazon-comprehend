// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License").
// You may not use this file except in compliance with the License.
// A copy of the License is located at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// or in the "license" file accompanying this file. This file is distributed
// on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the License for the specific language governing
// permissions and limitations under the License.

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
 * OpenSearch Service Rest Client Wrapper class
 */
@RequiredArgsConstructor
public class OpenSearchServiceClient {
    private final RestClient restClient;

    /**
     * Perform OpenSearch Service request
     *
     * @param request Received OpenSearch Service request
     * @return OpenSearch Service service response
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
     * Perform OpenSearch Service request with received request and new endpoint
     *
     * @param request  Received OpenSearch Service request
     * @param endpoint New OpenSearch Service Domain Endpoint
     * @return OpenSearch Service service response
     */
    public Response performRequest(@NonNull Request request, @NonNull String endpoint) {
        Request newRequest = new Request(request.getMethod(), endpoint);
        newRequest.setEntity(request.getEntity());
        return performRequest(newRequest);
    }

    /**
     * Perform OpenSearch Service request with given method and endpoint (with no payload)
     *
     * @param method   Request method
     * @param endpoint OpenSearch Service Domain Endpoint
     * @return OpenSearch Service service response
     */
    public Response performRequest(@NonNull String method, @NonNull String endpoint) {
        Request request = new Request(method, endpoint);
        return performRequest(request);
    }

    /**
     * Perform OpenSearch Service request with given method, endpoint and payload
     *
     * @param method   Request method
     * @param endpoint OpenSearch Service Domain Endpoint
     * @param payload  The new payload for the request
     * @return OpenSearch Service service response
     */
    public Response performRequest(@NonNull String method, @NonNull String endpoint, @NonNull String payload) {
        Request request = new Request(method, endpoint);
        request.setJsonEntity(payload);
        return performRequest(request);
    }

    /**
     * Perform OpenSearch Service request with given method, endpoint, payload and new headers
     *
     * @param method    Request method
     * @param endpoint  OpenSearch Service Domain Endpoint
     * @param payload   The new payload for the request
     * @param headerMap The map for the request headers
     * @return OpenSearch Service service response
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
