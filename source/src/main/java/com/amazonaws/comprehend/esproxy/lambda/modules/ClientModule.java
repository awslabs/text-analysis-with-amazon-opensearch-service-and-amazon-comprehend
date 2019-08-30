package com.amazonaws.comprehend.esproxy.lambda.modules;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.comprehend.esproxy.lambda.client.ElasticsearchClient;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 *  Build ElasticsearchClient & AmazonComprehend client
 */
public class ClientModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public AWSCredentialsProvider getCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    @Provides
    @Singleton
    private HttpRequestInterceptor buildRequestInterceptor(final AWSCredentialsProvider credentialsProvider,
                                                           @Named(ModuleConstants.REGION) final String region) {
        String serviceName = Constants.AMAZON_ELASTICSEARCH_SERVICE_NAME;
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        return new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
    }

    @Provides
    @Singleton
    public ClientConfiguration getClientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setUserAgentSuffix(Constants.USER_AGENT_VALUE);
        return clientConfiguration;
    }

    @Provides
    @Singleton
    public ElasticsearchClient buildElasticsearchClient(final HttpRequestInterceptor interceptor,
                                                        @Named(ModuleConstants.ELASTICSEARCH_DOMAIN_ENDPOINT)
                                                        final String elasticsearchDomainEndpoint) {
        final Header[] headers = new Header[]{new BasicHeader(Constants.USER_AGENT_NAME,
                Constants.USER_AGENT_VALUE)};
        final String endpointWithProtocol = String.format("%s%s", Constants.PROTOCOL, elasticsearchDomainEndpoint);

        RestClient restClient = RestClient.builder(HttpHost.create(endpointWithProtocol))
                .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor))
                .setDefaultHeaders(headers)
                .build();
        return new ElasticsearchClient(restClient);
    }

    @Provides
    @Singleton
    public AmazonComprehend buildAmazonComprehend(final AWSCredentialsProvider credentialsProvider,
                                                  final ClientConfiguration clientConfiguration,
                                                  @Named(ModuleConstants.REGION) final String region) {
        return AmazonComprehendClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withClientConfiguration(clientConfiguration)
                .withRegion(region)
                .build();
    }

}
