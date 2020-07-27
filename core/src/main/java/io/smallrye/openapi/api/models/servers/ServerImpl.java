package io.smallrye.openapi.api.models.servers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Server} OpenAPI model interface.
 */
public class ServerImpl extends ExtensibleImpl<Server> implements Server, ModelImpl {

    private String url;
    private String description;
    private Map<String, ServerVariable> variables;

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#getVariables()
     */
    @Override
    public Map<String, ServerVariable> getVariables() {
        return ModelUtil.unmodifiableMap(this.variables);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#setVariables(java.util.Map)
     */
    @Override
    public void setVariables(Map<String, ServerVariable> variables) {
        this.variables = ModelUtil.replace(variables, LinkedHashMap<String, ServerVariable>::new);
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.servers.Server#addVariable(java.lang.String, ServerVariable)
     */
    @Override
    public Server addVariable(String variableName, ServerVariable variable) {
        this.variables = ModelUtil.add(variableName, variable, this.variables, LinkedHashMap<String, ServerVariable>::new);
        return this;
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.servers.Server#removeVariable(java.lang.String)
     */
    @Override
    public void removeVariable(String variableName) {
        ModelUtil.remove(this.variables, variableName);
    }
}