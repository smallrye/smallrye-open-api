package io.smallrye.openapi.gradleplugin;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfig.DuplicateOperationIdBehavior;
import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;
import io.smallrye.openapi.api.constants.OpenApiConstants;

/**
 * Bag for all properties exposed by {@link SmallryeOpenApiExtension} and {@link SmallryeOpenApiTask}, latter takes its defaults
 * from {@link SmallryeOpenApiExtension}.
 */
class Configs {

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
    final Property<String> infoTermsOfService;
    final Property<String> infoContactEmail;
    final Property<String> infoContactName;
    final Property<String> infoContactUrl;
    final Property<String> infoLicenseName;
    final Property<String> infoLicenseUrl;
    final Property<OperationIdStrategy> operationIdStrategy;
    final Property<DuplicateOperationIdBehavior> duplicateOperationIdBehavior;
    final SetProperty<String> scanProfiles;
    final SetProperty<String> scanExcludeProfiles;
    final Property<String> encoding;

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
        openApiVersion = objects.property(String.class).convention(OpenApiConstants.OPEN_API_VERSION);
        infoTitle = objects.property(String.class);
        infoVersion = objects.property(String.class);
        infoDescription = objects.property(String.class);
        infoTermsOfService = objects.property(String.class);
        infoContactEmail = objects.property(String.class);
        infoContactName = objects.property(String.class);
        infoContactUrl = objects.property(String.class);
        infoLicenseName = objects.property(String.class);
        infoLicenseUrl = objects.property(String.class);
        operationIdStrategy = objects.property(OperationIdStrategy.class);
        duplicateOperationIdBehavior = objects.property(DuplicateOperationIdBehavior.class);
        scanProfiles = objects.setProperty(String.class);
        scanExcludeProfiles = objects.setProperty(String.class);
        encoding = objects.property(String.class).convention(StandardCharsets.UTF_8.name());
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
        infoTermsOfService = objects.property(String.class).convention(ext.getInfoTermsOfService());
        infoContactEmail = objects.property(String.class).convention(ext.getInfoContactEmail());
        infoContactName = objects.property(String.class).convention(ext.getInfoContactName());
        infoContactUrl = objects.property(String.class).convention(ext.getInfoContactUrl());
        infoLicenseName = objects.property(String.class).convention(ext.getInfoLicenseName());
        infoLicenseUrl = objects.property(String.class).convention(ext.getInfoLicenseUrl());
        operationIdStrategy = objects.property(OperationIdStrategy.class).convention(ext.getOperationIdStrategy());
        duplicateOperationIdBehavior = objects.property(DuplicateOperationIdBehavior.class);
        scanProfiles = objects.setProperty(String.class).convention(ext.getScanProfiles());
        scanExcludeProfiles = objects.setProperty(String.class).convention(ext.scanExcludeProfiles);
        encoding = objects.property(String.class).convention(ext.encoding);
    }

    OpenApiConfig asOpenApiConfig() {
        return new OpenApiConfig() {
            @Override
            public String modelReader() {
                return modelReader.getOrNull();
            }

            @Override
            public String filter() {
                return filter.getOrNull();
            }

            @Override
            public boolean scanDisable() {
                return scanDisabled.get();
            }

            @Override
            public Set<String> scanPackages() {
                return new HashSet<>(scanPackages.getOrElse(emptyList()));
            }

            @Override
            public Set<String> scanClasses() {
                return new HashSet<>(scanClasses.getOrElse(emptyList()));
            }

            @Override
            public Set<String> scanExcludePackages() {
                return new HashSet<>(scanExcludePackages.getOrElse(emptyList()));
            }

            @Override
            public Set<String> scanExcludeClasses() {
                return new HashSet<>(scanExcludeClasses.getOrElse(emptyList()));
            }

            @Override
            public Set<String> servers() {
                return new HashSet<>(servers.getOrElse(emptyList()));
            }

            @Override
            public Set<String> pathServers(String path) {
                return asCsvSet(pathServers.getting(path).getOrNull());
            }

            @Override
            public Set<String> operationServers(String operationId) {
                return asCsvSet(operationServers.getting(operationId).getOrNull());
            }

            @Override
            public boolean scanDependenciesDisable() {
                return scanDependenciesDisable.get();
            }

            @Override
            public String customSchemaRegistryClass() {
                return customSchemaRegistryClass.getOrNull();
            }

            @Override
            public boolean applicationPathDisable() {
                return applicationPathDisable.get();
            }

            @Override
            public String getOpenApiVersion() {
                return openApiVersion.getOrNull();
            }

            @Override
            public String getInfoTitle() {
                return infoTitle.getOrNull();
            }

            @Override
            public String getInfoVersion() {
                return infoVersion.getOrNull();
            }

            @Override
            public String getInfoDescription() {
                return infoDescription.getOrNull();
            }

            @Override
            public String getInfoTermsOfService() {
                return infoTermsOfService.getOrNull();
            }

            @Override
            public String getInfoContactEmail() {
                return infoContactEmail.getOrNull();
            }

            @Override
            public String getInfoContactName() {
                return infoContactName.getOrNull();
            }

            @Override
            public String getInfoContactUrl() {
                return infoContactUrl.getOrNull();
            }

            @Override
            public String getInfoLicenseName() {
                return infoLicenseName.getOrNull();
            }

            @Override
            public String getInfoLicenseUrl() {
                return infoLicenseUrl.getOrNull();
            }

            @Override
            public OperationIdStrategy getOperationIdStrategy() {
                return operationIdStrategy.getOrNull();
            }

            @Override
            public DuplicateOperationIdBehavior getDuplicateOperationIdBehavior() {
                return duplicateOperationIdBehavior.getOrElse(OpenApiConfig.DUPLICATE_OPERATION_ID_BEHAVIOR_DEFAULT);
            }

            @Override
            public Set<String> getScanProfiles() {
                return scanProfiles.getOrElse(emptySet());
            }

            @Override
            public Set<String> getScanExcludeProfiles() {
                return scanExcludeProfiles.getOrElse(emptySet());
            }

            // following aren't implemented by GenerateSchemaMojo, just documenting those here...

            //      public boolean arrayReferencesEnable() {
            //      }
            //
            //      public boolean privatePropertiesEnable() {
            //      }
            //
            //      public String propertyNamingStrategy() {
            //      }
            //
            //      public boolean sortedPropertiesEnable() {
            //      }
            //
            //      public Map<String, String> getSchemas() {
            //      }
            //
            //      public java.util.Optional<String[]> getDefaultProduces() {
            //      }
            //
            //      public java.util.Optional<String[]> getDefaultConsumes() {
            //      }
            //
            //      public java.util.Optional<Boolean> allowNakedPathParameter() {
            //      }
            //
            //      public boolean removeUnusedSchemas() {
            //      }
            //
            //      public void doAllowNakedPathParameter() {
            //      }
            //
            //      public Set<String> scanDependenciesJars() {
            //      }
        };
    }
}
