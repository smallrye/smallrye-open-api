package io.smallrye.openapi.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.OASConfig;

import io.smallrye.openapi.api.constants.JsonbConstants;
import io.smallrye.openapi.api.constants.OpenApiConstants;

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.asciidoc#list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 */
public interface OpenApiConfig {

    enum OperationIdStrategy {
        METHOD,
        CLASS_METHOD,
        PACKAGE_CLASS_METHOD
    }

    enum DuplicateOperationIdBehavior {
        FAIL,
        WARN
    }

    enum AutoInheritance {
        NONE,
        BOTH,
        PARENT_ONLY
    }

    public static OpenApiConfig fromConfig(Config config) {
        return new OpenApiConfigImpl(config);
    }

    DuplicateOperationIdBehavior DUPLICATE_OPERATION_ID_BEHAVIOR_DEFAULT = DuplicateOperationIdBehavior.WARN;
    Integer MAXIMUM_STATIC_FILE_SIZE_DEFAULT = 3 * 1024 * 1024;

    <R, T> T getConfigValue(String propertyName, Class<R> type, Function<R, T> converter, Supplier<T> defaultValue);

    <R, T> Map<String, T> getConfigValueMap(String propertyNamePrefix, Class<R> type, Function<R, T> converter);

    default <T> T getConfigValue(String propertyName, Class<T> type, Supplier<T> defaultValue) {
        return getConfigValue(propertyName, type, Function.identity(), defaultValue);
    }

    default String modelReader() {
        return getConfigValue(OASConfig.MODEL_READER, String.class, () -> null);
    }

    default String filter() {
        return getConfigValue(OASConfig.FILTER, String.class, () -> null);
    }

    default boolean scanDisable() {
        return getConfigValue(OASConfig.SCAN_DISABLE, Boolean.class, () -> Boolean.FALSE);
    }

    default Set<String> scanPackages() {
        return getConfigValue(OASConfig.SCAN_PACKAGES, String[].class, this::toSet, Collections::emptySet);
    }

    default Set<String> scanClasses() {
        return getConfigValue(OASConfig.SCAN_CLASSES, String[].class, this::toSet, Collections::emptySet);
    }

    default Set<String> scanExcludePackages() {
        return getConfigValue(OASConfig.SCAN_EXCLUDE_PACKAGES, String[].class, values -> {
            Set<String> valueSet = toSet(values);
            valueSet.addAll(OpenApiConstants.NEVER_SCAN_PACKAGES);
            return Collections.unmodifiableSet(valueSet);
        }, () -> OpenApiConstants.NEVER_SCAN_PACKAGES);
    }

    default Set<String> scanExcludeClasses() {
        return getConfigValue(OASConfig.SCAN_EXCLUDE_CLASSES, String[].class, values -> {
            Set<String> valueSet = toSet(values);
            valueSet.addAll(OpenApiConstants.NEVER_SCAN_CLASSES);
            return Collections.unmodifiableSet(valueSet);
        }, () -> OpenApiConstants.NEVER_SCAN_CLASSES);
    }

    default boolean scanBeanValidation() {
        return getConfigValue(OASConfig.SCAN_BEANVALIDATION, Boolean.class, () -> Boolean.TRUE);
    }

    default List<String> servers() {
        return getConfigValue(OASConfig.SERVERS, String[].class, this::toList, Collections::emptyList);
    }

    default List<String> pathServers(String path) {
        return getConfigValue(OASConfig.SERVERS_PATH_PREFIX + path, String[].class, this::toList, Collections::emptyList);
    }

    default List<String> operationServers(String operationId) {
        return getConfigValue(OASConfig.SERVERS_OPERATION_PREFIX + operationId, String[].class, this::toList,
                Collections::emptyList);
    }

    default boolean scanDependenciesDisable() {
        return getConfigValue(OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, Boolean.class,
                () -> getConfigValue(OpenApiConstants.SCAN_DEPENDENCIES_DISABLE, Boolean.class,
                        () -> Boolean.FALSE));
    }

    default Set<String> scanDependenciesJars() {
        return getConfigValue(OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_JARS, String[].class, this::toSet,
                () -> getConfigValue(OpenApiConstants.SCAN_DEPENDENCIES_JARS, String[].class, this::toSet,
                        Collections::emptySet));
    }

    default boolean arrayReferencesEnable() {
        return getConfigValue(OpenApiConstants.SMALLRYE_ARRAY_REFERENCES_ENABLE, Boolean.class, () -> Boolean.TRUE);
    }

    default String customSchemaRegistryClass() {
        return getConfigValue(OpenApiConstants.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS, String.class,
                () -> getConfigValue(OpenApiConstants.CUSTOM_SCHEMA_REGISTRY_CLASS, String.class,
                        () -> null));
    }

    default boolean applicationPathDisable() {
        return getConfigValue(OpenApiConstants.SMALLRYE_APP_PATH_DISABLE, Boolean.class,
                () -> getConfigValue(OpenApiConstants.APP_PATH_DISABLE, Boolean.class,
                        () -> Boolean.FALSE));
    }

