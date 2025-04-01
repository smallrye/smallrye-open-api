package io.smallrye.openapi.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASConfig;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;

/**
 * Bag for all properties exposed by {@link SmallryeOpenApiExtension} and {@link SmallryeOpenApiTask}, latter takes its defaults
 * from {@link SmallryeOpenApiExtension}.
 */
class Configs implements SmallryeOpenApiProperties {

    final RegularFileProperty configProperties;
    final Property<String> schemaFilename;
    final Property<Boolean> scanDependenciesDisable;
    final Property<String> modelReader;
    final Property<String> filter;
    final Property<Boolean> scanDisabled;
    final ListProperty<String> scanPackages;
    final ListProperty<String> scanClasses;
    final ListProperty<String> scanExcludePackages;
    final ListProperty<String> scanExcludeClasses;
    final ListProperty<String> servers;
    final MapProperty<String, String> pathServers;
    final MapProperty<String, String> operationServers;
    final Property<String> customSchemaRegistryClass;
    final Property<Boolean> applicationPathDisable;
    final Property<String> openApiVersion;
    final Property<String> infoTitle;
    final Property<String> infoVersion;
    final Property<String> infoDescription;
    final Property<String> infoSummary;
    final Property<String> infoTermsOfService;
    final Property<String> infoContactEmail;
    final Property<String> infoContactName;
    final Property<String> infoContactUrl;
    final Property<String> infoLicenseName;
    final Property<String> infoLicenseUrl;
    final Property<OpenApiConfig.OperationIdStrategy> operationIdStrategy;
    final Property<OpenApiConfig.DuplicateOperationIdBehavior> duplicateOperationIdBehavior;
    final ListProperty<String> scanProfiles;
    final ListProperty<String> scanExcludeProfiles;
    final MapProperty<String, String> scanResourceClasses;
    final Property<String> outputFileTypeFilter;
    final Property<String> encoding;
    final ListProperty<String> includeStandardJavaModules;

    Configs(ObjectFactory objects) {
        configProperties = objects.fileProperty();
        schemaFilename = objects.property(String.class).convention("openapi");
        scanDependenciesDisable = objects.property(Boolean.class).convention(false);
        modelReader = objects.property(String.class);
        filter = objects.property(String.class);
        scanDisabled = objects.property(Boolean.class);
        scanPackages = objects.listProperty(String.class);
        scanClasses = objects.listProperty(String.class);
        scanExcludePackages = objects.listProperty(String.class);
        scanExcludeClasses = objects.listProperty(String.class);
        servers = objects.listProperty(String.class);
        pathServers = objects.mapProperty(String.class, String.class);
        operationServers = objects.mapProperty(String.class, String.class);
        customSchemaRegistryClass = objects.property(String.class);
        applicationPathDisable = objects.property(Boolean.class).convention(false);
        openApiVersion = objects.property(String.class).convention(SmallRyeOASConfig.Defaults.VERSION);
        infoTitle = objects.property(String.class);
        infoVersion = objects.property(String.class);
        infoDescription = objects.property(String.class);
        infoSummary = objects.property(String.class);
        infoTermsOfService = objects.property(String.class);
        infoContactEmail = objects.property(String.class);
        infoContactName = objects.property(String.class);
        infoContactUrl = objects.property(String.class);
        infoLicenseName = objects.property(String.class);
        infoLicenseUrl = objects.property(String.class);
        operationIdStrategy = objects.property(OpenApiConfig.OperationIdStrategy.class);
        duplicateOperationIdBehavior = objects.property(OpenApiConfig.DuplicateOperationIdBehavior.class);
        scanProfiles = objects.listProperty(String.class);
        scanExcludeProfiles = objects.listProperty(String.class);
        scanResourceClasses = objects.mapProperty(String.class, String.class);
        outputFileTypeFilter = objects.property(String.class).convention("ALL");
        encoding = objects.property(String.class).convention(StandardCharsets.UTF_8.name());
        includeStandardJavaModules = objects.listProperty(String.class);
    }

