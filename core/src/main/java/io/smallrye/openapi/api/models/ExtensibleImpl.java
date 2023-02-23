package io.smallrye.openapi.api.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Extensible;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * Implementation of the {@link Extensible} OpenAPI model interface. Base class for many of the
 * OpenAPI models.
 *
 * @author eric.wittmann@gmail.com
 */
public abstract class ExtensibleImpl<T extends Extensible<T>> implements Extensible<T>, ModelImpl {

    private Map<String, Object> extensions;

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#getExtensions()
     */
    @Override
    public Map<String, Object> getExtensions() {
        return ModelUtil.unmodifiableMap(this.extensions);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#addExtension(java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public T addExtension(String name, Object value) {
        this.extensions = ModelUtil.add(name, value, this.extensions, LinkedHashMap<String, Object>::new);
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#removeExtension(java.lang.String)
     */
    @Override
    public void removeExtension(String name) {
        ModelUtil.remove(this.extensions, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#setExtensions(java.util.Map)
     */
    @Override
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = ModelUtil.replace(extensions, LinkedHashMap<String, Object>::new);
    }

}
