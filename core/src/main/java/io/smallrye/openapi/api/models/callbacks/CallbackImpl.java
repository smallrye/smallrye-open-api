package io.smallrye.openapi.api.models.callbacks;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.MapModel;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Callback} OpenAPI model interface.
 */
public class CallbackImpl extends ExtensibleImpl<Callback> implements Callback, ModelImpl, MapModel<PathItem> {

    private String ref;
    private Map<String, PathItem> pathItems;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = ReferenceType.CALLBACK.referenceOf(ref);
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public Callback ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.callbacks.Callback#addPathItem(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.PathItem)
     */
    @Override
    public Callback addPathItem(String name, PathItem item) {
        this.pathItems = ModelUtil.add(name, item, this.pathItems);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.callbacks.Callback#removePathItem(java.lang.String)
     */
    @Override
    public void removePathItem(String name) {
        ModelUtil.remove(this.pathItems, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.callbacks.Callback#getPathItems()
     */
    @Override
    public Map<String, PathItem> getPathItems() {
        return ModelUtil.unmodifiableMap(this.pathItems);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.callbacks.Callback#setPathItems(java.util.Map)
     */
    @Override
    public void setPathItems(Map<String, PathItem> items) {
        this.pathItems = ModelUtil.replace(items);
    }

    // Begin Methods to support implementation of Map for MicroProfile OpenAPI 1.1

    @Override
    public Map<String, PathItem> getMap() {
        return this.pathItems;
    }

    @Override
    public void setMap(Map<String, PathItem> map) {
        this.pathItems = map;
    }

    @Override
    public PathItem get(Object key) {
        return MapModel.super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return MapModel.super.containsKey(key);
    }

    @Override
    public PathItem put(String key, PathItem value) {
        return MapModel.super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends PathItem> m) {
        MapModel.super.putAll(m);
    }

    @Override
    public PathItem remove(Object key) {
        return MapModel.super.remove(key);
    }

    // End Methods to support implementation of Map for MicroProfile OpenAPI 1.1
}
