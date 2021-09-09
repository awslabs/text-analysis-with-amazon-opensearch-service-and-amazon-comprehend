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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Help to attach the comprehend result back to the bulk content
 * The contentRowNum records where to put back the fieldNameAndOperation in a batch request
 * i.e.:
 * fieldNameAndOperation = "message_DetectKeyPhrases"
 * contentRowNum = 4
 */
@AllArgsConstructor
@Getter
public class BatchFieldLocator {
    @Setter
    private String fieldNameAndOperation;

    private final int contentRowNum;
}