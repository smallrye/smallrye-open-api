package io.smallrye.openapi.api.models.servers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;

import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link ServerVariables} OpenAPI model interface.
 */
//TODO: Remove class for MicroProfile OpenAPI 2.0
@Deprecated
public class ServerVariablesImpl extends LinkedHashMap<String, ServerVariable> implements ServerVariables, ModelImpl {

    private static final long serialVersionUID = -7724841358483233927L;

    private Map<String, Object> extensions;

    public ServerVariablesImpl() {
        super();
    }

    public ServerVariablesImpl(Map<String, ServerVariable> elements) {
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
    public ServerVariables addExtension(String name, Object value) {
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
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariables#addServerVariable(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.servers.ServerVariable)
     */
    @Override
    public ServerVariables addServerVariable(String name, ServerVariable serverVariable) {
        if (serverVariable == null) {
            return this;
        }
        this.put(name, serverVariable);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariables#removeServerVariable(java.lang.String)
     */
    @Override
    public void removeServerVariable(String name) {
        this.remove(name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariables#getServerVariables()
     */
    @Override
    public Map<String, ServerVariable> getServerVariables() {
        return Collections.unmodifiableMap(this);
    }

    @Override
    public void setServerVariables(Map<String, ServerVariable> items) {
        this.clear();
        this.putAll(items);
    }
}
