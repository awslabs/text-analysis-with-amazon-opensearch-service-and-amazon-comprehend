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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

import static com.amazonaws.comprehend.esproxy.lambda.modules.ModuleConstants.OPEN_SEARCH_SERVICE_DOMAIN_ENDPOINT;
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
    @Named(OPEN_SEARCH_SERVICE_DOMAIN_ENDPOINT)
    public String getSmartDomainEndpoint() {
        return System.getenv(OPEN_SEARCH_SERVICE_DOMAIN_ENDPOINT);
    }

}
