package io.smallrye.openapi.mavenplugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASConfig;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;

/**
 * Implementation of the {@link OpenApiConfig} interface that gets config information from maven
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MavenConfig implements OpenApiConfig {

    private final Map<String, String> properties;

    public MavenConfig(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String modelReader() {
        return properties.getOrDefault(OASConfig.MODEL_READER, null);
    }

    @Override
    public String filter() {
        return properties.getOrDefault(OASConfig.FILTER, null);
    }

    @Override
    public boolean scanDisable() {
        return Boolean.parseBoolean(properties.getOrDefault(OASConfig.FILTER, "false"));
    }

    @Override
    public Set<String> scanPackages() {
        return asCsvSet(properties.getOrDefault(OASConfig.SCAN_PACKAGES, null));
    }

    @Override
    public Set<String> scanClasses() {
        return asCsvSet(properties.getOrDefault(OASConfig.SCAN_CLASSES, null));
    }

    @Override
    public Set<String> scanExcludePackages() {
        Set<String> result = asCsvSet(properties.getOrDefault(OASConfig.SCAN_EXCLUDE_PACKAGES, null));
        result.addAll(OpenApiConstants.NEVER_SCAN_PACKAGES);
        return result;
    }

    @Override
    public Set<String> scanExcludeClasses() {
        Set<String> result = asCsvSet(properties.getOrDefault(OASConfig.SCAN_EXCLUDE_CLASSES, null));
        result.addAll(OpenApiConstants.NEVER_SCAN_CLASSES);
        return result;
    }

    @Override
    public List<String> servers() {
        return asCsvList(properties.getOrDefault(OASConfig.SERVERS, null));
    }

    @Override
    public List<String> pathServers(String path) {
        return asCsvList(properties.getOrDefault(OASConfig.SERVERS_PATH_PREFIX + path, null));
    }

    @Override
    public List<String> operationServers(String operationId) {
        return asCsvList(properties.getOrDefault(OASConfig.SERVERS_OPERATION_PREFIX + operationId, null));
    }

    @Override
    public boolean scanDependenciesDisable() {
        return Boolean.parseBoolean(properties.getOrDefault(OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, "false"));
    }

    @Override
    public String customSchemaRegistryClass() {
        return properties.getOrDefault(OpenApiConstants.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS, null);
    }

    @Override
    public boolean applicationPathDisable() {
        return Boolean.parseBoolean(properties.getOrDefault(OpenApiConstants.SMALLRYE_APP_PATH_DISABLE, "false"));
    }

    @Override
    public String getOpenApiVersion() {
        return properties.getOrDefault(OpenApiConstants.VERSION, null);
    }

    @Override
    public String getInfoTitle() {
        return properties.getOrDefault(OpenApiConstants.INFO_TITLE, null);
    }

    @Override
    public String getInfoVersion() {
        return properties.getOrDefault(OpenApiConstants.INFO_VERSION, null);
    }

    @Override
    public String getInfoDescription() {
        return properties.getOrDefault(OpenApiConstants.INFO_DESCRIPTION, null);
    }

    @Override
    public String getInfoTermsOfService() {
        return properties.getOrDefault(OpenApiConstants.INFO_TERMS, null);
    }

    @Override
    public String getInfoContactEmail() {
        return properties.getOrDefault(OpenApiConstants.INFO_CONTACT_EMAIL, null);
    }

    @Override
    public String getInfoContactName() {
        return properties.getOrDefault(OpenApiConstants.INFO_CONTACT_NAME, null);
    }

    @Override
    public String getInfoContactUrl() {
        return properties.getOrDefault(OpenApiConstants.INFO_CONTACT_URL, null);
    }

    @Override
    public String getInfoLicenseName() {
        return properties.getOrDefault(OpenApiConstants.INFO_LICENSE_NAME, null);
    }

    @Override
    public String getInfoLicenseUrl() {
        return properties.getOrDefault(OpenApiConstants.INFO_LICENSE_URL, null);
    }

    @Override
    public OperationIdStrategy getOperationIdStrategy() {
        String strategy = properties.getOrDefault(OpenApiConstants.OPERATION_ID_STRAGEGY, null);
        if (strategy != null) {
            return OperationIdStrategy.valueOf(strategy);
        }
        return null;
    }

    @Override
    public DuplicateOperationIdBehavior getDuplicateOperationIdBehavior() {
        String behavior = properties.getOrDefault(OpenApiConstants.DUPLICATE_OPERATION_ID_BEHAVIOR,
                OpenApiConfig.DUPLICATE_OPERATION_ID_BEHAVIOR_DEFAULT.name());
        return DuplicateOperationIdBehavior.valueOf(behavior);
    }

    @Override
    public Set<String> getScanProfiles() {
        return asCsvSet(properties.getOrDefault(OpenApiConstants.SCAN_PROFILES, null));
    }

    @Override
    public Set<String> getScanExcludeProfiles() {
        return asCsvSet(properties.getOrDefault(OpenApiConstants.SCAN_EXCLUDE_PROFILES, null));
    }
}
