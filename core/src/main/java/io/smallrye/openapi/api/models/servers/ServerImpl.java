package io.smallrye.openapi.api.models.servers;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Server} OpenAPI model interface.
 */
@SuppressWarnings("deprecation")
public class ServerImpl extends ExtensibleImpl<Server> implements Server, ModelImpl {

    private String url;
    private String description;
    // TODO: Update type MicroProfile OpenAPI 2.0
    private ServerVariables variables;

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
    // TODO: Update return type MicroProfile OpenAPI 2.0
    public ServerVariables getVariables() {
        //return ModelUtil.unmodifiableMap(this.variables);
        return variables;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.Server#setVariables(java.util.Map)
     */
    @Override
    // TODO: @Override for MicroProfile OpenAPI 2.0
    public void setVariables(Map<String, ServerVariable> variables) {
        //this.variables = ModelUtil.replace(variables, LinkedHashMap<String, ServerVariable>::new);
        if (variables == null) {
            this.variables = null;
        } else {
            this.variables = new ServerVariablesImpl(variables);
        }
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.servers.Server#addVariable(java.lang.String, ServerVariable)
     */
    // TODO: @Override for MicroProfile OpenAPI 2.0 (and restore JavaDoc comment)
    public Server addVariable(String variableName, ServerVariable variable) {
        //this.variables = ModelUtil.add(variableName, variable, this.variables, LinkedHashMap<String, ServerVariable>::new);
        if (variableName == null) {
            return this;
        }
        if (this.variables == null) {
            this.variables = new ServerVariablesImpl();
        }
        variables.addServerVariable(variableName, variable);
        return this;
    }

    /*
     * @see org.eclipse.microprofile.openapi.models.servers.Server#removeVariable(java.lang.String)
     */
    // TODO: @Override for MicroProfile OpenAPI 2.0 (and restore JavaDoc comment)
    public void removeVariable(String variableName) {
        ModelUtil.remove(this.variables, variableName);
    }

    @Override
    // TODO: Remove method for MicroProfile OpenAPI 2.0
    public void setVariables(ServerVariables variables) {
        this.variables = variables;
    }
}