package com.amazonaws.comprehend.esproxy.lambda;

import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.modules.ModuleConstants;
import com.amazonaws.comprehend.esproxy.lambda.modules.RequestHandlerModule;
import com.amazonaws.comprehend.esproxy.lambda.processor.ElasticsearchProcessor;
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
public class ElasticsearchProxyRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Injector INJECTOR = Guice.createInjector(new RequestHandlerModule());
    @Inject
    @Named(ModuleConstants.PREPROCESSING_CONFIG_PROCESSOR)
    private ElasticsearchProcessor configProcessor;

    @Inject
    @Named(ModuleConstants.INDEX_PROCESSOR)
    private ElasticsearchProcessor indexProcessor;

    @Inject
    @Named(ModuleConstants.BULK_PROCESSOR)
    private ElasticsearchProcessor bulkProcessor;

    @Inject
    @Named(ModuleConstants.DEFAULT_PROCESSOR)
    private ElasticsearchProcessor end2endProcessor;

    public ElasticsearchProxyRequestHandler() {
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
            // Transform API Gateway request to Elasticsearch request
            Request esRequest = HTTPTransformer.apiGatewayRequestToESRequest(requestEvent);
            ElasticsearchProcessor elasticsearchProcessor = getProcessor(esRequest);
            Response esConfigResponse = elasticsearchProcessor.processRequest(esRequest, logger);
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

    private ElasticsearchProcessor getProcessor(@NonNull final Request esRequest) {
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
    ElasticsearchProxyRequestHandler(Injector testInjector) {
        testInjector.injectMembers(this);
    }
}
