package io.smallrye.openapi.api.models.callbacks;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.PathItem;

import io.smallrye.openapi.api.models.MapModel;
import io.smallrye.openapi.internal.models.callbacks.Callback;

/**
 * @deprecated use {@link org.eclipse.microprofile.openapi.OASFactory#createCallback()} instead.
 */
@Deprecated(since = "4.0", forRemoval = true)
public class CallbackImpl extends Callback implements MapModel<PathItem> { // NOSONAR

    // Begin Methods to support implementation of Map for MicroProfile OpenAPI 1.1

    @Override
    public Map<String, PathItem> getMap() {
        return getPathItems();
    }

    @Override
    public void setMap(Map<String, PathItem> map) {
        setPathItems(map);
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
