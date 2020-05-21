package io.smallrye.openapi.api.models.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.Scopes;

import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Scopes} OpenAPI model interface.
 */
//TODO: Remove class for MicroProfile OpenAPI 2.0
public class ScopesImpl extends LinkedHashMap<String, String> implements Scopes, ModelImpl {

    private static final long serialVersionUID = -6449984041086619713L;

    private Map<String, Object> extensions;

    public ScopesImpl() {
        super();
    }

    public ScopesImpl(Map<String, String> elements) {
        super(elements);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#getExtensions()
     */
    @Override
    public Map<String, Object> getExtensions() {
        return (this.extensions == null) ? null : Collections.unmodifiableMap(this.extensions);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#addExtension(java.lang.String, java.lang.Object)
     */
    @Override
    public Scopes addExtension(String name, Object value) {
        if (value == null) {
            return this;
        }
        if (extensions == null) {
            this.extensions = new LinkedHashMap<>();
        }
        this.extensions.put(name, value);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#removeExtension(java.lang.String)
     */
    @Override
    public void removeExtension(String name) {
        if (this.extensions != null) {
            this.extensions.remove(name);
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#setExtensions(java.util.Map)
     */
    @Override
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = (extensions == null) ? null : new LinkedHashMap<>(extensions);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.Scopes#addScope(java.lang.String, java.lang.String)
     */
    @Override
    public Scopes addScope(String scope, String description) {
        if (scope == null) {
            return this;
        }
        this.put(scope, description);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.Scopes#removeScope(java.lang.String)
     */
    @Override
    public void removeScope(String scope) {
        this.remove(scope);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.Scopes#getScopes()
     */
    @Override
    public Map<String, String> getScopes() {
        return Collections.unmodifiableMap(this);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.Scopes#setScopes(java.util.Map)
     */
    @Override
    public void setScopes(Map<String, String> items) {
        this.clear();
        this.putAll(items);
    }
}
