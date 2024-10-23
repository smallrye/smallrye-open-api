package io.smallrye.openapi.api.models.responses;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;

import io.smallrye.openapi.api.models.MapModel;
import io.smallrye.openapi.internal.models.responses.APIResponses;

/**
 * @deprecated use {@link org.eclipse.microprofile.openapi.OASFactory#createAPIResponses()} instead.
 */
@Deprecated(since = "4.0", forRemoval = true)
public class APIResponsesImpl extends APIResponses implements MapModel<APIResponse> { // NOSONAR

    // Begin Methods to support implementation of Map for MicroProfile OpenAPI 1.1

    @Override
    public Map<String, APIResponse> getMap() {
        return getAPIResponses();
    }

    @Override
    public void setMap(Map<String, APIResponse> map) {
        setAPIResponses(map);
    }

    @Override
    public APIResponse get(Object key) {
        return MapModel.super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return MapModel.super.containsKey(key);
    }

    @Override
    public APIResponse put(String key, APIResponse value) {
        return MapModel.super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends APIResponse> m) {
        MapModel.super.putAll(m);
    }

    @Override
    public APIResponse remove(Object key) {
        return MapModel.super.remove(key);
    }

    // End Methods to support implementation of Map for MicroProfile OpenAPI 1.1
}
