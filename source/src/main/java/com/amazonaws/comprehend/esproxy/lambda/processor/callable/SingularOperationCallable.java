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

package com.amazonaws.comprehend.esproxy.lambda.processor.callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.comprehend.esproxy.lambda.exception.ClientExceptionMessage;
import com.amazonaws.comprehend.esproxy.lambda.exception.ServiceExceptionMessage;
import com.amazonaws.comprehend.esproxy.lambda.model.ComprehendOperationEnum;
import com.amazonaws.comprehend.esproxy.lambda.model.LanguageCode;
import com.amazonaws.comprehend.esproxy.lambda.processor.response.SingularResponse;
import com.amazonaws.comprehend.esproxy.lambda.utils.Constants;
import com.amazonaws.services.comprehend.AmazonComprehend;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.concurrent.Callable;

/**
 * A Comprehend Singular operation task that returns a Comprehend Client response
 */
@RequiredArgsConstructor
public class SingularOperationCallable implements Callable<SingularResponse> {
    @NonNull
    private final String fieldNameAndOperation;

    @NonNull
    private final ComprehendOperationEnum comprehendOperation;

    @NonNull
    private final LanguageCode languageCode;

    @NonNull
    private final String content;

    @NonNull
    private final AmazonComprehend comprehendClient;

    @Override
    public SingularResponse call() {
        try {
            return comprehendOperation.getComprehendOperation()
                    .sendSingularRequest(fieldNameAndOperation, content, languageCode, comprehendClient);
        } catch (AmazonServiceException e) {
            ServiceExceptionMessage serviceException = ServiceExceptionMessage.builder()
                    .statusCode(e.getStatusCode())
                    .errorCode(e.getErrorCode())
                    .requestId(e.getRequestId())
                    .errorMessage(e.getErrorMessage())
                    .build();
            return new SingularResponse(String.format("%s_Error", fieldNameAndOperation),
                    new JSONObject(serviceException), null);
        } catch (AmazonClientException e) {
            ClientExceptionMessage clientException = ClientExceptionMessage.builder()
                    .errorCode(Constants.CLIENT_EXCEPTION_ERROR_CODE)
                    .errorMessage(e.getMessage())
                    .build();
            return new SingularResponse(String.format("%s_Error", fieldNameAndOperation),
                    new JSONObject(clientException), null);
        }
    }

}
