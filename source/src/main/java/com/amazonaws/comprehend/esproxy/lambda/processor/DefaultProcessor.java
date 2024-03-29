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

package com.amazonaws.comprehend.esproxy.lambda.processor;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/**
 * The DefaultProcessor that process Comprehend OpenSearchService proxy requests without modifying the content
 */
@RequiredArgsConstructor
public class DefaultProcessor implements OpenSearchServiceProcessor {
    @NonNull
    private final OpenSearchServiceClient esClient;

    /**
     * Process Comprehend OpenSearchService proxy requests without modifying the content
     *
     * @param request The received OpenSearchService client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return OpenSearchService service response
     */
    @Override
    public Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger) {
        logger.log("Default OpenSearchService requests detected");
        return esClient.performRequest(request);
    }
}
