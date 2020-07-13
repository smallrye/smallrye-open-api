package io.smallrye.openapi.api.models.media;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Discriminator;

import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Discriminator} OpenAPI model interface.
 */
public class DiscriminatorImpl implements Discriminator, ModelImpl {

    private String propertyName;
    private Map<String, String> mapping;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#getPropertyName()
     */
    @Override
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#setPropertyName(java.lang.String)
     */
    @Override
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#addMapping(java.lang.String, java.lang.String)
     */
    @Override
    public Discriminator addMapping(String name, String value) {
        this.mapping = ModelUtil.add(name, value, this.mapping, LinkedHashMap<String, String>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#removeMapping(java.lang.String)
     */
    @Override
    public void removeMapping(String name) {
        ModelUtil.remove(this.mapping, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#mapping(java.util.Map)
     */
    @Override
    public Discriminator mapping(Map<String, String> mapping) {
        ModelUtil.replace(mapping, LinkedHashMap<String, String>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#getMapping()
     */
    @Override
    public Map<String, String> getMapping() {
        return ModelUtil.unmodifiableMap(this.mapping);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#setMapping(java.util.Map)
     */
    @Override
    public void setMapping(Map<String, String> mapping) {
        this.mapping = ModelUtil.replace(mapping, LinkedHashMap<String, String>::new);
    }

}
