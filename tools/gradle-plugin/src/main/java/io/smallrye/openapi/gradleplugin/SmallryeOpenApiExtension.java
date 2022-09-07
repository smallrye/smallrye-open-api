package io.smallrye.openapi.gradleplugin;

import javax.inject.Inject;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;

/**
 * Gradle extension objects, which allows Gradle project wide defaults, or just easier configuration
 * in Gradle build scripts.
 *
 * <p>
 * See {@link SmallryeOpenApiProperties} for information about the individual options.
 */
public class SmallryeOpenApiExtension extends Configs implements SmallryeOpenApiProperties {

    @Inject
    public SmallryeOpenApiExtension(ObjectFactory objects) {
        super(objects);
    }

    @Override
    public RegularFileProperty getConfigProperties() {
        return configProperties;
    }

    @Override
    public Property<String> getSchemaFilename() {
        return schemaFilename;
    }

    @Override
    public Property<Boolean> getScanDependenciesDisable() {
        return scanDependenciesDisable;
    }

    @Override
    public Property<String> getModelReader() {
        return modelReader;
    }

    @Override
    public Property<String> getFilter() {
        return filter;
    }

    @Override
    public Property<Boolean> getScanDisabled() {
        return scanDisabled;
    }

    @Override
    public ListProperty<String> getScanPackages() {
        return scanPackages;
    }

    @Override
    public ListProperty<String> getScanClasses() {
        return scanClasses;
    }

    @Override
    public ListProperty<String> getScanExcludePackages() {
        return scanExcludePackages;
    }

    @Override
    public ListProperty<String> getScanExcludeClasses() {
        return scanExcludeClasses;
    }

    @Override
    public ListProperty<String> getServers() {
        return servers;
    }

    @Override
    public MapProperty<String, String> getPathServers() {
        return pathServers;
    }

    @Override
    public MapProperty<String, String> getOperationServers() {
        return operationServers;
    }

    @Override
    public Property<String> getCustomSchemaRegistryClass() {
        return customSchemaRegistryClass;
    }

    @Override
    public Property<Boolean> getApplicationPathDisable() {
        return applicationPathDisable;
    }

    @Override
    public Property<String> getOpenApiVersion() {
        return openApiVersion;
    }

    @Override
    public Property<String> getInfoTitle() {
        return infoTitle;
    }

    @Override
    public Property<String> getInfoVersion() {
        return infoVersion;
    }

    @Override
    public Property<String> getInfoDescription() {
        return infoDescription;
    }

    @Override
    public Property<String> getInfoTermsOfService() {
        return infoTermsOfService;
    }

    @Override
    public Property<String> getInfoContactEmail() {
        return infoContactEmail;
    }

    @Override
    public Property<String> getInfoContactName() {
        return infoContactName;
    }

    @Override
    public Property<String> getInfoContactUrl() {
        return infoContactUrl;
    }

    @Override
    public Property<String> getInfoLicenseName() {
        return infoLicenseName;
    }

    @Override
    public Property<String> getInfoLicenseUrl() {
        return infoLicenseUrl;
    }

    @Override
    public Property<OperationIdStrategy> getOperationIdStrategy() {
        return operationIdStrategy;
    }

    @Override
    public SetProperty<String> getScanProfiles() {
        return scanProfiles;
    }

    @Override
    public SetProperty<String> getScanExcludeProfiles() {
        return scanExcludeProfiles;
    }

    @Override
    public Property<String> getEncoding() {
        return encoding;
    }
}
