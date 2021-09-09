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

package com.amazonaws.comprehend.esproxy.lambda.utils;

public class Constants {
    // Client related

    public final static String AMAZON_ELASTICSEARCH_SERVICE_NAME = "es";

    public final static String PROTOCOL = "https://";

    public final static String USER_AGENT_NAME = "User-Agent";

    public final static String USER_AGENT_VALUE = "ComprehendOpenSearchServiceProxy/1.0";

    // Config related

    public final static String CONFIG_PATH = "/.comprehend/config/0";

    public final static String CONFIG_PATH_EXPOSED_TO_CUSTOMER = "/preprocessing_configurations";

    public final static String CONFIG_KEY = "comprehendConfigurations";

    // Identifier related

    public final static String UPDATE_REQUEST = "_update";

    public final static String SEARCH_REQUEST = "_search";

    public final static String BULK_REQUEST = "/_bulk";

    // A index must be all lowercase, shouldn't start with "_", shouldn't contain ",#><(){}"
    public final static String INDEX_REGEX = "^(?!_)(?!.*[A-Z,#><(){}]).*";

    public final static int INDEX_PATH_LENGTH = 3;

    public final static int INDEX_PATH_LENGTH_WITHOUT_ID = 2;

    // A bulk index action must match the format: {"index":{"_index":"test","_type":"_doc","_id":"1"}}
    public final static String BULK_INDEX_ACTION_KEY = "index";

    public final static String BULK_CREATE_ACTION_KEY = "create";

    public final static String BULK_INDEX_ACTION_KEY_NAME = "_index";

    // Ingestion related

    public final static int MAX_THREAD = 50;

    public final static int MAX_BATCH_SIZE = 25;

    public final static int INDEX_EXECUTOR_TIMEOUT_SECONDS = 10;

    public final static int BULK_EXECUTOR_TIMEOUT_SECONDS = 300;

    public final static int KIBANA_UPLOAD_TIMEOUT_SECONDS = 20;

    public final static String BULK_INDEX_KEY_NAME = "index";

    public final static String TIME_STAMP_KEY = "process_time";

    // Exception Key

    public final static String CLIENT_EXCEPTION_ERROR_CODE = "AmazonClientException";

    public final static String KIBANA_KEY_NAME = "Kibana";

    public final static String KEYPHRASES_KEY_NAME = "keyPhrases";

}
