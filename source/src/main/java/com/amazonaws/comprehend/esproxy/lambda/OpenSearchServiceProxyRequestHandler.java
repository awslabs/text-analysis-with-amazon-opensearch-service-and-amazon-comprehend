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

package com.amazonaws.comprehend.esproxy.lambda;

import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.modules.ModuleConstants;
import com.amazonaws.comprehend.esproxy.lambda.modules.RequestHandlerModule;
import com.amazonaws.comprehend.esproxy.lambda.processor.OpenSearchServiceProcessor;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.comprehend.esproxy.lambda.utils.HTTPTransformer;
import com.amazonaws.comprehend.esproxy.lambda.utils.RequestIdentifier;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.NonNull;
import org.apache.http.HttpStatus;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import javax.inject.Named;
import java.util.Arrays;

/**
 * Handler to handle the APIGateway proxy request
 */
public class OpenSearchServiceProxyRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Injector INJECTOR = Guice.createInjector(new RequestHandlerModule());
    @Inject
    @Named(ModuleConstants.PREPROCESSING_CONFIG_PROCESSOR)
    private OpenSearchServiceProcessor configProcessor;

    @Inject
    @Named(ModuleConstants.INDEX_PROCESSOR)
    private OpenSearchServiceProcessor indexProcessor;

    @Inject
    @Named(ModuleConstants.BULK_PROCESSOR)
    private OpenSearchServiceProcessor bulkProcessor;

    @Inject
    @Named(ModuleConstants.DEFAULT_PROCESSOR)
    private OpenSearchServiceProcessor end2endProcessor;

    public OpenSearchServiceProxyRequestHandler() {
        INJECTOR.injectMembers(this);
    }

    /**
     * Handle the APIGateway proxy request
     *
     * @param requestEvent APIGatewayProxyRequestEvent send from customer through API Gateway Rest API
     * @param context Lambda runtime context
     * @return API Gateway Proxy Response
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Received API Gateway request event");

        try {
            // Transform API Gateway request to OpenSearch Service request
            Request esRequest = HTTPTransformer.apiGatewayRequestToESRequest(requestEvent);
            OpenSearchServiceProcessor openSearchServiceProcessor = getProcessor(esRequest);
            Response esConfigResponse = openSearchServiceProcessor.processRequest(esRequest, logger);
            return HTTPTransformer.esResponseToAPIGatewayResponse(esConfigResponse);

        } catch (InvalidRequestException e) {
            logger.log("Received InvalidRequestException when processing request");
            logger.log(Arrays.toString(e.getStackTrace()));
            return new APIGatewayProxyResponseEvent()
                    .withBody(e.getMessage())
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.log("Received InternalErrorException when processing request");
            logger.log(Arrays.toString(e.getStackTrace()));
            return new APIGatewayProxyResponseEvent()
                    .withBody(e.getMessage())
                    .withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private OpenSearchServiceProcessor getProcessor(@NonNull final Request esRequest) {
        if (RequestIdentifier.isConfigRequest(esRequest)) {
            return configProcessor;
        }
        if (RequestIdentifier.isMutationRequest(esRequest)) {
            if (RequestIdentifier.isIndexRequest(esRequest)) {
                return indexProcessor;
            }
            if (RequestIdentifier.isBulkRequest(esRequest)) {
                return bulkProcessor;
            }
        }
        return end2endProcessor;
    }

    @VisibleForTesting
    OpenSearchServiceProxyRequestHandler(Injector testInjector) {
        testInjector.injectMembers(this);
    }
}
