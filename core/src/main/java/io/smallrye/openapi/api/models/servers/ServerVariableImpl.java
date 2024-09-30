package io.smallrye.openapi.api.models.servers;

import java.util.List;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link ServerVariable} OpenAPI model interface.
 */
public class ServerVariableImpl extends ExtensibleImpl<ServerVariable> implements ServerVariable, ModelImpl {

    private List<String> enumeration;
    private String defaultValue;
    private String description;

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#getEnumeration()
     */
    @Override
    public List<String> getEnumeration() {
        return ModelUtil.unmodifiableList(this.enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#setEnumeration(java.util.List)
     */
    @Override
    public void setEnumeration(List<String> enumeration) {
        this.enumeration = ModelUtil.replace(enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#addEnumeration(java.lang.String)
     */
    @Override
    public ServerVariable addEnumeration(String enumeration) {
        this.enumeration = ModelUtil.add(enumeration, this.enumeration);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#removeEnumeration(String)
     */
    @Override
    public void removeEnumeration(String enumeration) {
        ModelUtil.remove(this.enumeration, enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#setDefaultValue(java.lang.String)
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}
