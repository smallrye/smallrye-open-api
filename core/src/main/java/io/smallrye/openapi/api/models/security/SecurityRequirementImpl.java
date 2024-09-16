package io.smallrye.openapi.api.models.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;

import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link SecurityRequirement} OpenAPI model interface.
 */
public class SecurityRequirementImpl extends LinkedHashMap<String, List<String>> implements SecurityRequirement, ModelImpl {

    private static final long serialVersionUID = -2336114397712664136L;

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityRequirement#addScheme(java.lang.String, java.lang.String)
     */
    @Override
    public SecurityRequirement addScheme(String securitySchemeName, String scope) {
        if (scope == null) {
            addScheme(securitySchemeName);
        } else {
            addScheme(securitySchemeName, new ArrayList<>(Collections.singletonList(scope)));
        }
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityRequirement#addScheme(java.lang.String, java.util.List)
     */
    @Override
    public SecurityRequirement addScheme(String securitySchemeName, List<String> scopes) {
        if (scopes == null) {
            scopes = new ArrayList<>(0);
        }
        this.put(securitySchemeName, scopes);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityRequirement#addScheme(java.lang.String)
     */
    @Override
    public SecurityRequirement addScheme(String securitySchemeName) {
        addScheme(securitySchemeName, (List<String>) null);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityRequirement#removeScheme(String)
     */
    @Override
    public void removeScheme(String securitySchemeName) {
        this.remove(securitySchemeName);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityRequirement#getSchemes()
     */
    @Override
    public Map<String, List<String>> getSchemes() {
        return Collections.unmodifiableMap(this);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.SecurityRequirement#setSchemes(java.util.Map)
     */
    @Override
    public void setSchemes(Map<String, List<String>> items) {
        this.clear();
        if (items != null) {
            items.forEach(this::addScheme);
        }
    }

}
