package io.smallrye.openapi.api.models.responses;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link APIResponses} OpenAPI model interface.
 */
public class APIResponsesImpl extends ExtensibleImpl<APIResponses> implements APIResponses, ModelImpl {

    private Map<String, APIResponse> apiResponses;

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#addAPIResponse(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public APIResponses addAPIResponse(String name, APIResponse apiResponse) {
        this.apiResponses = ModelUtil.add(name, apiResponse, this.apiResponses, LinkedHashMap<String, APIResponse>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#removeAPIResponse(java.lang.String)
     */
    @Override
    public void removeAPIResponse(String name) {
        ModelUtil.remove(this.apiResponses, name);
    }

    @Override
    public Map<String, APIResponse> getAPIResponses() {
        return ModelUtil.unmodifiableMap(this.apiResponses);
    }

    @Override
    public void setAPIResponses(Map<String, APIResponse> items) {
        this.apiResponses = ModelUtil.replace(items, LinkedHashMap<String, APIResponse>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#getDefaultValue()
     */
    @Override
    public APIResponse getDefaultValue() {
        return getAPIResponse(DEFAULT);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#setDefaultValue(org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public void setDefaultValue(APIResponse defaultValue) {
        if (defaultValue == null) {
            removeAPIResponse(DEFAULT);
        } else {
            addAPIResponse(DEFAULT, defaultValue);
        }
    }

}