    Configs(ObjectFactory objects, SmallryeOpenApiExtension ext) {
        configProperties = objects.fileProperty().convention(ext.getConfigProperties());
        schemaFilename = objects.property(String.class).convention(ext.getSchemaFilename());
        scanDependenciesDisable = objects.property(Boolean.class).convention(ext.getScanDependenciesDisable());
        modelReader = objects.property(String.class).convention(ext.getModelReader());
        filter = objects.property(String.class).convention(ext.getFilter());
        scanDisabled = objects.property(Boolean.class).convention(ext.getScanDisabled());
        scanPackages = objects.listProperty(String.class).convention(ext.getScanPackages());
        scanClasses = objects.listProperty(String.class).convention(ext.getScanClasses());
        scanExcludePackages = objects.listProperty(String.class).convention(ext.getScanExcludePackages());
        scanExcludeClasses = objects.listProperty(String.class).convention(ext.getScanExcludeClasses());
        servers = objects.listProperty(String.class).convention(ext.getServers());
        pathServers = objects.mapProperty(String.class, String.class).convention(ext.getPathServers());
        operationServers = objects.mapProperty(String.class, String.class).convention(ext.getOperationServers());
        customSchemaRegistryClass = objects.property(String.class).convention(ext.getCustomSchemaRegistryClass());
        applicationPathDisable = objects.property(Boolean.class).convention(ext.getApplicationPathDisable());
        openApiVersion = objects.property(String.class).convention(ext.getOpenApiVersion());
        infoTitle = objects.property(String.class).convention(ext.getInfoTitle());
        infoVersion = objects.property(String.class).convention(ext.getInfoVersion());
        infoDescription = objects.property(String.class).convention(ext.getInfoDescription());
        infoSummary = objects.property(String.class).convention(ext.getInfoSummary());
        infoTermsOfService = objects.property(String.class).convention(ext.getInfoTermsOfService());
        infoContactEmail = objects.property(String.class).convention(ext.getInfoContactEmail());
        infoContactName = objects.property(String.class).convention(ext.getInfoContactName());
        infoContactUrl = objects.property(String.class).convention(ext.getInfoContactUrl());
        infoLicenseName = objects.property(String.class).convention(ext.getInfoLicenseName());
        infoLicenseUrl = objects.property(String.class).convention(ext.getInfoLicenseUrl());
        operationIdStrategy = objects.property(OpenApiConfig.OperationIdStrategy.class)
                .convention(ext.getOperationIdStrategy());
        duplicateOperationIdBehavior = objects.property(OpenApiConfig.DuplicateOperationIdBehavior.class)
                .convention(ext.getDuplicateOperationIdBehavior());
        scanProfiles = objects.listProperty(String.class).convention(ext.getScanProfiles());
        scanExcludeProfiles = objects.listProperty(String.class).convention(ext.getScanExcludeProfiles());
        scanResourceClasses = objects.mapProperty(String.class, String.class).convention(ext.getScanResourceClasses());
        outputFileTypeFilter = objects.property(String.class).convention(ext.getOutputFileTypeFilter());
        encoding = objects.property(String.class).convention(ext.getEncoding());
        includeStandardJavaModules = objects.listProperty(String.class).convention(ext.getIncludeStandardJavaModules());
    }

