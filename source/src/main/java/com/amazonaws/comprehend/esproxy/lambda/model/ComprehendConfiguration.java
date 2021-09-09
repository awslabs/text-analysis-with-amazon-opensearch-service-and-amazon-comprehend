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

package com.amazonaws.comprehend.esproxy.lambda.model;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Store each of the ComprehendConfiguration
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ComprehendConfiguration {
    @NonNull
    private String indexName;

    @NonNull
    private String fieldName;

    @NonNull
    private Set<ComprehendOperationEnum> comprehendOperations;

    @NonNull
    private LanguageCode languageCode;

    /**
     * Check whether this configuration is invalid
     * @return boolean
     */
    public boolean configIsInvalid() {
        return Strings.isNullOrEmpty(indexName) || Strings.isNullOrEmpty(fieldName) || comprehendOperations == null
                || comprehendOperations.isEmpty() || languageCode == null;
    }
}
