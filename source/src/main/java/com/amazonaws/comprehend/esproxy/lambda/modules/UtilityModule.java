package com.amazonaws.comprehend.esproxy.lambda.modules;

import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.utils.ConfigRetriever;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaUploader;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ConfigSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.IngestionSerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Build ConfigRetriever, KibanaUploader and Serializers
 */
public class UtilityModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        return objectMapper;
    }

    @Provides
    @Singleton
    public ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Constants.MAX_THREAD);
    }

    // Build ConfigRetriever and KibanaUploader
    @Provides
    @Singleton
    public ConfigRetriever buildConfigRetriever(final ConfigSerializer configSerializer,
                                                final ElasticsearchClient esClient) {
        return new ConfigRetriever(configSerializer, esClient);

    }

    @Provides
    @Singleton
    public KibanaUploader buildKibanaUploader(final ElasticsearchClient esClient, final ExecutorService executorService) {
        return new KibanaUploader(esClient, executorService);
    }

    // Build Serializers
    @Provides
    @Singleton
    public ConfigSerializer buildConfigSerializer(final ObjectMapper mapper) {
        return new ConfigSerializer(mapper);
    }

    @Provides
    @Singleton
    public IngestionSerializer buildIngestionSerializer(final ObjectMapper mapper) {
        return new IngestionSerializer(mapper);
    }

}
