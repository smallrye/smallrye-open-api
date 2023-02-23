package io.smallrye.openapi.api.models.media;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;

import io.smallrye.openapi.api.models.MapModel;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Content} OpenAPI model interface.
 */
public class ContentImpl implements Content, ModelImpl, MapModel<MediaType> {

    private Map<String, MediaType> mediaTypes;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#addMediaType(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.media.MediaType)
     */
    @Override
    public Content addMediaType(String name, MediaType mediaType) {
        this.mediaTypes = ModelUtil.add(name, mediaType, this.mediaTypes, LinkedHashMap<String, MediaType>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#removeMediaType(java.lang.String)
     */
    @Override
    public void removeMediaType(String name) {
        ModelUtil.remove(this.mediaTypes, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#getMediaTypes()
     */
    @Override
    public Map<String, MediaType> getMediaTypes() {
        return ModelUtil.unmodifiableMap(this.mediaTypes);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#setMediaTypes(java.util.Map)
     */
    @Override
    public void setMediaTypes(Map<String, MediaType> mediaTypes) {
        this.mediaTypes = ModelUtil.replace(mediaTypes, LinkedHashMap<String, MediaType>::new);
    }

    // Begin Methods to support implementation of Map for MicroProfile OpenAPI 1.1

    @Override
    public Map<String, MediaType> getMap() {
        return this.mediaTypes;
    }

    @Override
    public void setMap(Map<String, MediaType> map) {
        this.mediaTypes = map;
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
