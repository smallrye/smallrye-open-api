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

package io.smallrye.openapi.api.models.servers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;

import io.smallrye.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link ServerVariables} OpenAPI model interface.
 */
public class ServerVariablesImpl extends LinkedHashMap<String, ServerVariable> implements ServerVariables, ModelImpl {

    private static final long serialVersionUID = -7724841358483233927L;

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
    public ServerVariables addExtension(String name, Object value) {
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
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariables#addServerVariable(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.servers.ServerVariable)
     */
    @Override
    public ServerVariables addServerVariable(String name, ServerVariable serverVariable) {
        if (serverVariable == null) {
            return this;
        }
        this.put(name, serverVariable);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariables#removeServerVariable(java.lang.String)
     */
    @Override
    public void removeServerVariable(String name) {
        this.remove(name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariables#getServerVariables()
     */
    @Override
    public Map<String, ServerVariable> getServerVariables() {
        return Collections.unmodifiableMap(this);
    }

    @Override
    public void setServerVariables(Map<String, ServerVariable> items) {
        this.clear();
        this.putAll(items);
    }

}