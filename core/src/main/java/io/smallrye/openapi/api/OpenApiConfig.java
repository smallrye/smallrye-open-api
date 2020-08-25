package io.smallrye.openapi.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.smallrye.openapi.api.constants.OpenApiConstants;

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#31-list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 */
public interface OpenApiConfig {

    default String modelReader() {
        return null;
    }

    default String filter() {
        return null;
    }

    default boolean scanDisable() {
        return false;
    }

    default Pattern scanPackages() {
        return null;
    }

    default Pattern scanClasses() {
        return null;
    }

    default Pattern scanExcludePackages() {
        return Pattern.compile(
                "(" + OpenApiConstants.NEVER_SCAN_PACKAGES.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")");
    }

    default Pattern scanExcludeClasses() {
        return Pattern.compile(
                "(" + OpenApiConstants.NEVER_SCAN_CLASSES.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")");
    }

    default Set<String> servers() {
        return new HashSet<>();
    }

    default Set<String> pathServers(String path) {
        return new HashSet<>();
    }

    default Set<String> operationServers(String operationId) {
        return new HashSet<>();
    }

    default boolean scanDependenciesDisable() {
        return false;
    }

    default Set<String> scanDependenciesJars() {
        return new HashSet<>();
    }

    default boolean schemaReferencesEnable() {
        return true;
    }

    default String customSchemaRegistryClass() {
        return null;
    }

    default boolean applicationPathDisable() {
        return false;
    }

    default Map<String, String> getSchemas() {
        return new HashMap<>();
    }

    // Here we extend this in SmallRye with some more configure options (mp.openapi.extensions)
    default String getOpenApiVersion() {
        return null;
    }

    default String getInfoTitle() {
        return null;
    }

    default String getInfoVersion() {
        return null;
    }

    default String getInfoDescription() {
        return null;
    }

    default String getInfoTermsOfService() {
        return null;
    }

    default String getInfoContactEmail() {
        return null;
    }

    default String getInfoContactName() {
        return null;
    }

    default String getInfoContactUrl() {
        return null;
    }

    default String getInfoLicenseName() {
        return null;
    }

    default String getInfoLicenseUrl() {
        return null;
    }

    default OperationIdStrategy getOperationIdStrategy() {
        return null;
    }

    enum OperationIdStrategy {
        METHOD,
        CLASS_METHOD,
        PACKAGE_CLASS_METHOD
    }

}