    Config asMicroprofileConfig() {
        return new SmallRyeConfigBuilder()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(getProperties(), "gradle-plugin", ConfigSource.DEFAULT_ORDINAL))
                .build();
    }

    private Map<String, String> getProperties() {
        // First check if the configProperties is set, if so, load that.
        Map<String, String> cp = new HashMap<>();
        File propertiesFile = configProperties.getAsFile().getOrElse(null);

        if (propertiesFile != null) {
            Properties p = new Properties();

            try (InputStream is = Files.newInputStream(propertiesFile.toPath())) {
                p.load(is);
                p.stringPropertyNames().forEach(name -> cp.put(name, p.getProperty(name)));
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }

        // Now add properties set in the plugin.
        addToPropertyMap(cp, OASConfig.MODEL_READER, modelReader);
        addToPropertyMap(cp, OASConfig.FILTER, filter);
        addToPropertyMap(cp, OASConfig.SCAN_DISABLE, scanDisabled);
        addToPropertyMap(cp, OASConfig.SCAN_PACKAGES, scanPackages);
        addToPropertyMap(cp, OASConfig.SCAN_CLASSES, scanClasses);
        addToPropertyMap(cp, OASConfig.SCAN_EXCLUDE_PACKAGES, scanExcludePackages);
        addToPropertyMap(cp, OASConfig.SCAN_EXCLUDE_CLASSES, scanExcludeClasses);
        addToPropertyMap(cp, OASConfig.SERVERS, servers);
        addToPropertyMap(cp, OASConfig.SERVERS_PATH_PREFIX, pathServers);
        addToPropertyMap(cp, OASConfig.SERVERS_OPERATION_PREFIX, operationServers);
        addToPropertyMap(cp, SmallRyeOASConfig.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, scanDependenciesDisable);
        addToPropertyMap(cp, SmallRyeOASConfig.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS, customSchemaRegistryClass);
        addToPropertyMap(cp, SmallRyeOASConfig.SMALLRYE_APP_PATH_DISABLE, applicationPathDisable);
        addToPropertyMap(cp, SmallRyeOASConfig.VERSION, openApiVersion);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_TITLE, infoTitle);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_VERSION, infoVersion);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_DESCRIPTION, infoDescription);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_SUMMARY, infoSummary);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_TERMS, infoTermsOfService);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_CONTACT_EMAIL, infoContactEmail);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_CONTACT_NAME, infoContactName);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_CONTACT_URL, infoContactUrl);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_LICENSE_NAME, infoLicenseName);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_LICENSE_URL, infoLicenseUrl);
        addToPropertyMap(cp, SmallRyeOASConfig.OPERATION_ID_STRAGEGY, operationIdStrategy);
        addToPropertyMap(cp, SmallRyeOASConfig.DUPLICATE_OPERATION_ID_BEHAVIOR, duplicateOperationIdBehavior);
        addToPropertyMap(cp, SmallRyeOASConfig.SCAN_PROFILES, scanProfiles);
        addToPropertyMap(cp, SmallRyeOASConfig.SCAN_EXCLUDE_PROFILES, scanExcludeProfiles);
        addToPropertyMap(cp, SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX, scanResourceClasses);

        return cp;
    }

    private void addToPropertyMap(Map<String, String> map, String key, Property<?> value) {
        if (value.isPresent()) {
            map.put(key, value.get().toString());
        }
    }

    private void addToPropertyMap(Map<String, String> map, String key, ListProperty<String> values) {
        if (values.isPresent() && !values.get().isEmpty()) {
            String value = values.get().stream()
                    .map(v -> v.replace("\\", "\\\\"))
                    .map(v -> v.replace(",", "\\,"))
                    .collect(Collectors.joining(","));
            map.put(key, value);
        }
    }

    private void addToPropertyMap(Map<String, String> map, String keyPrefix, MapProperty<String, String> values) {
        if (values.isPresent()) {
            values.get().forEach((key, value) -> map.put(keyPrefix + key, value));
        }
    }

    public RegularFileProperty getConfigProperties() {
        return configProperties;
    }

    public Property<String> getSchemaFilename() {
        return schemaFilename;
    }

    public Property<Boolean> getScanDependenciesDisable() {
        return scanDependenciesDisable;
    }

    public Property<String> getModelReader() {
        return modelReader;
    }

    public Property<String> getFilter() {
        return filter;
    }

    public Property<Boolean> getScanDisabled() {
        return scanDisabled;
    }

    public ListProperty<String> getScanPackages() {
        return scanPackages;
    }

    public ListProperty<String> getScanClasses() {
        return scanClasses;
    }

    public ListProperty<String> getScanExcludePackages() {
        return scanExcludePackages;
    }

    public ListProperty<String> getScanExcludeClasses() {
        return scanExcludeClasses;
    }

    public ListProperty<String> getServers() {
        return servers;
    }

    public MapProperty<String, String> getPathServers() {
        return pathServers;
    }

    public MapProperty<String, String> getOperationServers() {
        return operationServers;
    }

    public Property<String> getCustomSchemaRegistryClass() {
        return customSchemaRegistryClass;
    }

    public Property<Boolean> getApplicationPathDisable() {
        return applicationPathDisable;
    }

    public Property<String> getOpenApiVersion() {
        return openApiVersion;
    }

    public Property<String> getInfoTitle() {
        return infoTitle;
    }

    public Property<String> getInfoVersion() {
        return infoVersion;
    }

    public Property<String> getInfoDescription() {
        return infoDescription;
    }

    public Property<String> getInfoSummary() {
        return infoSummary;
    }

    public Property<String> getInfoTermsOfService() {
        return infoTermsOfService;
    }

    public Property<String> getInfoContactEmail() {
        return infoContactEmail;
    }

    public Property<String> getInfoContactName() {
        return infoContactName;
    }

    public Property<String> getInfoContactUrl() {
        return infoContactUrl;
    }

    public Property<String> getInfoLicenseName() {
        return infoLicenseName;
    }

    public Property<String> getInfoLicenseUrl() {
        return infoLicenseUrl;
    }

    public Property<OpenApiConfig.OperationIdStrategy> getOperationIdStrategy() {
        return operationIdStrategy;
    }

    public Property<OpenApiConfig.DuplicateOperationIdBehavior> getDuplicateOperationIdBehavior() {
        return duplicateOperationIdBehavior;
    }

    public ListProperty<String> getScanProfiles() {
        return scanProfiles;
    }

    public ListProperty<String> getScanExcludeProfiles() {
        return scanExcludeProfiles;
    }

    public MapProperty<String, String> getScanResourceClasses() {
        return scanResourceClasses;
    }

    public Property<String> getOutputFileTypeFilter() {
        return outputFileTypeFilter;
    }

    public Property<String> getEncoding() {
        return encoding;
    }

    public ListProperty<String> getIncludeStandardJavaModules() {
        return includeStandardJavaModules;
    }
}
