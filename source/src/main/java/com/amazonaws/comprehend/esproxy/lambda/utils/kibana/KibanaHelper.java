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

import com.amazonaws.util.IOUtils;
import lombok.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class KibanaHelper {
    public final static String KIBANA_HEADER_NAME = "kbn-xsrf";
    public final static String KIBANA_HEADER_VALUE = "reporting";

    public static final String INDEX_ID_NAME = "index-id";
    public final static String INDEX_PATTERN_CONTENT =
            "{\"attributes\":{\"title\":\"INDEX_NAME\",\"timeFieldName\":\"process_time\"}}";

    public static final String MARKDOWN_PATH = "/_plugin/kibana/api/saved_objects/visualization/comprehend_markdown";
    public static final String MARKDOWN_OVERRIDE = "visualization-id?overwrite=true";
    public static final String INDEX_PATH = "/_plugin/kibana/api/saved_objects/index-pattern";
    public static final String INDEX_OVERRIDE = "index-id?overwrite=true";
    public static final String VISUAL_PATH = "/_plugin/kibana/api/saved_objects/visualization";
    public static final String VISUAL_OVERRIDE = "visualization-id?overwrite=true";
    public final static String DASHBOARD_PATH = "/_plugin/kibana/api/saved_objects/dashboard";
    public final static String DASHBOARD_OVERRIDE = "comprehend-dashboard-id?overwrite=true";

    public final static String INDEX_NAME_KEY = "INDEX_NAME";
    public final static String FIELD_NAME_KEY = "FIELD_NAME";
    public final static String INDEX_ID_KEY = "INDEX_ID";

    public final static String MAPPING_KEY_NAME = "mapping";
    public final static String MARKDOWN_KEY_NAME = "markdown";
    public final static String VISUALIZATION_KEY_NAME = "visualization";
    public final static String DASHBOARD_KEY_NAME = "dashboard";

    private static Map<String, JSONObject> resourceMap = new HashMap<>();

    // Build Attributes for visualization
    static String buildAttributes(@NonNull String title, @NonNull String visState)
            throws JSONException {
        JSONObject attributes = new JSONObject();
        attributes.put("title", title);
        attributes.put("visState", visState);

        if (title.contains("markdown")) {
            attributes.put("kibanaSavedObjectMeta",
                    new JSONObject().put("searchSourceJSON", "{\"query\":{\"language\":\"lucene\"}}"));
        } else {
            attributes.put("kibanaSavedObjectMeta",
                    new JSONObject().put("searchSourceJSON", "{\"index\":\"INDEX_ID\"}"));
        }
        JSONObject visualization = new JSONObject().put("attributes", attributes);
        return visualization.toString();
    }

    // Build Dashboard
    static String buildDashboard(@NonNull final String content) {
        JSONObject meta = new JSONObject();
        meta.put("searchSourceJSON", "{\"query\":{\"language\":\"lucene\"}}");

        // build attributes
        JSONObject attributes = new JSONObject();
        attributes.put("title", "INDEX_NAME_FIELD_NAME-comprehend-dashboard");
        attributes.put("panelsJSON", new JSONArray(content).toString());
        attributes.put("kibanaSavedObjectMeta", meta);

        JSONObject dashboard = new JSONObject();
        dashboard.put("attributes", attributes);

        return dashboard.toString();
    }

    static JSONObject getResourceFileJson(@NonNull String resourceFileName) throws IOException {
        if (resourceMap.containsKey(resourceFileName)) return resourceMap.get(resourceFileName);

        InputStream newMappingInputStream = KibanaHelper.class.getClassLoader()
                .getResourceAsStream(String.format("%s.json", resourceFileName));
        if (newMappingInputStream == null) throw new IOException();

        JSONObject fileJson = new JSONObject(IOUtils.toString(newMappingInputStream));
        resourceMap.put(resourceFileName, fileJson);
        return fileJson;
    }
}
