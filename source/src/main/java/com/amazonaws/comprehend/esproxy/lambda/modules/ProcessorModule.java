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

package com.amazonaws.comprehend.esproxy.lambda.modules;

import com.amazonaws.comprehend.esproxy.lambda.processor.BulkProcessor;
import com.amazonaws.comprehend.esproxy.lambda.processor.PreprocessingConfigProcessor;
import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
import com.amazonaws.comprehend.esproxy.lambda.processor.DefaultProcessor;
import com.amazonaws.comprehend.esproxy.lambda.processor.IndexProcessor;
import com.amazonaws.comprehend.esproxy.lambda.processor.OpenSearchServiceProcessor;
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
 * Build OpenSearchServiceProcessors
 */
public class ProcessorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OpenSearchServiceProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.PREPROCESSING_CONFIG_PROCESSOR))
                .to(PreprocessingConfigProcessor.class);
        bind(OpenSearchServiceProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.INDEX_PROCESSOR)).to(IndexProcessor.class);
        bind(OpenSearchServiceProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.BULK_PROCESSOR)).to(BulkProcessor.class);
        bind(OpenSearchServiceProcessor.class)
                .annotatedWith(Names.named(ModuleConstants.DEFAULT_PROCESSOR)).to(DefaultProcessor.class);
    }

    @Provides
    @Singleton
    public PreprocessingConfigProcessor buildPreprocessingConfigProcessor(final ConfigSerializer configSerializer,
                                                                          final OpenSearchServiceClient esClient,
                                                                          final ConfigRetriever configRetriever,
                                                                          final KibanaUploader kibanaUploader) {
        return new PreprocessingConfigProcessor(configSerializer, esClient, configRetriever, kibanaUploader);
    }

    @Provides
    @Singleton
    public IndexProcessor buildIndexProcessor(final AmazonComprehend comprehendClient,
                                              final OpenSearchServiceClient esClient,
                                              final IngestionSerializer ingestionSerializer,
                                              final ConfigRetriever configRetriever,
                                              final ExecutorService executorService) {
        return new IndexProcessor(ingestionSerializer, comprehendClient, esClient, configRetriever, executorService);
    }

    @Provides
    @Singleton
    public BulkProcessor buildBulkProcessor(final AmazonComprehend comprehendClient,
                                            final OpenSearchServiceClient esClient,
                                            final IngestionSerializer ingestionSerializer,
                                            final ConfigRetriever configRetriever,
                                            final ExecutorService executorService) {
        return new BulkProcessor(ingestionSerializer, comprehendClient, esClient, configRetriever, executorService);
    }

    @Provides
    @Singleton
    public DefaultProcessor buildDefaultProcessor(final OpenSearchServiceClient esClient) {
        return new DefaultProcessor(esClient);
    }

}
