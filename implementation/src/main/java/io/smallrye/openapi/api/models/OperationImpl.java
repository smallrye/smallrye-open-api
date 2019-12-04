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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link ExternalDocumentation} OpenAPI model interface.
 */
public class OperationImpl extends ExtensibleImpl<Operation> implements Operation, ModelImpl {

    private List<String> tags;
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String operationId;
    private List<Parameter> parameters;
    private RequestBody requestBody;
    private APIResponses responses;
    private Map<String, Callback> callbacks;
    private Boolean deprecated;
    private List<SecurityRequirement> security;
    private List<Server> servers;

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getTags()
     */
    @Override
    public List<String> getTags() {
        return ModelUtil.unmodifiableList(this.tags);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setTags(java.util.List)
     */
    @Override
    public void setTags(List<String> tags) {
        this.tags = ModelUtil.replace(tags, ArrayList<String>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addTag(java.lang.String)
     */
    @Override
    public Operation addTag(String tag) {
        this.tags = ModelUtil.add(tag, this.tags, ArrayList<String>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#removeTag(String)
     */
    @Override
    public void removeTag(String tag) {
        ModelUtil.remove(this.tags, tag);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setSummary(java.lang.String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setOperationId(java.lang.String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        return ModelUtil.unmodifiableList(this.parameters);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setParameters(java.util.List)
     */
    @Override
    public void setParameters(List<Parameter> parameters) {
        this.parameters = ModelUtil.replace(parameters, ArrayList<Parameter>::new);
        ;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addParameter(org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public Operation addParameter(Parameter parameter) {
        this.parameters = ModelUtil.add(parameter, this.parameters, ArrayList<Parameter>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#removeParameter(org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public void removeParameter(Parameter parameter) {
        ModelUtil.remove(this.parameters, parameter);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getRequestBody()
     */
    @Override
    public RequestBody getRequestBody() {
        return this.requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setRequestBody(org.eclipse.microprofile.openapi.models.parameters.RequestBody)
     */
    @Override
    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getResponses()
     */
    @Override
    public APIResponses getResponses() {
        return this.responses;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setResponses(org.eclipse.microprofile.openapi.models.responses.APIResponses)
     */
    @Override
    public void setResponses(APIResponses responses) {
        this.responses = responses;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getCallbacks()
     */
    @Override
    public Map<String, Callback> getCallbacks() {
        return ModelUtil.unmodifiableMap(this.callbacks);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setCallbacks(java.util.Map)
     */
    @Override
    public void setCallbacks(Map<String, Callback> callbacks) {
        this.callbacks = ModelUtil.replace(callbacks, LinkedHashMap<String, Callback>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addCallback(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.callbacks.Callback)
     */
    @Override
    public Operation addCallback(String key, Callback callback) {
        this.callbacks = ModelUtil.add(key, callback, this.callbacks, LinkedHashMap<String, Callback>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#removeCallback(java.lang.String)
     */
    @Override
    public void removeCallback(String key) {
        ModelUtil.remove(this.callbacks, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getSecurity()
     */
    @Override
    public List<SecurityRequirement> getSecurity() {
        return ModelUtil.unmodifiableList(this.security);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setSecurity(java.util.List)
     */
    @Override
    public void setSecurity(List<SecurityRequirement> security) {
        this.security = ModelUtil.replace(security, ArrayList<SecurityRequirement>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addSecurityRequirement(org.eclipse.microprofile.openapi.models.security.SecurityRequirement)
     */
    @Override
    public Operation addSecurityRequirement(SecurityRequirement securityRequirement) {
        this.security = ModelUtil.add(securityRequirement, this.security, ArrayList<SecurityRequirement>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#removeSecurityRequirement(org.eclipse.microprofile.openapi.models.security.SecurityRequirement)
     */
    @Override
    public void removeSecurityRequirement(SecurityRequirement securityRequirement) {
        ModelUtil.remove(this.security, securityRequirement);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getServers()
     */
    @Override
    public List<Server> getServers() {
        return ModelUtil.unmodifiableList(this.servers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setServers(java.util.List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = ModelUtil.replace(servers, ArrayList<Server>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public Operation addServer(Server server) {
        this.servers = ModelUtil.add(server, this.servers, ArrayList<Server>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#removeServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public void removeServer(Server server) {
        ModelUtil.remove(this.servers, server);
    }

}
