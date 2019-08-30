package com.amazonaws.comprehend.esproxy.lambda.utils;

import com.amazonaws.comprehend.esproxy.lambda.model.BatchFieldLocator;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.amazonaws.services.comprehend.model.DetectDominantLanguageResult;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.comprehend.model.DetectSyntaxResult;
import com.amazonaws.services.comprehend.model.DominantLanguage;
import com.amazonaws.services.comprehend.model.Entity;
import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.amazonaws.services.comprehend.model.PartOfSpeechTag;
import com.amazonaws.services.comprehend.model.PartOfSpeechTagType;
import com.amazonaws.services.comprehend.model.SentimentScore;
import com.amazonaws.services.comprehend.model.SyntaxToken;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Getter
public class TestConstants {
    // General
    final public static String EMPTY_STRING = "";
    final public static String DUMMY_METHOD = HttpPost.METHOD_NAME;
    final public static String DUMMY_ENDPOINT = "/tweeter/doc/1";
    final public static String DUMMY_PAYLOAD = "{\"test\":\"I am a dummy payload\"}";
    final public static String DUMMY_RESPONSE = "dummy response";
    final public static String DUMMY_RESPONSE_JSON = "{\"result\":\"dummy result\"}";
    final public static String DUMMY_ERROR_MESSAGE = "dummy error message";
    final public static Map<String, String> DUMMY_HEADER_MAP = Collections.singletonMap("dummyKey", "dummyValue");
    final public static String ERROR_CODE = "InternalServerException";
    final public static String DUMMY_REQUEST_ID = "12345";

    final public static String GET_METHOD = HttpGet.METHOD_NAME;
    final public static String PUT_METHOD = HttpPut.METHOD_NAME;
    final public static String POST_METHOD = HttpPost.METHOD_NAME;
    final public static String DUMMY_MESSAGE = "How are you";
    final public static int STATUS_CODE_OK = HttpStatus.SC_OK;

    final public static String WRONG_PATH = "/some/funny/path";
    final public static String CORRECT_INDEX_PATH = "/index/type/id";
    final public static String WRONG_INDEX_PATH = "/index/.happy/id/_search";
    final public static String WRONG_INDEX_PATTERN = "/U_$%/type/id";
    final public static List<BatchFieldLocator> FIELD_LOCATOR_LIST = Collections.singletonList(
            new BatchFieldLocator(String.format("%s_%s", TestConstants.FIELD_NAME, ComprehendOperationEnum.DetectSentiment), 1));
    final public static List<String> CONTENT_LIST =
            Collections.singletonList(TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT);

    // Kibana logs
    final public static String UPLOAD_MAPPING_LOG = "Upload Mappings for Comprehend fields";
    final public static String UPLOAD_MARKDOWN_LOG = "Upload markdowns to Kibana";
    final public static String UPLOAD_INDEX_PATTERN_LOG = "Upload Comprehend Index Pattern to Kibana";
    final public static String UPLOAD_VISUALIZATION_LOG = "Upload visualizations to Kibana";
    final public static String UPLOAD_DASHBOARD_LOG = "Upload Comprehend Dashboard to Kibana";
    final public static String UPLOAD_FAILED_LOG = "Upload Dashboard failed with Exception: " +
            "java.util.concurrent.ExecutionException: " +
            "com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException";

    final public static String MAPPING_JSON = "{\"properties\":" +
            "{\"FIELD_NAME_DetectDominantLanguage.languages\":{\"type\":\"nested\"}," +
            "\"FIELD_NAME_DetectEntities.entities\":{\"type\":\"nested\"}," +
            "\"FIELD_NAME_DetectSyntax.syntaxTokens\":{\"type\":\"nested\"}," +
            "\"FIELD_NAME_DetectKeyPhrases.keyPhrases\":{\"type\":\"nested\"}}}";

    // Callable
    public final static String STATUS_CODE_KEY = "statusCode";

    public final static String ERROR_CODE_KEY = "errorCode";

    public final static String REQUEST_ID_KEY = "requestId";

    public final static String ERROR_MESSAGE_KEY = "errorMessage";

