package io.smallrye.openapi.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.smallrye.openapi.api.constants.JsonbConstants;
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

    Integer MAXIMUM_STATIC_FILE_SIZE_DEFAULT = 3 * 1024 * 1024;

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

    default boolean arrayReferencesEnable() {
        return true;
    }

    default String customSchemaRegistryClass() {
        return null;
    }

    default boolean applicationPathDisable() {
        return false;
    }

    default boolean privatePropertiesEnable() {
        return true;
    }

    default String propertyNamingStrategy() {
        return JsonbConstants.IDENTITY;
    }

    default boolean sortedPropertiesEnable() {
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

    default Optional<String[]> getDefaultProduces() {
        return Optional.empty();
    }

    default Optional<String[]> getDefaultConsumes() {
        return Optional.empty();
    }

    default Optional<Boolean> allowNakedPathParameter() {
        return Optional.empty();
    }

    default Set<String> getScanProfiles() {
        return new HashSet<>();
    }

    default Set<String> getScanExcludeProfiles() {
        return new HashSet<>();
    }

    default void doAllowNakedPathParameter() {
    }

    default Integer getMaximumStaticFileSize() {
        return MAXIMUM_STATIC_FILE_SIZE_DEFAULT;
    }

    enum OperationIdStrategy {
        METHOD,
        CLASS_METHOD,
        PACKAGE_CLASS_METHOD
    }

    default Pattern patternOf(String configValue) {
        return patternOf(configValue, null);
    }

    default Pattern patternOf(String configValue, Set<String> buildIn) {
        Pattern pattern = null;

        if (configValue != null && (configValue.startsWith("^") || configValue.endsWith("$"))) {
            pattern = Pattern.compile(configValue);
        } else {
            Set<String> literals = asCsvSet(configValue);
            if (buildIn != null && !buildIn.isEmpty()) {
                literals.addAll(buildIn);
            }
            if (literals.isEmpty()) {
                return Pattern.compile("", Pattern.LITERAL);
            } else {
                pattern = Pattern.compile("(" + literals.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")");
            }
        }

        return pattern;
    }

    default Set<String> asCsvSet(String items) {
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
