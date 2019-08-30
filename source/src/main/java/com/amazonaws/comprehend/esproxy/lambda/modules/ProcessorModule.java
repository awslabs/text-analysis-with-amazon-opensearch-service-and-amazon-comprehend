package com.amazonaws.comprehend.esproxy.lambda.modules;

import com.amazonaws.comprehend.esproxy.lambda.processor.BulkProcessor;
import com.amazonaws.comprehend.esproxy.lambda.processor.PreprocessingConfigProcessor;
import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.processor.DefaultProcessor;
import com.amazonaws.comprehend.esproxy.lambda.processor.IndexProcessor;
import com.amazonaws.comprehend.esproxy.lambda.processor.ElasticsearchProcessor;
import com.amazonaws.comprehend.esproxy.lambda.utils.ConfigRetriever;
import com.amazonaws.comprehend.esproxy.lambda.utils.kibana.KibanaUploader;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.ConfigSerializer;
import com.amazonaws.comprehend.esproxy.lambda.utils.serializer.IngestionSerializer;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

/**
 * Build ElasticsearchProcessors
 */
public class ProcessorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ElasticsearchProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.PREPROCESSING_CONFIG_PROCESSOR))
                .to(PreprocessingConfigProcessor.class);
        bind(ElasticsearchProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.INDEX_PROCESSOR)).to(IndexProcessor.class);
        bind(ElasticsearchProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.BULK_PROCESSOR)).to(BulkProcessor.class);
        bind(ElasticsearchProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.DEFAULT_PROCESSOR)).to(DefaultProcessor.class);
    }

    @Provides
    @Singleton
    public PreprocessingConfigProcessor buildPreprocessingConfigProcessor(final ConfigSerializer configSerializer,
                                                                          final ElasticsearchClient esClient,
                                                                          final ConfigRetriever configRetriever,
                                                                          final KibanaUploader kibanaUploader) {
        return new PreprocessingConfigProcessor(configSerializer, esClient, configRetriever, kibanaUploader);
    }

    @Provides
    @Singleton
    public IndexProcessor buildIndexProcessor(final AmazonComprehend comprehendClient,
                                              final ElasticsearchClient esClient,
                                              final IngestionSerializer ingestionSerializer,
                                              final ConfigRetriever configRetriever,
                                              final ExecutorService executorService) {
        return new IndexProcessor(ingestionSerializer, comprehendClient, esClient, configRetriever, executorService);
    }

    @Provides
    @Singleton
    public BulkProcessor buildBulkProcessor(final AmazonComprehend comprehendClient,
                                            final ElasticsearchClient esClient,
                                            final IngestionSerializer ingestionSerializer,
                                            final ConfigRetriever configRetriever,
                                            final ExecutorService executorService) {
        return new BulkProcessor(ingestionSerializer, comprehendClient, esClient, configRetriever, executorService);
    }

    @Provides
    @Singleton
    public DefaultProcessor buildDefaultProcessor(final ElasticsearchClient esClient) {
        return new DefaultProcessor(esClient);
    }

}
