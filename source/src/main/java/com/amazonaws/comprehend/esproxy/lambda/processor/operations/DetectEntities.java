package com.amazonaws.comprehend.esproxy.lambda.processor.operations;

import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.BatchResponse;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.BatchDetectEntitiesItemResult;
import com.amazonaws.services.comprehend.model.BatchDetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.BatchDetectEntitiesResult;
import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.comprehend.model.Entity;
import lombok.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetectEntities implements ComprehendOperation {
    private static final String[] VISUALIZATION_NAME_LIST = {"entity-commercial-item", "entity-event",
            "entity-location", "entity-organization", "entity-person", "entity-title"};

    @Override
    public String[] getVisualizationNameList() {
        return VISUALIZATION_NAME_LIST;
    }

    /**
     * Send out DetectEntitiesRequest, return SingularResponse that stores DetectEntitiesResult
     * Results are flattened for Kibana visualization
     */
    @Override
    public SingularResponse sendSingularRequest(@NonNull final String fieldNameAndOperation,
                                                @NonNull final String content,
                                                @NonNull final LanguageCode languageCode,
                                                @NonNull final AmazonComprehend comprehendClient) {
        DetectEntitiesRequest request
                = new DetectEntitiesRequest().withText(content).withLanguageCode(languageCode.toString());
        DetectEntitiesResult detectEntitiesResult = comprehendClient.detectEntities(request);

        return new SingularResponse(fieldNameAndOperation, new JSONObject(detectEntitiesResult),
                flattenEntityList(detectEntitiesResult.getEntities()));
    }

    /**
     * Send out BatchDetectEntitiesRequest, return BatchResponse that stores BatchDetectEntitiesResult
     * Results are flattened for Kibana visualization
     */
    @Override
    public BatchResponse sendBatchRequest(@NonNull final List<BatchFieldLocator> fieldLocatorList,
                                          @NonNull final List<String> contentList,
                                          @NonNull final LanguageCode languageCode,
                                          @NonNull final AmazonComprehend comprehendClient) {
        BatchDetectEntitiesRequest request
                = new BatchDetectEntitiesRequest().withTextList(contentList).withLanguageCode(languageCode.toString());
        BatchDetectEntitiesResult batchDetectEntitiesResult = comprehendClient.batchDetectEntities(request);

        List<JSONObject> flattenedEntityList = new ArrayList<>();
        for (BatchDetectEntitiesItemResult resultItem : batchDetectEntitiesResult.getResultList()) {
            flattenedEntityList.add(flattenEntityList(resultItem.getEntities()));
        }

        return new BatchResponse<>(
                fieldLocatorList,
                batchDetectEntitiesResult.getResultList(),
                batchDetectEntitiesResult.getErrorList(), flattenedEntityList);
    }

    /**
     * Flatten the entityList to JSONObject
     *
     * @param entityList The received entityList from Comprehend response
     * @return JSONObject that contains flattened EntityList as JSONArray
     */
    private static JSONObject flattenEntityList(@NonNull final List<Entity> entityList) {
        JSONObject flattenedEntityObject = new JSONObject();
        for (Entity entity : entityList) {
            try {
                // If the entity type exists, add the new entity to the JSONArray, otherwise create a new JSONObject
                if (flattenedEntityObject.has(entity.getType())) {
                    flattenedEntityObject.getJSONArray(entity.getType()).put(new JSONObject(entity));
                } else {
                    flattenedEntityObject.put(entity.getType(), Collections.singletonList((new JSONObject(entity))));
                }
            } catch (JSONException e) {
                return null;
            }
        }
        return flattenedEntityObject;
    }
}
