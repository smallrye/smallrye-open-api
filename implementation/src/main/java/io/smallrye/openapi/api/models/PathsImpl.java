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

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Paths} OpenAPI model interface.
 */
public class PathsImpl extends ExtensibleImpl<Paths> implements Paths, ModelImpl {

    private Map<String, PathItem> pathItems;

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#addPathItem(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.PathItem)
     */
    @Override
    public Paths addPathItem(String name, PathItem item) {
        this.pathItems = ModelUtil.add(name, item, this.pathItems, LinkedHashMap<String, PathItem>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#removePathItem(java.lang.String)
     */
    @Override
    public void removePathItem(String name) {
        ModelUtil.remove(this.pathItems, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#getPathItems()
     */
    @Override
    public Map<String, PathItem> getPathItems() {
        return ModelUtil.unmodifiableMap(this.pathItems);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Paths#setPathItems(java.util.Map)
     */
    @Override
    public void setPathItems(Map<String, PathItem> items) {
        this.pathItems = ModelUtil.replace(items, LinkedHashMap<String, PathItem>::new);
    }

}