/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.openapi.api.models.responses;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link APIResponses} OpenAPI model interface.
 */
public class APIResponsesImpl extends LinkedHashMap<String, APIResponse> implements APIResponses, ModelImpl {

    private static final long serialVersionUID = 7767651877116575739L;

    private Map<String, Object> extensions;

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#getExtensions()
     */
    @Override
    public Map<String, Object> getExtensions() {
        return this.extensions;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#addExtension(java.lang.String, java.lang.Object)
     */
    @Override
    public APIResponses addExtension(String name, Object value) {
        if (value == null) {
            return this;
        }
        if (extensions == null) {
            this.extensions = new LinkedHashMap<>();
        }
        this.extensions.put(name, value);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#removeExtension(java.lang.String)
     */
    @Override
    public void removeExtension(String name) {
        if (this.extensions != null) {
            this.extensions.remove(name);
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#setExtensions(java.util.Map)
     */
    @Override
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#addAPIResponse(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public APIResponses addAPIResponse(String name, APIResponse apiResponse) {
        if (apiResponse == null) {
            return this;
        }
        this.put(name, apiResponse);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#removeAPIResponse(java.lang.String)
     */
    @Override
    public void removeAPIResponse(String name) {
        this.remove(name);
    }

    @Override
    public Map<String, APIResponse> getAPIResponses() {
        return Collections.unmodifiableMap(this);
    }

    @Override
    public void setAPIResponses(Map<String, APIResponse> items) {
        this.clear();
        this.putAll(items);
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

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#defaultValue(org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public APIResponses defaultValue(APIResponse defaultValue) {
        setDefaultValue(defaultValue);
        return this;
    }

}