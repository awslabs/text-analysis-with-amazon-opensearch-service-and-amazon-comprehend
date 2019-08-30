package com.amazonaws.comprehend.esproxy.lambda.modules;

import com.google.inject.AbstractModule;

/**
 * Inject objects into ElasticsearchProxyRequestHandler
 */
public class RequestHandlerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new ClientModule());
        install(new ConfigurationModule());
        install(new UtilityModule());
        install(new ProcessorModule());
    }
}