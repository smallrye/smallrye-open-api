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

package io.smallrye.openapi.api.models.media;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;

import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Content} OpenAPI model interface.
 */
public class ContentImpl extends LinkedHashMap<String, MediaType> implements Content, ModelImpl {

    private static final long serialVersionUID = -8680275279421417582L;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#addMediaType(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.media.MediaType)
     */
    @Override
    public Content addMediaType(String name, MediaType mediaType) {
        if (mediaType == null) {
            return this;
        }
        this.put(name, mediaType);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#removeMediaType(java.lang.String)
     */
    @Override
    public void removeMediaType(String name) {
        this.remove(name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#getMediaTypes()
     */
    @Override
    public Map<String, MediaType> getMediaTypes() {
        return Collections.unmodifiableMap(this);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Content#setMediaTypes(java.util.Map)
     */
    @Override
    public void setMediaTypes(Map<String, MediaType> mediaTypes) {
        this.clear();
        this.putAll(mediaTypes);
    }

}