    // Bulk payload
    final public static String BULK_INDEX_PAYLOAD =
            "{\"index\":{\"_index\":\"tweeter\",\"_type\":\"_doc\",\"_id\":\"1\"}}";
    final public static String BULK_CREATE_PAYLOAD =
            "{\"create\":{\"_index\":\"tweeter\",\"_type\":\"_doc\",\"_id\":\"1\"}}";
    final public static String WRONG_BULK_INDEX_PAYLOAD =
            "{\"wrong_pattern\":{\"_index\":\"tweeter\",\"_type\":\"_doc\",\"_id\":\"1\"}}";
    final public static String BULK_PAYLOAD_NO_INDEX_KEY =
            "{\"index\":{\"happy\":\"tweeter\",\"_type\":\"_doc\",\"_id\":\"1\"}}";

    // Index related
    final public static String CUSTOMER_INGESTION_PAYLOAD = "{\n" +
            "    \"name\":\"Tom\",\n" +
            "    \"location\":\"Office\",\n" +
            "    \"text\":\"Tom went to the office\"\n" +
            "}";
    final public static String CUSTOMER_INGESTION_PAYLOAD_NAME_KEY = "name";
    final public static String CUSTOMER_INGESTION_PAYLOAD_LOCATION_KEY = "location";
    final public static String CUSTOMER_INGESTION_PAYLOAD_TEXT_KEY = "text";

    final public static String CUSTOMER_INGESTION_PAYLOAD_NAME = "Tom";
    final public static String CUSTOMER_INGESTION_PAYLOAD_LOCATION = "Office";
    final public static String CUSTOMER_INGESTION_PAYLOAD_TEXT = "Tom went to the office";

