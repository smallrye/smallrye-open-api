package io.smallrye.openapi.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.asciidoc#list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 */
public interface OpenApiConfig {

    /**
     * Set of classes which should never be scanned, regardless of user configuration.
     */
    public static final Set<String> NEVER_SCAN_CLASSES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList()));

    /**
     * Set of packages which should never be scanned, regardless of user configuration.
     */
    public static final Set<String> NEVER_SCAN_PACKAGES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("java.lang")));

    /**
     * Default set of packages with annotations that should not be checked for
     * nested/composed annotations. For example, the annotations
     * <code>org.jetbrains.annotations.NotNull</code> will not be checked for
     * additional annotations. Generally, only application-provided custom
     * annotations would be used to compose multiple OpenAPI annotations. This
     * list allows the scanner to short-circuit the scanning of an excessive
     * number of annotations.
     */
    public static final Set<String> DEFAULT_COMPOSITION_EXCLUDE_PACKAGES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(
                    "java",
                    "jakarta",
                    "kotlin",
                    "com.fasterxml.jackson",
                    "io.quarkus",
                    "org.eclipse.microprofile.openapi",
                    "org.jetbrains.annotations")));

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
            valueSet.addAll(NEVER_SCAN_PACKAGES);
            return Collections.unmodifiableSet(valueSet);
        }, () -> NEVER_SCAN_PACKAGES);
    }

    default Set<String> scanExcludeClasses() {
        return getConfigValue(OASConfig.SCAN_EXCLUDE_CLASSES, String[].class, values -> {
            Set<String> valueSet = toSet(values);
            valueSet.addAll(NEVER_SCAN_CLASSES);
            return Collections.unmodifiableSet(valueSet);
        }, () -> NEVER_SCAN_CLASSES);
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
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, Boolean.class,
                () -> getConfigValue(SmallRyeOASConfig.SCAN_DEPENDENCIES_DISABLE, Boolean.class,
                        () -> Boolean.FALSE));
    }

    default Set<String> scanDependenciesJars() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_SCAN_DEPENDENCIES_JARS, String[].class, this::toSet,
                () -> getConfigValue(SmallRyeOASConfig.SCAN_DEPENDENCIES_JARS, String[].class, this::toSet,
                        Collections::emptySet));
    }

    default boolean arrayReferencesEnable() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_ARRAY_REFERENCES_ENABLE, Boolean.class, () -> Boolean.TRUE);
    }

    default String customSchemaRegistryClass() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS, String.class,
                () -> getConfigValue(SmallRyeOASConfig.CUSTOM_SCHEMA_REGISTRY_CLASS, String.class,
                        () -> null));
    }

    default boolean applicationPathDisable() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_APP_PATH_DISABLE, Boolean.class,
                () -> getConfigValue(SmallRyeOASConfig.APP_PATH_DISABLE, Boolean.class,
                        () -> Boolean.FALSE));
    }

    default boolean privatePropertiesEnable() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, Boolean.class, () -> Boolean.TRUE);
    }

    default String propertyNamingStrategy() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY, String.class, () -> JsonbConstants.IDENTITY);
    }

    default boolean sortedPropertiesEnable() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_SORTED_PROPERTIES_ENABLE, Boolean.class, () -> Boolean.FALSE);
    }

    default Map<String, String> getSchemas() {
        return getConfigValueMap(OASConfig.SCHEMA_PREFIX, String.class, Function.identity());
    }

    // Here we extend this in SmallRye with some more configure options (mp.openapi.extensions)
    default String getOpenApiVersion() {
        return getConfigValue(SmallRyeOASConfig.VERSION, String.class, () -> null);
    }

    default String getInfoTitle() {
        return getConfigValue(SmallRyeOASConfig.INFO_TITLE, String.class, () -> null);
    }

    default String getInfoVersion() {
        return getConfigValue(SmallRyeOASConfig.INFO_VERSION, String.class, () -> null);
    }

    default String getInfoDescription() {
        return getConfigValue(SmallRyeOASConfig.INFO_DESCRIPTION, String.class, () -> null);
    }

    default String getInfoTermsOfService() {
        return getConfigValue(SmallRyeOASConfig.INFO_TERMS, String.class, () -> null);
    }

    default String getInfoContactEmail() {
        return getConfigValue(SmallRyeOASConfig.INFO_CONTACT_EMAIL, String.class, () -> null);
    }

    default String getInfoContactName() {
        return getConfigValue(SmallRyeOASConfig.INFO_CONTACT_NAME, String.class, () -> null);
    }

    default String getInfoContactUrl() {
        return getConfigValue(SmallRyeOASConfig.INFO_CONTACT_URL, String.class, () -> null);
    }

    default String getInfoLicenseName() {
        return getConfigValue(SmallRyeOASConfig.INFO_LICENSE_NAME, String.class, () -> null);
    }

    default String getInfoLicenseUrl() {
        return getConfigValue(SmallRyeOASConfig.INFO_LICENSE_URL, String.class, () -> null);
    }

    default OperationIdStrategy getOperationIdStrategy() {
        return getConfigValue(SmallRyeOASConfig.OPERATION_ID_STRAGEGY, String.class, OperationIdStrategy::valueOf, () -> null);
    }

    default DuplicateOperationIdBehavior getDuplicateOperationIdBehavior() {
        return getConfigValue(SmallRyeOASConfig.DUPLICATE_OPERATION_ID_BEHAVIOR,
                String.class,
                DuplicateOperationIdBehavior::valueOf,
                () -> DUPLICATE_OPERATION_ID_BEHAVIOR_DEFAULT);
    }

    default Optional<String[]> getDefaultProduces() {
        return getConfigValue(SmallRyeOASConfig.DEFAULT_PRODUCES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultConsumes() {
        return getConfigValue(SmallRyeOASConfig.DEFAULT_CONSUMES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultPrimitivesProduces() {
        return getConfigValue(SmallRyeOASConfig.DEFAULT_PRODUCES_PRIMITIVES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultPrimitivesConsumes() {
        return getConfigValue(SmallRyeOASConfig.DEFAULT_CONSUMES_PRIMITIVES, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultStreamingProduces() {
        return getConfigValue(SmallRyeOASConfig.DEFAULT_PRODUCES_STREAMING, String[].class, Optional::of, Optional::empty);
    }

    default Optional<String[]> getDefaultStreamingConsumes() {
        return getConfigValue(SmallRyeOASConfig.DEFAULT_CONSUMES_STREAMING, String[].class, Optional::of, Optional::empty);
    }

    default Optional<Boolean> allowNakedPathParameter() {
        return Optional.empty();
    }

    void setAllowNakedPathParameter(Boolean allowNakedPathParameter);

    default void doAllowNakedPathParameter() {
        setAllowNakedPathParameter(Boolean.TRUE);
    }

    default Set<String> getScanProfiles() {
        return getConfigValue(SmallRyeOASConfig.SCAN_PROFILES, String[].class, this::toSet, Collections::emptySet);
    }

    default Set<String> getScanExcludeProfiles() {
        return getConfigValue(SmallRyeOASConfig.SCAN_EXCLUDE_PROFILES, String[].class, this::toSet, Collections::emptySet);
    }

    default Map<String, String> getScanResourceClasses() {
        return getConfigValueMap(SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX, String.class, Function.identity());
    }

    default boolean removeUnusedSchemas() {
        return getConfigValue(SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_SCHEMAS, Boolean.class, () -> Boolean.FALSE);
    }

    default Integer getMaximumStaticFileSize() {
        return getConfigValue(SmallRyeOASConfig.MAXIMUM_STATIC_FILE_SIZE, Integer.class,
                () -> MAXIMUM_STATIC_FILE_SIZE_DEFAULT);
    }

    default AutoInheritance getAutoInheritance() {
        return getConfigValue(SmallRyeOASConfig.AUTO_INHERITANCE, String.class, AutoInheritance::valueOf,
                () -> AutoInheritance.NONE);
    }

    default Set<String> getScanCompositionExcludePackages() {
        return getConfigValue(SmallRyeOASConfig.SCAN_COMPOSITION_EXCLUDE_PACKAGES, String[].class, this::toSet,
                () -> DEFAULT_COMPOSITION_EXCLUDE_PACKAGES);
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
