/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

package io.smallrye.openapi.api;

import java.util.Set;

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#31-list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 */
public interface OpenApiConfig {

    public String modelReader();

    public String filter();

    public boolean scanDisable();

    public Set<String> scanPackages();

    public Set<String> scanClasses();

    public Set<String> scanExcludePackages();

    public Set<String> scanExcludeClasses();

    public Set<String> servers();

    public Set<String> pathServers(String path);

    public Set<String> operationServers(String operationId);

    public boolean scanDependenciesDisable();

    public Set<String> scanDependenciesJars();

    public boolean schemaReferencesEnable();

    public String customSchemaRegistryClass();

}
