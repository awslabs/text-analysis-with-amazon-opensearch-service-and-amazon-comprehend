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

package com.amazonaws.comprehend.esproxy.lambda.utils.kibana;

import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaHelper.*;

/**
 * Upload Comprehend Kibana sample dashboard
 */
@RequiredArgsConstructor
public class KibanaUploader {
    // Dashboard for DetectDominantLanguage is currently not available
    private final static String[] DASHBOARD_OPERATION_LIST = {"entity", "keyphrase", "sentiment", "syntax"};

    @NonNull
    private final OpenSearchServiceClient esClient;

    @NonNull
    private final ExecutorService executorService;

    /**
     * Upload mapping for Comprehend configurations
     *
     * @param configMap New Comprehend Configuration map from preprocessing request
     */
    public void uploadMapping(@NonNull Map<String, ComprehendConfiguration> configMap, @NonNull LambdaLogger logger) {
        logger.log("Upload Mappings for Comprehend fields");

        try {
            JSONObject mappingJson = getResourceFileJson(MAPPING_KEY_NAME);
            Set<String> indexNameSet = new HashSet<>();

            for (ComprehendConfiguration configuration : configMap.values()) {
                String indexName = configuration.getIndexName();
                String fieldName = configuration.getFieldName();

                // If the index is new, add a new mapping. Otherwise update the existing mapping
                if (!indexNameSet.contains(indexName) && isIndexNotFound(indexName)) {
                    esClient.performRequest(HttpPut.METHOD_NAME, String.format("/%s", indexName),
                            mappingJson.get(String.format("new-%s", MAPPING_KEY_NAME)).toString().replaceAll(FIELD_NAME_KEY, fieldName));
                    indexNameSet.add(indexName);
                } else {
                    esClient.performRequest(HttpPut.METHOD_NAME, String.format("/%s/_mapping/_doc", indexName),
                            mappingJson.get(MAPPING_KEY_NAME).toString().replaceAll(FIELD_NAME_KEY, fieldName));
                }
            }
        } catch (IOException e) {
            logger.log("Upload Mappings failed with IOException" + e);
        }
    }

    /**
     * Upload Kibana dashboard from the new Comprehend configurations
     *
     * @param configMap New Comprehend Configuration map from preprocessing request
     */
    public void uploadKibanaDashboard(@NonNull Map<String, ComprehendConfiguration> configMap, @NonNull LambdaLogger logger) {
        Set<String> indexNameSet = new HashSet<>();
        List<Future> futureList = new ArrayList<>();

        try {
            // Upload markdown visualizations
            futureList.add(submitMarkdownRequest(logger));

            for (ComprehendConfiguration configuration : configMap.values()) {
                String indexName = configuration.getIndexName();
                String fieldName = configuration.getFieldName();
                Set<ComprehendOperationEnum> operations = configuration.getComprehendOperations();

                // Upload index pattern to Kibana
                if (!indexNameSet.contains(indexName)) {
                    futureList.add(submitIndexPatternRequest(indexName, logger));
                    indexNameSet.add(indexName);
                }
                // Upload visualization
                futureList.add(submitVisualRequest(indexName, fieldName, operations, logger));

                // Upload Comprehend Dashboard
                futureList.add(submitDashboardRequest(indexName, fieldName, logger));
            }

            // Check whether all tasks have completed execution, or until timeout occurs
            for (Future future : futureList) {
                future.get(Constants.KIBANA_UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (TimeoutException e) {
            logger.log("Upload Dashboard timed out, some dashboard/visualization may not be uploaded. " + e);
        } catch (Exception e) {
            logger.log("Upload Dashboard failed with Exception: " + e);
        }
    }

    private Future submitMarkdownRequest(@NonNull LambdaLogger logger) throws IOException {
        logger.log("Upload markdowns to Kibana");
        JSONObject markdownJson = getResourceFileJson(MARKDOWN_KEY_NAME);

        return executorService.submit(() -> {
            for (String operation : DASHBOARD_OPERATION_LIST) {
                String markdownName = String.format("%s-%s", MARKDOWN_KEY_NAME, operation);
                String endpoint = String.format("%s-%s-%s",
                        MARKDOWN_PATH, operation, MARKDOWN_OVERRIDE);
                String payload = buildAttributes(markdownName, markdownJson.get(markdownName).toString());

                performUploadRequest(endpoint, payload, logger);
            }
        });
    }

    private Future submitIndexPatternRequest(@NonNull String indexName, @NonNull LambdaLogger logger) {
        logger.log("Upload Comprehend Index Pattern to Kibana");

        return executorService.submit(() -> {
            String endpoint = String.format("%s/%s-%s", INDEX_PATH, indexName, INDEX_OVERRIDE);
            String payload = INDEX_PATTERN_CONTENT.replaceAll(INDEX_NAME_KEY, indexName);

            performUploadRequest(endpoint, payload, logger);
        });
    }

    private Future submitVisualRequest(@NonNull String indexName,
                                       @NonNull String fieldName,
                                       @NonNull Set<ComprehendOperationEnum> operations,
                                       @NonNull LambdaLogger logger) throws IOException {
        logger.log("Upload visualizations to Kibana");

        JSONObject visualizationJson = getResourceFileJson(VISUALIZATION_KEY_NAME);
        List<String> visualizationNameList = new ArrayList<>();

        // Load visualization name for each operation and add to the visualizationNameList
        operations.forEach(
                operation -> visualizationNameList.addAll(
                        Arrays.asList(operation.getComprehendOperation().getVisualizationNameList()))
        );

        return executorService.submit(() -> {
            // For each visualization name, submit a upload request
            for (String visualizationName : visualizationNameList) {
                String visualName = String.format("%s_%s-%s", indexName, fieldName, visualizationName);
                String endpoint = String.format("%s/%s-%s", VISUAL_PATH, visualName, VISUAL_OVERRIDE);

                String payload = buildAttributes(visualName, visualizationJson.get(visualizationName).toString())
                        .replaceAll(INDEX_NAME_KEY, indexName)
                        .replaceAll(FIELD_NAME_KEY, fieldName)
                        .replaceAll(INDEX_ID_KEY, String.format("%s-%s", indexName, INDEX_ID_NAME));
                performUploadRequest(endpoint, payload, logger);
            }
        });
    }

    private Future submitDashboardRequest(@NonNull String indexName, @NonNull String fieldName,
                                          @NonNull LambdaLogger logger) throws IOException {
        logger.log("Upload Comprehend Dashboard to Kibana");
        JSONObject dashboardJson = getResourceFileJson(DASHBOARD_KEY_NAME);

        return executorService.submit(() -> {
            String endpoint = String.format("%s/%s_%s-%s", DASHBOARD_PATH, indexName, fieldName,
                    DASHBOARD_OVERRIDE);

            String payload = buildDashboard(dashboardJson.get(DASHBOARD_KEY_NAME).toString())
                    .replaceAll(INDEX_NAME_KEY, indexName)
                    .replaceAll(FIELD_NAME_KEY, fieldName);
            performUploadRequest(endpoint, payload, logger);
        });
    }

    private void performUploadRequest(@NonNull String endpoint, @NonNull String payload, @NonNull LambdaLogger logger) {
        esClient.performRequest(HttpPost.METHOD_NAME, endpoint, payload,
                Collections.singletonMap(KIBANA_HEADER_NAME, KIBANA_HEADER_VALUE));
    }

    private boolean isIndexNotFound(@NonNull String indexName) {
        return esClient.performRequest(HttpGet.METHOD_NAME, String.format("/%s", indexName))
                .getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND;
    }

}
