package com.amazonaws.comprehend.esproxy.lambda.utils.serializer;

import com.amazonaws.comprehend.esproxy.lambda.exception.CustomerMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.InvalidRequestException;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendConfiguration;
import com.amazonaws.comprehend.esproxy.lambda.model.PreprocessingConfigRequest;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigSerializerTest {
    final private ObjectMapper mapper = new ObjectMapper();

    private ConfigSerializer test;

    private Map<String, ComprehendConfiguration> configMap;

    private PreprocessingConfigRequest configRequest;

    List<ComprehendConfiguration> configList;

    @Before
    public void setup() {
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);

        test = new ConfigSerializer(mapper);
    }

    // Deserialize
    @Test
    public void succeedDeserializeSingleConfig() {
        configRequest = test.deserialize(TestConstants.CONFIG);
        configList = configRequest.getComprehendConfigurations();

        assertEquals(1, configList.size());
        ComprehendConfiguration object = configList.get(0);

        Assert.assertEquals(TestConstants.FIELD_NAME, object.getFieldName());
        Assert.assertEquals(TestConstants.COMPREHEND_OPERATIONS, object.getComprehendOperations());
        Assert.assertEquals(TestConstants.LANGUAGE_CODE, object.getLanguageCode());
    }

    @Test
    public void succeedDeserializeConfigList() {
        configRequest = test.deserialize(TestConstants.CONFIG_LIST);
        configList = configRequest.getComprehendConfigurations();

        assertEquals(2, configList.size());

        ComprehendConfiguration object1 = configList.get(0);
        assertEquals(TestConstants.FIELD_NAME, object1.getFieldName());
        assertEquals(TestConstants.COMPREHEND_OPERATIONS, object1.getComprehendOperations());
        assertEquals(TestConstants.LANGUAGE_CODE, object1.getLanguageCode());

        ComprehendConfiguration object2 = configList.get(1);
        assertEquals(TestConstants.NEW_FIELD_NAME, object2.getFieldName());
        assertEquals(TestConstants.NEW_OPERATIONS, object2.getComprehendOperations());
        assertEquals(TestConstants.NEW_LANGUAGE_CODE, object2.getLanguageCode());
    }

    @Test
    public void failDeserializeOnEmptyString() {
        try {
            configRequest = test.deserialize(TestConstants.EMPTY_STRING);
        } catch (InvalidRequestException e) {
            Assert.assertEquals(CustomerMessage.INPUT_NULL_OR_EMPTY_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnEmptyConfigComponent() {
        try {
            configRequest = test.deserialize(TestConstants.EMPTY_CONFIG_CONTENT);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnEmptyConfigList() {
        try {
            configRequest = test.deserialize(TestConstants.EMPTY_CONFIG_LIST);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnMissingConfigKey() {
        try {
            configRequest = test.deserialize(TestConstants.MISSING_CONFIG_KEY);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnMissingConfigComponent() {
        try {
            configRequest = test.deserialize(TestConstants.MISSING_CONFIG_COMPONENT);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnEmptyComprehendOperationList() {
        try {
            configRequest = test.deserialize(TestConstants.EMPTY_COMPREHEND_OPERATION_LIST);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnWrongConfigComponent() {
        try {
            configRequest = test.deserialize(TestConstants.WRONG_CONFIG_COMPONENT);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnWrongLanguageCode() {
        try {
            configRequest = test.deserialize(TestConstants.WRONG_LANGUAGE_CODE);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    @Test
    public void failDeserializeOnWrongComprehendOperation() {
        try {
            configRequest = test.deserialize(TestConstants.WRONG_COMPREHEND_OPERATION);
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_MALFORMED_ERROR, e.getMessage());
        }
    }

    // Serialize
    @Test
    public void SucceedSerializeConfigMap() {
        String configStr = test.serialize(TestConstants.getListConfigRequest());
        assertEquals(configStr, TestConstants.getPlainText(TestConstants.CONFIG_LIST));
    }

    @Test(expected = InvalidRequestException.class)
    public void FailedToSerializeConfigMap() throws IOException {
        ObjectMapper mockBadMapper = mock(ObjectMapper.class);
        when(mockBadMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException(TestConstants.DUMMY_ERROR_MESSAGE) {});
        test = new ConfigSerializer(mockBadMapper);
        test.serialize(TestConstants.getConfigRequest());
    }

    // Transform Config Request to ConfigMap
    @Test
    public void SucceedTransformConfigRequestToConfigMap() {
        configMap = ConfigSerializer.transformConfigRequestToConfigMap(TestConstants.getConfigRequest());

        assertEquals(1, configMap.size());
        ComprehendConfiguration object = configMap.get(
                String.format("%s_%s", TestConstants.INDEX_NAME, TestConstants.FIELD_NAME));

        Assert.assertEquals(TestConstants.FIELD_NAME, object.getFieldName());
        Assert.assertEquals(TestConstants.COMPREHEND_OPERATIONS, object.getComprehendOperations());
        Assert.assertEquals(TestConstants.LANGUAGE_CODE, object.getLanguageCode());
    }

    @Test
    public void FailedToTransformConfigRequestToConfigMapEmptyFields() {
        try {
            configMap = ConfigSerializer.transformConfigRequestToConfigMap(TestConstants.getEmptyFieldConfigRequest());
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_FIELD_MISSING_OR_EMPTY_ERROR, e.getMessage());
        }
    }

    @Test
    public void FailedToTransformConfigRequestToConfigMapDuplicateName() {
        try {
            configMap = ConfigSerializer.transformConfigRequestToConfigMap(TestConstants.getDuplicateConfigRequest());
        } catch (InvalidRequestException e) {
            assertEquals(CustomerMessage.CONFIG_FIELD_NAME_DUPLICATED_ERROR, e.getMessage());
        }
    }

    // Transform ConfigMap to Config Request
    @Test
    public void SucceedTransformConfigMapToConfigRequest() {
        configRequest = ConfigSerializer.transformConfigMapToConfigRequest(TestConstants.getConfigMap());
        configList = configRequest.getComprehendConfigurations();

        assertEquals(1, configList.size());
        ComprehendConfiguration object = configList.get(0);

        Assert.assertEquals(TestConstants.FIELD_NAME, object.getFieldName());
        Assert.assertEquals(TestConstants.COMPREHEND_OPERATIONS, object.getComprehendOperations());
        Assert.assertEquals(TestConstants.LANGUAGE_CODE, object.getLanguageCode());
    }

}
