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

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link MediaType} OpenAPI model interface.
 */
public class MediaTypeImpl extends ExtensibleImpl<MediaType> implements MediaType, ModelImpl {

    private Schema schema;
    private Object example;
    private Map<String, Example> examples;
    private Map<String, Encoding> encoding;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return ModelUtil.unmodifiableMap(this.examples);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = ModelUtil.replace(examples, LinkedHashMap<String, Example>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#addExample(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public MediaType addExample(String key, Example example) {
        this.examples = ModelUtil.add(key, example, this.examples, LinkedHashMap<String, Example>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#removeExample(java.lang.String)
     */
    @Override
    public void removeExample(String key) {
        ModelUtil.remove(this.examples, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getEncoding()
     */
    @Override
    public Map<String, Encoding> getEncoding() {
        return ModelUtil.unmodifiableMap(this.encoding);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setEncoding(java.util.Map)
     */
    @Override
    public void setEncoding(Map<String, Encoding> encoding) {
        this.encoding = ModelUtil.replace(encoding, LinkedHashMap<String, Encoding>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#addEncoding(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.media.Encoding)
     */
    @Override
    public MediaType addEncoding(String key, Encoding encodingItem) {
        this.encoding = ModelUtil.add(key, encodingItem, this.encoding, LinkedHashMap<String, Encoding>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#removeEncoding(java.lang.String)
     */
    @Override
    public void removeEncoding(String key) {
        ModelUtil.remove(this.encoding, key);
    }

}
