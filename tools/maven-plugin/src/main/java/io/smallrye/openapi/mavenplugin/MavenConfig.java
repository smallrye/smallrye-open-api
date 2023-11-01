package io.smallrye.openapi.mavenplugin;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
    public Pattern scanPackages() {
        return patternOf(properties.getOrDefault(OASConfig.SCAN_PACKAGES, null));
    }

    @Override
    public Pattern scanClasses() {
        return patternOf(properties.getOrDefault(OASConfig.SCAN_CLASSES, null));
    }

    @Override
    public Pattern scanExcludePackages() {
        return patternOf(properties.getOrDefault(OASConfig.SCAN_EXCLUDE_PACKAGES, null), OpenApiConstants.NEVER_SCAN_PACKAGES);
    }

    @Override
    public Pattern scanExcludeClasses() {
        return patternOf(properties.getOrDefault(OASConfig.SCAN_EXCLUDE_CLASSES, null), OpenApiConstants.NEVER_SCAN_CLASSES);
    }

    @Override
    public Set<String> servers() {
        return asCsvSet(properties.getOrDefault(OASConfig.SERVERS, null));
    }

    @Override
    public Set<String> pathServers(String path) {
        return asCsvSet(properties.getOrDefault(OASConfig.SERVERS_PATH_PREFIX + path, null));
    }

    @Override
    public Set<String> operationServers(String operationId) {
        return asCsvSet(properties.getOrDefault(OASConfig.SERVERS_OPERATION_PREFIX + operationId, null));
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
    public Set<String> getScanProfiles() {
        return asCsvSet(properties.getOrDefault(OpenApiConstants.SCAN_PROFILES, null));
    }

    @Override
    public Set<String> getScanExcludeProfiles() {
        return asCsvSet(properties.getOrDefault(OpenApiConstants.SCAN_EXCLUDE_PROFILES, null));
    }

    @Override
    public Integer getMaximumStaticFileSize() {
        return Integer.parseInt(
                properties.getOrDefault(OpenApiConstants.MAXIMUM_STATIC_FILE_SIZE,
                        String.valueOf(OpenApiConfig.MAXIMUM_STATIC_FILE_SIZE_DEFAULT)));
    }
}
