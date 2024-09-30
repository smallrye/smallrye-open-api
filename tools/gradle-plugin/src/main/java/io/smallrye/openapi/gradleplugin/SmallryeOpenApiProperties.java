package io.smallrye.openapi.gradleplugin;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import io.smallrye.openapi.api.OpenApiConfig;

public interface SmallryeOpenApiProperties {

    /**
     * Load any properties from a file. This file is loaded first, and gets overwritten by explicitly
     * set properties in the maven configuration. Example
     * `${basedir}/src/main/resources/application.properties`.
     */
    RegularFileProperty getConfigProperties();

    /**
     * Filename of the schema Defaults to openapi. So the files created will be openapi.yaml and
     * openapi.json.
     */
    Property<String> getSchemaFilename();

    /**
     * Disable scanning the project's dependencies for OpenAPI model classes too
     */
    Property<Boolean> getScanDependenciesDisable();

    /**
     * Configuration property to specify the fully qualified name of the OASModelReader
     * implementation.
     */
    Property<String> getModelReader();

    /**
     * Configuration property to specify the fully qualified name of the OASFilter implementation.
     */
    Property<String> getFilter();

    /**
     * Configuration property to disable annotation scanning.
     */
    Property<Boolean> getScanDisabled();

    /**
     * Configuration property to specify the list of packages to scan.
     */
    ListProperty<String> getScanPackages();

    /**
     * Configuration property to specify the list of classes to scan.
     */
    ListProperty<String> getScanClasses();

    /**
     * Configuration property to specify the list of packages to exclude from scans.
     */
    ListProperty<String> getScanExcludePackages();

    /**
     * Configuration property to specify the list of classes to exclude from scans.
     */
    ListProperty<String> getScanExcludeClasses();

    /**
     * Configuration property to specify the list of global servers that provide connectivity
     * information.
     */
    ListProperty<String> getServers();

    /**
     * Prefix of the configuration property to specify an alternative list of servers to service all
     * operations in a path
     */
    MapProperty<String, String> getPathServers();

    /**
     * Prefix of the configuration property to specify an alternative list of servers to service an
     * operation.
     */
    MapProperty<String, String> getOperationServers();

    /**
     * Fully qualified name of a CustomSchemaRegistry, which can be used to specify a custom schema
     * for a type.
     */
    Property<String> getCustomSchemaRegistryClass();

    /**
     * Disable scanning of the javax.ws.rs.Path (and jakarta.ws.rs.Path) for the application path.
     */
    Property<Boolean> getApplicationPathDisable();

    /**
     * To specify a custom OpenAPI version.
     */
    Property<String> getOpenApiVersion();

    Property<String> getInfoTitle();

    Property<String> getInfoVersion();

    Property<String> getInfoDescription();

    Property<String> getInfoTermsOfService();

    Property<String> getInfoContactEmail();

    Property<String> getInfoContactName();

    Property<String> getInfoContactUrl();

    Property<String> getInfoLicenseName();

    Property<String> getInfoLicenseUrl();

    /**
     * Configuration property to specify how the operationid is generated. Can be used to minimize
     * risk of collisions between operations.
     */
    Property<OpenApiConfig.OperationIdStrategy> getOperationIdStrategy();

    /**
     * Configuration property to specify what should happen if duplicate operationIds occur.
     */
    Property<OpenApiConfig.DuplicateOperationIdBehavior> getDuplicateOperationIdBehavior();

    /**
     * Profiles which explicitly include operations. Any operation without a matching profile is
     * excluded.
     */
    ListProperty<String> getScanProfiles();

    /**
     * Profiles which explicitly exclude operations. Any operation without a matching profile is
     * included.
     */
    ListProperty<String> getScanExcludeProfiles();

    MapProperty<String, String> getScanResourceClasses();

    /**
     * Filter the type of files that will be generated, allowed values are {@code ALL}, {@code YAML} and {@code JSON}.
     */
    Property<String> getOutputFileTypeFilter();

    /**
     * Output encoding for openapi document.
     */
    Property<String> getEncoding();

    /**
     * List of standard Java modules that should be made available to annotation scanning for introspection.
     */
    ListProperty<String> getIncludeStandardJavaModules();
}
