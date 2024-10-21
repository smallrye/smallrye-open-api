package io.smallrye.openapi.api.models.media;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.MediaType;

import io.smallrye.openapi.api.models.MapModel;
import io.smallrye.openapi.internal.models.media.Content;

/**
 * @deprecated use {@link org.eclipse.microprofile.openapi.OASFactory#createParameter()} instead.
 */
@Deprecated(since = "4.0", forRemoval = true)
public class ContentImpl extends Content implements MapModel<MediaType> { // NOSONAR

    // Begin Methods to support implementation of Map for MicroProfile OpenAPI 1.1

    @Override
    public Map<String, MediaType> getMap() {
        return getMediaTypes();
    }

    @Override
    public void setMap(Map<String, MediaType> map) {
        setMediaTypes(map);
    }

    @Override
    public MediaType get(Object key) {
        return MapModel.super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return MapModel.super.containsKey(key);
    }

    @Override
    public MediaType put(String key, MediaType value) {
        return MapModel.super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends MediaType> m) {
        MapModel.super.putAll(m);
    }

    @Override
    public MediaType remove(Object key) {
        return MapModel.super.remove(key);
    }

    // End Methods to support implementation of Map for MicroProfile OpenAPI 1.1
}
