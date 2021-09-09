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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.comprehend.esproxy.lambda.client.OpenSearchServiceClient;
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
 *  Build OpenSearchServiceClient & AmazonComprehend client
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
    public OpenSearchServiceClient buildOpenSearchServiceClient(final HttpRequestInterceptor interceptor,
                                                                @Named(ModuleConstants.OPEN_SEARCH_SERVICE_DOMAIN_ENDPOINT)
                                                        final String opensearchDomainEndpoint) {
        final Header[] headers = new Header[]{new BasicHeader(Constants.USER_AGENT_NAME,
                Constants.USER_AGENT_VALUE)};
        final String endpointWithProtocol = String.format("%s%s", Constants.PROTOCOL, opensearchDomainEndpoint);

        RestClient restClient = RestClient.builder(HttpHost.create(endpointWithProtocol))
                .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor))
                .setDefaultHeaders(headers)
                .build();
        return new OpenSearchServiceClient(restClient);
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
