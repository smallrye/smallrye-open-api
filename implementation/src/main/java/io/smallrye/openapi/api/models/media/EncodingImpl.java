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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.media.Encoding;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Encoding} OpenAPI model interface.
 */
public class EncodingImpl extends ExtensibleImpl<Encoding> implements Encoding, ModelImpl {

    private String contentType;
    private Map<String, Header> headers;
    private Style style;
    private Boolean explode;
    private Boolean allowReserved;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getContentType()
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#addHeader(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.headers.Header)
     */
    @Override
    public Encoding addHeader(String key, Header header) {
        this.headers = ModelUtil.add(key, header, this.headers, LinkedHashMap<String, Header>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#removeHeader(java.lang.String)
     */
    @Override
    public void removeHeader(String key) {
        ModelUtil.remove(this.headers, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return ModelUtil.unmodifiableMap(this.headers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = ModelUtil.replace(headers, LinkedHashMap<String, Header>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setStyle(org.eclipse.microprofile.openapi.models.media.Encoding.Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setExplode(java.lang.Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setAllowReserved(java.lang.Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

}
