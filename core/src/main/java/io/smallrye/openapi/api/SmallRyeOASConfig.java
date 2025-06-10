package io.smallrye.openapi.api;

import org.eclipse.microprofile.openapi.OASConfig;

/**
 * Configurable properties in SmallRye OpenAPI
 *
 * @see org.eclipse.microprofile.openapi.OASConfig
 */
public final class SmallRyeOASConfig {

    private SmallRyeOASConfig() {
    }

    private static final String VENDOR_NAME = "smallrye.";
    private static final String SUFFIX_SCAN_DEPENDENCIES_DISABLE = "scan-dependencies.disable";
    private static final String SUFFIX_SCAN_DEPENDENCIES_JARS = "scan-dependencies.jars";
    private static final String SUFFIX_SCHEMA_REFERENCES_ENABLE = "schema-references.enable";
    private static final String SUFFIX_ARRAY_REFERENCES_ENABLE = "array-references.enable";
    private static final String SUFFIX_CUSTOM_SCHEMA_REGISTRY_CLASS = "custom-schema-registry.class";
    private static final String SUFFIX_APP_PATH_DISABLE = "application-path.disable";
    private static final String SUFFIX_PRIVATE_PROPERTIES_ENABLE = "private-properties.enable";
    private static final String SUFFIX_PROPERTY_NAMING_STRATEGY = "property-naming-strategy";
    private static final String SUFFIX_SORTED_PROPERTIES_ENABLE = "sorted-properties.enable";
    private static final String SUFFIX_REMOVE_UNUSED_SCHEMAS_ENABLE = "remove-unused-schemas.enable";
    private static final String SUFFIX_MERGE_SCHEMA_EXAMPLES = "merge-schema-examples";
    private static final String SUFFIX_SORTED_PARAMETERS_ENABLE = "sorted-parameters.enable";
    private static final String SMALLRYE_PREFIX = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME;

    public static final String SMALLRYE_SCAN_DEPENDENCIES_DISABLE = SMALLRYE_PREFIX + SUFFIX_SCAN_DEPENDENCIES_DISABLE;
    public static final String SCAN_DEPENDENCIES_DISABLE = OASConfig.EXTENSIONS_PREFIX + SUFFIX_SCAN_DEPENDENCIES_DISABLE;

    public static final String SMALLRYE_SCAN_DEPENDENCIES_JARS = SMALLRYE_PREFIX + SUFFIX_SCAN_DEPENDENCIES_JARS;
    public static final String SCAN_DEPENDENCIES_JARS = OASConfig.EXTENSIONS_PREFIX + SUFFIX_SCAN_DEPENDENCIES_JARS;

    public static final String SCHEMA_REFERENCES_ENABLE = OASConfig.EXTENSIONS_PREFIX + SUFFIX_SCHEMA_REFERENCES_ENABLE;

    public static final String SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS = SMALLRYE_PREFIX + SUFFIX_CUSTOM_SCHEMA_REGISTRY_CLASS;
    public static final String CUSTOM_SCHEMA_REGISTRY_CLASS = OASConfig.EXTENSIONS_PREFIX + SUFFIX_CUSTOM_SCHEMA_REGISTRY_CLASS;

    public static final String SMALLRYE_APP_PATH_DISABLE = SMALLRYE_PREFIX + SUFFIX_APP_PATH_DISABLE;
    public static final String APP_PATH_DISABLE = OASConfig.EXTENSIONS_PREFIX + SUFFIX_APP_PATH_DISABLE;

    public static final String SMALLRYE_ARRAY_REFERENCES_ENABLE = SMALLRYE_PREFIX + SUFFIX_ARRAY_REFERENCES_ENABLE;

    public static final String SMALLRYE_PRIVATE_PROPERTIES_ENABLE = SMALLRYE_PREFIX + SUFFIX_PRIVATE_PROPERTIES_ENABLE;

