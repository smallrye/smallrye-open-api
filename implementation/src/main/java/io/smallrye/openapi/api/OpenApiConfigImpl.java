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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.OASConfig;

/**
 * Implementation of the {@link OpenApiConfig} interface that gets config information from a
 * standard MP Config object.
 * 
 * @author eric.wittmann@gmail.com
 */
public class OpenApiConfigImpl implements OpenApiConfig {

    private Config config;

    private String modelReader;
    private String filter;
    private Boolean scanDisable;
    private Set<String> scanPackages;
    private Set<String> scanClasses;
    private Set<String> scanExcludePackages;
    private Set<String> scanExcludeClasses;
    private Set<String> servers;
    private Boolean scanDependenciesDisable;
    private Set<String> scanDependenciesJars;
    private Boolean schemaReferencesEnable;
    private String customSchemaRegistryClass;

    /**
     * Constructor.
     * 
     * @param config MicroProfile Config instance
     */
    public OpenApiConfigImpl(Config config) {
        this.config = config;
    }

    /**
     * @return the MP config instance
     */
    protected Config getConfig() {
        // We cannot use ConfigProvider.getConfig() as the archive is not deployed yet - TCCL cannot be set
        return config;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#modelReader()
     */
    @Override
    public String modelReader() {
        if (modelReader == null) {
            modelReader = getConfig().getOptionalValue(OASConfig.MODEL_READER, String.class).orElse(null);
        }
        return modelReader;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#filter()
     */
    @Override
    public String filter() {
        if (filter == null) {
            filter = getConfig().getOptionalValue(OASConfig.FILTER, String.class).orElse(null);
        }
        return filter;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanDisable()
     */
    @Override
    public boolean scanDisable() {
        if (scanDisable == null) {
            scanDisable = getConfig().getOptionalValue(OASConfig.SCAN_DISABLE, Boolean.class).orElse(false);
        }
        return scanDisable;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanPackages()
     */
    @Override
    public Set<String> scanPackages() {
        if (scanPackages == null) {
            String packages = getConfig().getOptionalValue(OASConfig.SCAN_PACKAGES, String.class).orElse(null);
            scanPackages = asCsvSet(packages);
        }
        return scanPackages;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanClasses()
     */
    @Override
    public Set<String> scanClasses() {
        if (scanClasses == null) {
            String classes = getConfig().getOptionalValue(OASConfig.SCAN_CLASSES, String.class).orElse(null);
            scanClasses = asCsvSet(classes);
        }
        return scanClasses;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanExcludePackages()
     */
    @Override
    public Set<String> scanExcludePackages() {
        if (scanExcludePackages == null) {
            String packages = getConfig().getOptionalValue(OASConfig.SCAN_EXCLUDE_PACKAGES, String.class).orElse(null);
            scanExcludePackages = asCsvSet(packages);
        }
        return scanExcludePackages;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanExcludeClasses()
     */
    @Override
    public Set<String> scanExcludeClasses() {
        if (scanExcludeClasses == null) {
            String classes = getConfig().getOptionalValue(OASConfig.SCAN_EXCLUDE_CLASSES, String.class).orElse(null);
            scanExcludeClasses = asCsvSet(classes);
        }
        return scanExcludeClasses;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#servers()
     */
    @Override
    public Set<String> servers() {
        if (servers == null) {
            String theServers = getConfig().getOptionalValue(OASConfig.SERVERS, String.class).orElse(null);
            servers = asCsvSet(theServers);
        }
        return servers;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#pathServers(java.lang.String)
     */
    @Override
    public Set<String> pathServers(String path) {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS_PATH_PREFIX + path, String.class).orElse(null);
        return asCsvSet(servers);
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#operationServers(java.lang.String)
     */
    @Override
    public Set<String> operationServers(String operationId) {
        String servers = getConfig().getOptionalValue(OASConfig.SERVERS_OPERATION_PREFIX + operationId, String.class)
                .orElse(null);
        return asCsvSet(servers);
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanDependenciesDisable()
     */
    @Override
    public boolean scanDependenciesDisable() {
        if (scanDependenciesDisable == null) {
            scanDependenciesDisable = getConfig().getOptionalValue(OpenApiConstants.SCAN_DEPENDENCIES_DISABLE, Boolean.class)
                    .orElse(false);
        }
        return scanDependenciesDisable;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanDependenciesJars()
     */
    @Override
    public Set<String> scanDependenciesJars() {
        if (scanDependenciesJars == null) {
            String classes = getConfig().getOptionalValue(OpenApiConstants.SCAN_DEPENDENCIES_JARS, String.class).orElse(null);
            scanDependenciesJars = asCsvSet(classes);
        }
        return scanDependenciesJars;
    }

    @Override
    public boolean schemaReferencesEnable() {
        if (schemaReferencesEnable == null) {
            schemaReferencesEnable = getConfig().getOptionalValue(OpenApiConstants.SCHEMA_REFERENCES_ENABLE, Boolean.class)
                    .orElse(false);
        }
        return schemaReferencesEnable;
    }

    @Override
    public String customSchemaRegistryClass() {
        if (customSchemaRegistryClass == null) {
            customSchemaRegistryClass = getConfig()
                    .getOptionalValue(OpenApiConstants.CUSTOM_SCHEMA_REGISTRY_CLASS, String.class).orElse(null);
        }
        return customSchemaRegistryClass;
    }

    private static Set<String> asCsvSet(String items) {
        Set<String> rval = new HashSet<>();
        if (items != null) {
            String[] split = items.split(",");
            for (String item : split) {
                rval.add(item.trim());
            }
        }
        return rval;
    }

}
