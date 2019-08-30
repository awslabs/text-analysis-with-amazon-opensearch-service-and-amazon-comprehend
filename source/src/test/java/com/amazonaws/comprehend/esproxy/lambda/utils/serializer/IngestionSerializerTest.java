package com.amazonaws.comprehend.esproxy.lambda.utils.serializer;

import com.amazonaws.comprehend.esproxy.lambda.exception.InternalErrorException;
import com.amazonaws.comprehend.esproxy.lambda.utils.TestConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IngestionSerializerTest {
    final private ObjectMapper mapper = new ObjectMapper();

    private IngestionSerializer test;

    @Before
    public void setup() {
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
    }

    @Test
    public void succeedDeserializeJsonNode() {
        test = new IngestionSerializer(mapper);
        JsonNode result = test.deserialize(TestConstants.CUSTOMER_INGESTION_PAYLOAD);
        assertEquals(result.get(TestConstants.CUSTOMER_INGESTION_PAYLOAD_NAME_KEY).asText(),
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_NAME);
        assertEquals(result.get(TestConstants.CUSTOMER_INGESTION_PAYLOAD_LOCATION_KEY).asText(),
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_LOCATION);
        assertEquals(result.get(TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT_KEY).asText(),
                TestConstants.CUSTOMER_INGESTION_PAYLOAD_TEXT);
    }

    @Test
    public void succeedSerializeJsonString() throws IOException {
        test = new IngestionSerializer(mapper);
        JsonNode jsonNode = mapper.readTree(TestConstants.CUSTOMER_INGESTION_PAYLOAD);
        String result = test.serialize(jsonNode);
        assertEquals(TestConstants.getPlainText(TestConstants.CUSTOMER_INGESTION_PAYLOAD),
                TestConstants.getPlainText(result));
    }

    @Test(expected = InternalErrorException.class)
    public void failedDeserializeJsonNode() throws IOException {
        ObjectMapper mockBadMapper = mock(ObjectMapper.class);
        when(mockBadMapper.readTree(anyString())).thenThrow(new IOException());
        test = new IngestionSerializer(mockBadMapper);
        test.deserialize(TestConstants.CUSTOMER_INGESTION_PAYLOAD);
    }

    @Test(expected = InternalErrorException.class)
    public void failedSerializeJsonString() throws IOException {
        ObjectMapper mockBadMapper = mock(ObjectMapper.class);
        when(mockBadMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException(TestConstants.DUMMY_ERROR_MESSAGE) {});
        test = new IngestionSerializer(mockBadMapper);
        JsonNode jsonNode = mapper.readTree(TestConstants.CUSTOMER_INGESTION_PAYLOAD);
        test.serialize(jsonNode);
    }

}
