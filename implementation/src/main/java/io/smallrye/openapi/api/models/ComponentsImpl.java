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

package io.smallrye.openapi.api.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Components} OpenAPI model interface.
 */
public class ComponentsImpl extends ExtensibleImpl<Components> implements Components, ModelImpl {

    private Map<String, Schema> schemas;
    private Map<String, APIResponse> responses;
    private Map<String, Parameter> parameters;
    private Map<String, Example> examples;
    private Map<String, RequestBody> requestBodies;
    private Map<String, Header> headers;
    private Map<String, SecurityScheme> securitySchemes;
    private Map<String, Link> links;
    private Map<String, Callback> callbacks;

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getSchemas()
     */
    @Override
    public Map<String, Schema> getSchemas() {
        return ModelUtil.unmodifiableMap(this.schemas);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setSchemas(java.util.Map)
     */
    @Override
    public void setSchemas(Map<String, Schema> schemas) {
        this.schemas = ModelUtil.replace(schemas, LinkedHashMap<String, Schema>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addSchema(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Components addSchema(String key, Schema schema) {
        this.schemas = ModelUtil.add(key, schema, this.schemas, LinkedHashMap<String, Schema>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeSchema(java.lang.String)
     */
    @Override
    public void removeSchema(String key) {
        ModelUtil.remove(this.schemas, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getResponses()
     */
    @Override
    public Map<String, APIResponse> getResponses() {
        return ModelUtil.unmodifiableMap(this.responses);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setResponses(java.util.Map)
     */
    @Override
    public void setResponses(Map<String, APIResponse> responses) {
        this.responses = ModelUtil.replace(responses, LinkedHashMap<String, APIResponse>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addResponse(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public Components addResponse(String key, APIResponse response) {
        this.responses = ModelUtil.add(key, response, this.responses, LinkedHashMap<String, APIResponse>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeResponse(java.lang.String)
     */
    @Override
    public void removeResponse(String key) {
        ModelUtil.remove(this.responses, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getParameters()
     */
    @Override
    public Map<String, Parameter> getParameters() {
        return ModelUtil.unmodifiableMap(this.parameters);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = ModelUtil.replace(parameters, LinkedHashMap<String, Parameter>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addParameter(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public Components addParameter(String key, Parameter parameter) {
        this.parameters = ModelUtil.add(key, parameter, this.parameters, LinkedHashMap<String, Parameter>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeParameter(java.lang.String)
     */
    @Override
    public void removeParameter(String key) {
        ModelUtil.remove(this.parameters, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return ModelUtil.unmodifiableMap(this.examples);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = ModelUtil.replace(examples, LinkedHashMap<String, Example>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addExample(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public Components addExample(String key, Example example) {
        this.examples = ModelUtil.add(key, example, this.examples, LinkedHashMap<String, Example>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeExample(java.lang.String)
     */
    @Override
    public void removeExample(String key) {
        ModelUtil.remove(this.examples, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getRequestBodies()
     */
    @Override
    public Map<String, RequestBody> getRequestBodies() {
        return ModelUtil.unmodifiableMap(this.requestBodies);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setRequestBodies(java.util.Map)
     */
    @Override
    public void setRequestBodies(Map<String, RequestBody> requestBodies) {
        this.requestBodies = ModelUtil.replace(requestBodies, LinkedHashMap<String, RequestBody>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addRequestBody(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.parameters.RequestBody)
     */
    @Override
    public Components addRequestBody(String key, RequestBody requestBody) {
        this.requestBodies = ModelUtil.add(key, requestBody, this.requestBodies, LinkedHashMap<String, RequestBody>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeRequestBody(java.lang.String)
     */
    @Override
    public void removeRequestBody(String key) {
        ModelUtil.remove(this.requestBodies, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return ModelUtil.unmodifiableMap(this.headers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = ModelUtil.replace(headers, LinkedHashMap<String, Header>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addHeader(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.headers.Header)
     */
    @Override
    public Components addHeader(String key, Header header) {
        this.headers = ModelUtil.add(key, header, this.headers, LinkedHashMap<String, Header>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeHeader(java.lang.String)
     */
    @Override
    public void removeHeader(String key) {
        ModelUtil.remove(this.headers, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getSecuritySchemes()
     */
    @Override
    public Map<String, SecurityScheme> getSecuritySchemes() {
        return ModelUtil.unmodifiableMap(this.securitySchemes);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setSecuritySchemes(java.util.Map)
     */
    @Override
    public void setSecuritySchemes(Map<String, SecurityScheme> securitySchemes) {
        this.securitySchemes = ModelUtil.replace(securitySchemes, LinkedHashMap<String, SecurityScheme>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addSecurityScheme(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.security.SecurityScheme)
     */
    @Override
    public Components addSecurityScheme(String key, SecurityScheme securityScheme) {
        this.securitySchemes = ModelUtil.add(key, securityScheme, this.securitySchemes,
                LinkedHashMap<String, SecurityScheme>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeSecurityScheme(java.lang.String)
     */
    @Override
    public void removeSecurityScheme(String key) {
        ModelUtil.remove(this.securitySchemes, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getLinks()
     */
    @Override
    public Map<String, Link> getLinks() {
        return ModelUtil.unmodifiableMap(this.links);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setLinks(java.util.Map)
     */
    @Override
    public void setLinks(Map<String, Link> links) {
        this.links = ModelUtil.replace(links, LinkedHashMap<String, Link>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addLink(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.links.Link)
     */
    @Override
    public Components addLink(String key, Link link) {
        this.links = ModelUtil.add(key, link, this.links, LinkedHashMap<String, Link>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeLink(java.lang.String)
     */
    @Override
    public void removeLink(String key) {
        ModelUtil.remove(this.links, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getCallbacks()
     */
    @Override
    public Map<String, Callback> getCallbacks() {
        return ModelUtil.unmodifiableMap(this.callbacks);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setCallbacks(java.util.Map)
     */
    @Override
    public void setCallbacks(Map<String, Callback> callbacks) {
        this.callbacks = ModelUtil.replace(callbacks, LinkedHashMap<String, Callback>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addCallback(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.callbacks.Callback)
     */
    @Override
    public Components addCallback(String key, Callback callback) {
        this.callbacks = ModelUtil.add(key, callback, this.callbacks, LinkedHashMap<String, Callback>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#removeCallback(java.lang.String)
     */
    @Override
    public void removeCallback(String key) {
        ModelUtil.remove(this.callbacks, key);
    }

}