    final public static String CUSTOMER_INGESTION_PAYLOAD_NESTED = "{\n" +
            "    \"name\":\"Tom\",\n" +
            "    \"location\":\"Office\",\n" +
            "    \"text\":{\n" +
            "        \"message\":\"Today is a good day\",\n" +
            "        \"details\":{\n" +
            "            \"date\":\"Monday\",\n" +
            "            \"content\":\"Because I win the price\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    final public static String CUSTOMER_INGESTION_PAYLOAD_NO_KEYWORD = "{\n" +
            "    \"name\":\"Tom\",\n" +
            "    \"location\":\"Office\",\n" +
            "    \"time\":\"Monday\"\n" +
            "}";
    final public static String CUSTOMER_PAYLOAD_NO_INGESTION_REQUEST =
            "{ \"update\" : {\"_id\" : \"1\", \"_type\" : \"_doc\", \"_index\" : \"tweeter\"} }\n" +
                    "{ \"doc\" : {\"field2\" : \"value2\"} }\n" +
                    "{ \"delete\" : { \"_index\" : \"facebook\", \"_type\" : \"_doc\", \"_id\" : \"2\" } }\n" +
                    "{ \"update\" : {\"_id\" : \"3\", \"_type\" : \"_doc\", \"_index\" : \"tweeter\"} }\n" +
                    "{ \"doc\" : {\"field2\" : \"value2\"} }";

    final public static String CUSTOMER_PAYLOAD_NO_CONFIG_KEY_WORD =
            "{ \"index\" : {\"_id\" : \"1\", \"_type\" : \"_doc\", \"_index\" : \"tweeter\"} }\n" +
                    "{ \"doc\" : {\"field2\" : \"value2\"} }\n" +
                    "{ \"delete\" : { \"_index\" : \"facebook\", \"_type\" : \"_doc\", \"_id\" : \"2\" } }\n" +
                    "{ \"create\" : {\"_id\" : \"3\", \"_type\" : \"_doc\", \"_index\" : \"tweeter\"} }\n" +
                    "{ \"doc\" : {\"field2\" : \"value2\"} }";

    final public static String CUSTOMER_PAYLOAD_CONTAINS_CONFIG_KEY_WORD =
            "{ \"index\" : {\"_id\" : \"1\", \"_type\" : \"_doc\", \"_index\" : \"tweeter\"} }\n" +
                    "{ \"text\" : {\"field2\" : \"value2\"} }\n" +
                    "{ \"delete\" : { \"_index\" : \"facebook\", \"_type\" : \"_doc\", \"_id\" : \"2\" } }\n";

    // Config related
    final public static String CONFIG = "{\n\"" + Constants.CONFIG_KEY + "\": [\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"text\",\n" +
            "            \"comprehendOperations\": [\"DetectSentiment\"],\n" +
            "            \"languageCode\": \"en\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    final public static String FIELD_NAME = "text";
    final public static String INDEX_NAME = "tweeter";
    final public static Set<ComprehendOperationEnum> COMPREHEND_OPERATIONS =
            Collections.singleton(ComprehendOperationEnum.DetectSentiment);
    final public static LanguageCode LANGUAGE_CODE = LanguageCode.en;

    final public static String NEW_CONFIG = "{\n\"" + Constants.CONFIG_KEY + "\": [\n" +
            "        {\n" +
            "            \"indexName\": \"facebook\"," +
            "            \"fieldName\": \"content\",\n" +
            "            \"comprehendOperations\": [\"DetectKeyPhrases\"],\n" +
            "            \"languageCode\": \"de\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    final public static String NEW_FIELD_NAME = "content";
    final public static String NEW_INDEX_NAME = "facebook";
    final public static Set<ComprehendOperationEnum> NEW_OPERATIONS =
            Collections.singleton(ComprehendOperationEnum.DetectKeyPhrases);
    final public static LanguageCode NEW_LANGUAGE_CODE = LanguageCode.de;

    final public static String CONFIG_LIST = "{\n\"" + Constants.CONFIG_KEY + "\": [\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"text\",\n" +
            "            \"comprehendOperations\": [\"DetectSentiment\"],\n" +
            "            \"languageCode\": \"en\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"indexName\": \"facebook\"," +
            "            \"fieldName\": \"content\",\n" +
            "            \"comprehendOperations\": [\"DetectKeyPhrases\"],\n" +
            "            \"languageCode\": \"de\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    final public static String EMPTY_CONFIG_CONTENT = "{" + Constants.CONFIG_KEY + ":[]}";
    final public static String EMPTY_CONFIG_LIST = "{" + Constants.CONFIG_KEY + ":[\"\"]}";
    final public static String EMPTY_COMPREHEND_OPERATION_LIST = "{\n" + Constants.CONFIG_KEY + ": [\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"text\",\n" +
            "            \"comprehendOperations\": [],\n" +
            "            \"languageCode\": \"es\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    final public static String MISSING_CONFIG_KEY = "{\"happy\":\"today\"}";
    final public static String MISSING_CONFIG_COMPONENT = "{" + Constants.CONFIG_KEY + ":[\"fieldName\":\"title\"]}";

    final public static String WRONG_CONFIG_COMPONENT = "{" + Constants.CONFIG_KEY + ":[\"happy\":\"today\"]}";
    final public static String WRONG_LANGUAGE_CODE = "{\n" + Constants.CONFIG_KEY + ": [\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"text\",\n" +
            "            \"comprehendOperations\": [\"DetectSentiment\"],\n" +
            "            \"languageCode\": \"English\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    final public static String WRONG_COMPREHEND_OPERATION = "{\n" + Constants.CONFIG_KEY + ": [\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"text\",\n" +
            "            \"comprehendOperations\": [\"SentimentDetect\"],\n" +
            "            \"languageCode\": \"en\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    final public static String DUPLICATE_FIELD_NAME = "{\n\"" + Constants.CONFIG_KEY + "\": [\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"title\",\n" +
            "            \"comprehendOperations\": [\"DetectSentiment\"],\n" +
            "            \"languageCode\": \"en\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"indexName\": \"tweeter\"," +
            "            \"fieldName\": \"title\",\n" +
            "            \"comprehendOperations\": [\"DetectSentiment\"],\n" +
            "            \"languageCode\": \"en\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static Map<String, ComprehendConfiguration> getConfigMap() {
        ComprehendConfiguration defaultObject = new ComprehendConfiguration(
                INDEX_NAME, FIELD_NAME, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        Map<String, ComprehendConfiguration> configMap = new HashMap<>();
        configMap.put(FIELD_NAME, defaultObject);
        return configMap;
    }

    public static Map<String, ComprehendConfiguration> getNewConfigMap() {
        ComprehendConfiguration newObject = new ComprehendConfiguration(
                NEW_INDEX_NAME, NEW_FIELD_NAME, NEW_OPERATIONS, NEW_LANGUAGE_CODE);
        Map<String, ComprehendConfiguration> newConfigMap = new HashMap<>();
        newConfigMap.put(NEW_FIELD_NAME, newObject);
        return newConfigMap;
    }

    public static Map<String, ComprehendConfiguration> getListConfigMap() {
        ComprehendConfiguration newObject1 = new ComprehendConfiguration(
                INDEX_NAME, FIELD_NAME, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        ComprehendConfiguration newObject2 = new ComprehendConfiguration(
                NEW_INDEX_NAME, NEW_FIELD_NAME, NEW_OPERATIONS, NEW_LANGUAGE_CODE);
        Map<String, ComprehendConfiguration> newConfigMap = new HashMap<>();
        newConfigMap.put(FIELD_NAME, newObject1);
        newConfigMap.put(NEW_FIELD_NAME, newObject2);
        return newConfigMap;
    }

    public static PreprocessingConfigRequest getConfigRequest() {
        ComprehendConfiguration defaultObject = new ComprehendConfiguration(
                INDEX_NAME, FIELD_NAME, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        List<ComprehendConfiguration> configList = Collections.singletonList(defaultObject);
        return new PreprocessingConfigRequest(configList);
    }

    public static PreprocessingConfigRequest getNewConfigRequest() {
        ComprehendConfiguration newObject = new ComprehendConfiguration(
                NEW_INDEX_NAME, NEW_FIELD_NAME, NEW_OPERATIONS, NEW_LANGUAGE_CODE);
        List<ComprehendConfiguration> configList = Collections.singletonList(newObject);
        return new PreprocessingConfigRequest(configList);
    }

    public static PreprocessingConfigRequest getListConfigRequest() {
        ComprehendConfiguration newObject1 = new ComprehendConfiguration(
                INDEX_NAME, FIELD_NAME, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        ComprehendConfiguration newObject2 = new ComprehendConfiguration(
                NEW_INDEX_NAME, NEW_FIELD_NAME, NEW_OPERATIONS, NEW_LANGUAGE_CODE);
        List<ComprehendConfiguration> configList = new ArrayList<>();
        configList.add(newObject1);
        configList.add(newObject2);
        return new PreprocessingConfigRequest(configList);
    }

    public static PreprocessingConfigRequest getEmptyFieldConfigRequest() {
        ComprehendConfiguration defaultObject = new ComprehendConfiguration(
                INDEX_NAME, EMPTY_STRING, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        List<ComprehendConfiguration> configList = Collections.singletonList(defaultObject);
        return new PreprocessingConfigRequest(configList);
    }

    public static PreprocessingConfigRequest getDuplicateConfigRequest() {
        ComprehendConfiguration newObject1 = new ComprehendConfiguration(
                INDEX_NAME, FIELD_NAME, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        ComprehendConfiguration newObject2 = new ComprehendConfiguration(
                INDEX_NAME, FIELD_NAME, COMPREHEND_OPERATIONS, LANGUAGE_CODE);
        List<ComprehendConfiguration> configList = new ArrayList<>();
        configList.add(newObject1);
        configList.add(newObject2);
        return new PreprocessingConfigRequest(configList);
    }

    // ResponseFlattener related
    final public static String FIELD_NAME_AND_OPERATION = "message_DetectEntities";
    final public static String ORGANIZATION_KEY = "ORGANIZATION";
    final public static String ORGANIZATION_VALUE = "Amazon";
    final public static String LOCATION_KEY = "LOCATION";
    final public static String LOCATION_VALUE1 = "Seattle";
    final public static String LOCATION_VALUE2 = "Washington";

    public static DetectEntitiesResult getDetectEntitiesResult() {
        Entity entity1 = new Entity().withBeginOffset(0).withEndOffset(6).withScore(0.98f)
                .withText(ORGANIZATION_VALUE).withType(ORGANIZATION_KEY);

        Entity entity2 = new Entity().withBeginOffset(18).withEndOffset(31).withScore(0.70f)
                .withText(LOCATION_VALUE1).withType(LOCATION_KEY);

        Entity entity3 = new Entity().withBeginOffset(10).withEndOffset(15).withScore(0.98f)
                .withText(LOCATION_VALUE2).withType(LOCATION_KEY);

        DetectEntitiesResult detectEntitiesResult = new DetectEntitiesResult();
        detectEntitiesResult.setEntities(Lists.newArrayList(entity1, entity2, entity3));
        return detectEntitiesResult;
    }

    public static DetectKeyPhrasesResult getDetectKeyPhrasesResult() {
        KeyPhrase keyPhrase1 = new KeyPhrase().withBeginOffset(0).withEndOffset(6).withScore(0.98f)
                .withText(ORGANIZATION_VALUE);
        KeyPhrase keyPhrase2 = new KeyPhrase().withBeginOffset(18).withEndOffset(31).withScore(0.70f)
                .withText(LOCATION_VALUE1);
        KeyPhrase keyPhrase3 = new KeyPhrase().withBeginOffset(10).withEndOffset(15).withScore(0.70f)
                .withText(LOCATION_VALUE2);

        DetectKeyPhrasesResult detectKeyPhrasesResult = new DetectKeyPhrasesResult();
        detectKeyPhrasesResult.setKeyPhrases(Lists.newArrayList(keyPhrase1, keyPhrase2, keyPhrase3));
        return detectKeyPhrasesResult;
    }

    public static DetectSyntaxResult getDetectSyntaxResult() {
        SyntaxToken syntax1 = new SyntaxToken().withBeginOffset(0).withEndOffset(6).withTokenId(1)
                .withText(ORGANIZATION_VALUE).withPartOfSpeech(new PartOfSpeechTag().withTag(PartOfSpeechTagType.NOUN));

        SyntaxToken syntax2 = new SyntaxToken().withBeginOffset(18).withEndOffset(31).withTokenId(2)
                .withText(LOCATION_VALUE1).withPartOfSpeech(new PartOfSpeechTag().withTag(PartOfSpeechTagType.NOUN));

        SyntaxToken syntax3 = new SyntaxToken().withBeginOffset(10).withEndOffset(15).withTokenId(3)
                .withText(LOCATION_VALUE2).withPartOfSpeech(new PartOfSpeechTag().withTag(PartOfSpeechTagType.NOUN));

        DetectSyntaxResult detectSyntaxResult = new DetectSyntaxResult();
        detectSyntaxResult.setSyntaxTokens(Lists.newArrayList(syntax1, syntax2, syntax3));
        return detectSyntaxResult;
    }

    public static DetectSentimentResult getDetectSentimentResult() {
        SentimentScore sentimentScore = new SentimentScore()
                .withPositive(0.2f).withNegative(0.2f).withNeutral(0.2f).withMixed(0.2f);

        DetectSentimentResult detectSentimentResult = new DetectSentimentResult();
        detectSentimentResult.setSentiment("POSITIVE");
        detectSentimentResult.setSentimentScore(sentimentScore);
        return detectSentimentResult;
    }

    public static DetectDominantLanguageResult getDetectDominantLanguageResult() {
        DominantLanguage language1 = new DominantLanguage().withLanguageCode("en").withScore(0.97f);
        DominantLanguage language2 = new DominantLanguage().withLanguageCode("es").withScore(0.02f);
        DominantLanguage language3 = new DominantLanguage().withLanguageCode("de").withScore(0.01f);

        DetectDominantLanguageResult detectLanguageResult = new DetectDominantLanguageResult();
        detectLanguageResult.setLanguages(Lists.newArrayList(language1, language2, language3));
        return detectLanguageResult;
    }

    public static String getPlainText(final String str) {
        String lineSeparator = System.getProperty("line.separator", "\n");
        return str.replaceAll(" ", EMPTY_STRING).replaceAll(lineSeparator, EMPTY_STRING);
    }
}