    default boolean privatePropertiesEnable() {
        return getConfigValue(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, Boolean.class, () -> Boolean.TRUE);
    }

    default String propertyNamingStrategy() {
        return getConfigValue(OpenApiConstants.SMALLRYE_PROPERTY_NAMING_STRATEGY, String.class, () -> JsonbConstants.IDENTITY);
    }

    default boolean sortedPropertiesEnable() {
        return getConfigValue(OpenApiConstants.SMALLRYE_SORTED_PROPERTIES_ENABLE, Boolean.class, () -> Boolean.FALSE);
    }

    default Map<String, String> getSchemas() {
        return getConfigValueMap(OASConfig.SCHEMA_PREFIX, String.class, Function.identity());
    }

    // Here we extend this in SmallRye with some more configure options (mp.openapi.extensions)
    default String getOpenApiVersion() {
        return getConfigValue(OpenApiConstants.VERSION, String.class, () -> null);
    }

    default String getInfoTitle() {
        return getConfigValue(OpenApiConstants.INFO_TITLE, String.class, () -> null);
    }

    default String getInfoVersion() {
        return getConfigValue(OpenApiConstants.INFO_VERSION, String.class, () -> null);
    }

    default String getInfoDescription() {
        return getConfigValue(OpenApiConstants.INFO_DESCRIPTION, String.class, () -> null);
    }

    default String getInfoTermsOfService() {
        return getConfigValue(OpenApiConstants.INFO_TERMS, String.class, () -> null);
    }

    default String getInfoContactEmail() {
        return getConfigValue(OpenApiConstants.INFO_CONTACT_EMAIL, String.class, () -> null);
    }

    default String getInfoContactName() {
        return getConfigValue(OpenApiConstants.INFO_CONTACT_NAME, String.class, () -> null);
    }

    default String getInfoContactUrl() {
        return getConfigValue(OpenApiConstants.INFO_CONTACT_URL, String.class, () -> null);
    }

    default String getInfoLicenseName() {
        return getConfigValue(OpenApiConstants.INFO_LICENSE_NAME, String.class, () -> null);
    }

    default String getInfoLicenseUrl() {
        return getConfigValue(OpenApiConstants.INFO_LICENSE_URL, String.class, () -> null);
    }

    default OperationIdStrategy getOperationIdStrategy() {
        return getConfigValue(OpenApiConstants.OPERATION_ID_STRAGEGY, String.class, OperationIdStrategy::valueOf, () -> null);
    }

    default DuplicateOperationIdBehavior getDuplicateOperationIdBehavior() {
        return getConfigValue(OpenApiConstants.DUPLICATE_OPERATION_ID_BEHAVIOR,
                String.class,
                DuplicateOperationIdBehavior::valueOf,
                () -> DUPLICATE_OPERATION_ID_BEHAVIOR_DEFAULT);
    }

    default Optional<String[]> getDefaultProduces() {
        return getConfigValue(OpenApiConstants.DEFAULT_PRODUCES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultConsumes() {
        return getConfigValue(OpenApiConstants.DEFAULT_CONSUMES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultPrimitivesProduces() {
        return getConfigValue(OpenApiConstants.DEFAULT_PRODUCES_PRIMITIVES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultPrimitivesConsumes() {
        return getConfigValue(OpenApiConstants.DEFAULT_CONSUMES_PRIMITIVES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<Boolean> allowNakedPathParameter() {
        return Optional.empty();
    }

    void setAllowNakedPathParameter(Boolean allowNakedPathParameter);

    default void doAllowNakedPathParameter() {
        setAllowNakedPathParameter(Boolean.TRUE);
    }

    default Set<String> getScanProfiles() {
        return getConfigValue(OpenApiConstants.SCAN_PROFILES, String[].class, this::toSet, Collections::emptySet);
    }

    default Set<String> getScanExcludeProfiles() {
        return getConfigValue(OpenApiConstants.SCAN_EXCLUDE_PROFILES, String[].class, this::toSet, Collections::emptySet);
    }

    default Map<String, String> getScanResourceClasses() {
        return getConfigValueMap(OpenApiConstants.SCAN_RESOURCE_CLASS_PREFIX, String.class, Function.identity());
    }

    default boolean removeUnusedSchemas() {
        return getConfigValue(OpenApiConstants.SMALLRYE_REMOVE_UNUSED_SCHEMAS, Boolean.class, () -> Boolean.FALSE);
    }

    default Integer getMaximumStaticFileSize() {
        return getConfigValue(OpenApiConstants.MAXIMUM_STATIC_FILE_SIZE, Integer.class, () -> MAXIMUM_STATIC_FILE_SIZE_DEFAULT);
    }

    default AutoInheritance getAutoInheritance() {
        return getConfigValue(OpenApiConstants.AUTO_INHERITANCE, String.class, AutoInheritance::valueOf,
                () -> AutoInheritance.NONE);
    }

    default Set<String> toSet(String[] items) {
        return Arrays.stream(items)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default List<String> toList(String[] items) {
        return Arrays.stream(items)
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
