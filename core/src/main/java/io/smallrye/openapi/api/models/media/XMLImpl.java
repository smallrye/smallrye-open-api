package io.smallrye.openapi.api.models.media;

import org.eclipse.microprofile.openapi.models.media.XML;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link XML} OpenAPI model interface.
 */
public class XMLImpl extends ExtensibleImpl<XML> implements XML, ModelImpl {

    private String name;
    private String namespace;
    private String prefix;
    private Boolean attribute;
    private Boolean wrapped;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getNamespace()
     */
    @Override
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setNamespace(java.lang.String)
     */
    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getPrefix()
     */
    @Override
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setPrefix(java.lang.String)
     */
    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getAttribute()
     */
    @Override
    public Boolean getAttribute() {
        return this.attribute;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setAttribute(java.lang.Boolean)
     */
    @Override
    public void setAttribute(Boolean attribute) {
        this.attribute = attribute;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getWrapped()
     */
    @Override
    public Boolean getWrapped() {
        return this.wrapped;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setWrapped(java.lang.Boolean)
     */
    @Override
    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }

}
