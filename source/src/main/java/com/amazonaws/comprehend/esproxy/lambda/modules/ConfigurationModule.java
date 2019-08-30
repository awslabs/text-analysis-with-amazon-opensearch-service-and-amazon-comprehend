package com.amazonaws.comprehend.esproxy.lambda.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

import static com.amazonaws.comprehend.esproxy.lambda.modules.ModuleConstants.ELASTICSEARCH_DOMAIN_ENDPOINT;
import static com.amazonaws.comprehend.esproxy.lambda.modules.ModuleConstants.REGION;

/**
 *  Build Configurations from lambda environment variables
 */
public class ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Named(REGION)
    public String getRegion() {
        return System.getenv(REGION);
    }

    @Provides
    @Singleton
    @Named(ELASTICSEARCH_DOMAIN_ENDPOINT)
    public String getSmartDomainEndpoint() {
        return System.getenv(ELASTICSEARCH_DOMAIN_ENDPOINT);
    }

}
