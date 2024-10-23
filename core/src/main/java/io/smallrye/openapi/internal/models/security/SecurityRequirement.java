package io.smallrye.openapi.internal.models.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecurityRequirement extends AbstractSecurityRequirement {

    /** {@inheritDoc} */
    @Override
    public AbstractSecurityRequirement addScheme(String newKey, java.util.List<String> newValue) {
        if (newValue == null) {
            newValue = new ArrayList<>();
        }
        super.addScheme(newKey, newValue);
        return this;
    }

    @Override
    public org.eclipse.microprofile.openapi.models.security.SecurityRequirement addScheme(String securitySchemeName,
            String scope) {
        if (scope == null) {
            addScheme(securitySchemeName, (List<String>) null);
        } else {
            addScheme(securitySchemeName, new ArrayList<>(Collections.singletonList(scope)));
        }
        return this;
    }

    @Override
    public org.eclipse.microprofile.openapi.models.security.SecurityRequirement addScheme(String securitySchemeName) {
        addScheme(securitySchemeName, (List<String>) null);
        return this;
    }

}
