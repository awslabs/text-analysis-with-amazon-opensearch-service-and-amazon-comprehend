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

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.NonNull;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/**
 * {@code OpenSearchServiceProcessor} is the interface of all processors that
 * processes OpenSearch Service proxy request.
 */
public interface OpenSearchServiceProcessor {
    // Process OpenSearchService request and return OpenSearchService response

    /**
     * Process the received OpenSearchService client request and return OpenSearchService client response
     *
     * @param request The received OpenSearchService client request
     * @param logger  The LambdaLogger to output logs from the function code
     * @return The received OpenSearchService client response from Amazon OpenSearchService service
     */
    Response processRequest(@NonNull final Request request, @NonNull final LambdaLogger logger);
}
