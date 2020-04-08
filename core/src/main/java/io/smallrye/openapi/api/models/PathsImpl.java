package io.smallrye.openapi.api.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Paths} OpenAPI model interface.
 */
public class PathsImpl extends ExtensibleImpl<Paths> implements Paths, ModelImpl {

    private Map<String, PathItem> pathItems;

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#addPathItem(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.PathItem)
     */
    @Override
    public Paths addPathItem(String name, PathItem item) {
        this.pathItems = ModelUtil.add(name, item, this.pathItems, LinkedHashMap<String, PathItem>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#removePathItem(java.lang.String)
     */
    @Override
    public void removePathItem(String name) {
        ModelUtil.remove(this.pathItems, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#getPathItems()
     */
    @Override
    public Map<String, PathItem> getPathItems() {
        return ModelUtil.unmodifiableMap(this.pathItems);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#setPathItems(java.util.Map)
     */
    @Override
    public void setPathItems(Map<String, PathItem> items) {
        this.pathItems = ModelUtil.replace(items, LinkedHashMap<String, PathItem>::new);
    }

}