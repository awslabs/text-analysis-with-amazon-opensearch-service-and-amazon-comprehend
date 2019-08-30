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