    public static final String SMALLRYE_PROPERTY_NAMING_STRATEGY = SMALLRYE_PREFIX + SUFFIX_PROPERTY_NAMING_STRATEGY;

    public static final String SMALLRYE_SORTED_PROPERTIES_ENABLE = SMALLRYE_PREFIX + SUFFIX_SORTED_PROPERTIES_ENABLE;

    public static final String SMALLRYE_REMOVE_UNUSED_SCHEMAS = SMALLRYE_PREFIX + SUFFIX_REMOVE_UNUSED_SCHEMAS_ENABLE;

    public static final String SMALLRYE_MERGE_SCHEMA_EXAMPLES = SMALLRYE_PREFIX + SUFFIX_MERGE_SCHEMA_EXAMPLES;

    public static final String SMALLRYE_SORTED_PARAMETERS_ENABLE = SMALLRYE_PREFIX + SUFFIX_SORTED_PARAMETERS_ENABLE;

    public static final String SCAN_PROFILES = SMALLRYE_PREFIX + "scan.profiles";

    public static final String SCAN_EXCLUDE_PROFILES = SMALLRYE_PREFIX + "scan.exclude.profiles";

    public static final String SCAN_RESOURCE_CLASS_PREFIX = SMALLRYE_PREFIX + "scan.resource-class.";

    public static final String SCAN_COMPOSITION_EXCLUDE_PACKAGES = SMALLRYE_PREFIX + "scan.composition.exclude.packages";

    public static final String VERSION = SMALLRYE_PREFIX + "openapi";

    public static final String INFO_TITLE = SMALLRYE_PREFIX + "info.title";
    public static final String INFO_VERSION = SMALLRYE_PREFIX + "info.version";
    public static final String INFO_DESCRIPTION = SMALLRYE_PREFIX + "info.description";
    public static final String INFO_SUMMARY = SMALLRYE_PREFIX + "info.summary";
    public static final String INFO_TERMS = SMALLRYE_PREFIX + "info.termsOfService";
    public static final String INFO_CONTACT_EMAIL = SMALLRYE_PREFIX + "info.contact.email";
    public static final String INFO_CONTACT_NAME = SMALLRYE_PREFIX + "info.contact.name";
    public static final String INFO_CONTACT_URL = SMALLRYE_PREFIX + "info.contact.url";
    public static final String INFO_LICENSE_NAME = SMALLRYE_PREFIX + "info.license.name";
    public static final String INFO_LICENSE_IDENTIFIER = SMALLRYE_PREFIX + "info.license.identifier";
    public static final String INFO_LICENSE_URL = SMALLRYE_PREFIX + "info.license.url";
    public static final String OPERATION_ID_STRAGEGY = SMALLRYE_PREFIX + "operationIdStrategy";
    public static final String DUPLICATE_OPERATION_ID_BEHAVIOR = SMALLRYE_PREFIX + "duplicateOperationIdBehavior";
    public static final String DEFAULT_PRODUCES = SMALLRYE_PREFIX + "defaultProduces";
    public static final String DEFAULT_CONSUMES = SMALLRYE_PREFIX + "defaultConsumes";
    public static final String DEFAULT_PRODUCES_PRIMITIVES = SMALLRYE_PREFIX + "defaultPrimitivesProduces";
    public static final String DEFAULT_CONSUMES_PRIMITIVES = SMALLRYE_PREFIX + "defaultPrimitivesConsumes";
    public static final String DEFAULT_PRODUCES_STREAMING = SMALLRYE_PREFIX + "defaultStreamingProduces";
    public static final String DEFAULT_CONSUMES_STREAMING = SMALLRYE_PREFIX + "defaultStreamingConsumes";

    public static final String MAXIMUM_STATIC_FILE_SIZE = SMALLRYE_PREFIX + "maximumStaticFileSize";
    public static final String AUTO_INHERITANCE = SMALLRYE_PREFIX + "auto-inheritance";

    public static final class Defaults {
        public static final String VERSION = "3.1.0";

        private Defaults() {
        }
    }
}
