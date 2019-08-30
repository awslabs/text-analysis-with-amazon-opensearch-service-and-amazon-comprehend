package com.amazonaws.comprehend.esproxy.lambda.utils;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.NonNull;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.util.Map;

/**
 * Transformer to transform HTTP between API Gateway Request and Elasticsearch Request
 * HttpEntity to String
 */
public class HTTPTransformer {

    // Transform API Gateway request to Elasticsearch request
    public static Request apiGatewayRequestToESRequest(@NonNull final APIGatewayProxyRequestEvent requestEvent) {
        final Request esRequest = new Request(requestEvent.getHttpMethod(), requestEvent.getPath());
        Map<String,String> queryStringParams = requestEvent.getQueryStringParameters();

        if(queryStringParams!=null) queryStringParams.forEach(esRequest::addParameter);
        esRequest.setJsonEntity(requestEvent.getBody());
        return esRequest;
    }

    // Transform Elasticsearch response to API Gateway response
    public static APIGatewayProxyResponseEvent esResponseToAPIGatewayResponse(@NonNull final Response esResponse)
            throws InternalErrorException {
        int statusCode = esResponse.getStatusLine().getStatusCode();
        HttpEntity entity = esResponse.getEntity();

        if (entity == null) {
            return new APIGatewayProxyResponseEvent().withStatusCode(statusCode);
        }
        String responseBody = transformHttpEntityToString(entity);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(responseBody);
    }

    // Transform HttpEntity to String
    public static String transformHttpEntityToString(final HttpEntity entity)
            throws InternalErrorException {
        if (entity == null) {
            throw new InternalErrorException(CustomerMessage.INPUT_NULL_OR_EMPTY_ERROR);
        }
        try {
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            throw new InternalErrorException(CustomerMessage.INTERNAL_ERROR, e);
        }
    }
}
