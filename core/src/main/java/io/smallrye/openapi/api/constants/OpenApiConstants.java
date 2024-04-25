package io.smallrye.openapi.api.constants;

import io.smallrye.openapi.api.SmallRyeOASConfig;

/**
 * @author eric.wittmann@gmail.com
 * @deprecated use {@link SmallRyeOASConfig} instead
 */
@Deprecated
public final class OpenApiConstants {

    public static final String SCAN_DEPENDENCIES_DISABLE = SmallRyeOASConfig.SCAN_DEPENDENCIES_DISABLE;
    public static final String SCAN_DEPENDENCIES_JARS = SmallRyeOASConfig.SCAN_DEPENDENCIES_JARS;
    public static final String SCHEMA_REFERENCES_ENABLE = SmallRyeOASConfig.SCHEMA_REFERENCES_ENABLE;
    public static final String CUSTOM_SCHEMA_REGISTRY_CLASS = SmallRyeOASConfig.CUSTOM_SCHEMA_REGISTRY_CLASS;
    public static final String APP_PATH_DISABLE = SmallRyeOASConfig.APP_PATH_DISABLE;

    public static final String SMALLRYE_SCAN_DEPENDENCIES_DISABLE = SmallRyeOASConfig.SMALLRYE_SCAN_DEPENDENCIES_DISABLE;
    public static final String SMALLRYE_SCAN_DEPENDENCIES_JARS = SmallRyeOASConfig.SMALLRYE_SCAN_DEPENDENCIES_JARS;
    public static final String SMALLRYE_ARRAY_REFERENCES_ENABLE = SmallRyeOASConfig.SMALLRYE_ARRAY_REFERENCES_ENABLE;
    public static final String SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS = SmallRyeOASConfig.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS;
    public static final String SMALLRYE_APP_PATH_DISABLE = SmallRyeOASConfig.SMALLRYE_APP_PATH_DISABLE;
    public static final String SMALLRYE_PRIVATE_PROPERTIES_ENABLE = SmallRyeOASConfig.SMALLRYE_PRIVATE_PROPERTIES_ENABLE;
    public static final String SMALLRYE_PROPERTY_NAMING_STRATEGY = SmallRyeOASConfig.SMALLRYE_PROPERTY_NAMING_STRATEGY;
    public static final String SMALLRYE_SORTED_PROPERTIES_ENABLE = SmallRyeOASConfig.SMALLRYE_SORTED_PROPERTIES_ENABLE;
    public static final String SMALLRYE_REMOVE_UNUSED_SCHEMAS = SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_SCHEMAS;
    public static final String SCAN_PROFILES = SmallRyeOASConfig.SCAN_PROFILES;
    public static final String SCAN_EXCLUDE_PROFILES = SmallRyeOASConfig.SCAN_EXCLUDE_PROFILES;
    public static final String SCAN_RESOURCE_CLASS_PREFIX = SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX;
    public static final String SCAN_COMPOSITION_EXCLUDE_PACKAGES = SmallRyeOASConfig.SCAN_COMPOSITION_EXCLUDE_PACKAGES;

    public static final String VERSION = SmallRyeOASConfig.VERSION;
    public static final String INFO_TITLE = SmallRyeOASConfig.INFO_TITLE;
    public static final String INFO_VERSION = SmallRyeOASConfig.INFO_VERSION;
    public static final String INFO_DESCRIPTION = SmallRyeOASConfig.INFO_DESCRIPTION;
    public static final String INFO_TERMS = SmallRyeOASConfig.INFO_TERMS;
    public static final String INFO_CONTACT_EMAIL = SmallRyeOASConfig.INFO_CONTACT_EMAIL;
    public static final String INFO_CONTACT_NAME = SmallRyeOASConfig.INFO_CONTACT_NAME;
    public static final String INFO_CONTACT_URL = SmallRyeOASConfig.INFO_CONTACT_URL;
    public static final String INFO_LICENSE_NAME = SmallRyeOASConfig.INFO_LICENSE_NAME;
    public static final String INFO_LICENSE_URL = SmallRyeOASConfig.INFO_LICENSE_URL;
    public static final String OPERATION_ID_STRAGEGY = SmallRyeOASConfig.OPERATION_ID_STRAGEGY;
    public static final String DUPLICATE_OPERATION_ID_BEHAVIOR = SmallRyeOASConfig.DUPLICATE_OPERATION_ID_BEHAVIOR;
    public static final String DEFAULT_PRODUCES = SmallRyeOASConfig.DEFAULT_PRODUCES;
    public static final String DEFAULT_CONSUMES = SmallRyeOASConfig.DEFAULT_CONSUMES;
    public static final String DEFAULT_PRODUCES_PRIMITIVES = SmallRyeOASConfig.DEFAULT_PRODUCES_PRIMITIVES;
    public static final String DEFAULT_CONSUMES_PRIMITIVES = SmallRyeOASConfig.DEFAULT_CONSUMES_PRIMITIVES;
    public static final String DEFAULT_PRODUCES_STREAMING = SmallRyeOASConfig.DEFAULT_PRODUCES_STREAMING;
    public static final String DEFAULT_CONSUMES_STREAMING = SmallRyeOASConfig.DEFAULT_CONSUMES_STREAMING;

    public static final String MAXIMUM_STATIC_FILE_SIZE = SmallRyeOASConfig.MAXIMUM_STATIC_FILE_SIZE;
    public static final String AUTO_INHERITANCE = SmallRyeOASConfig.AUTO_INHERITANCE;

    /**
     * Constructor.
     */
    private OpenApiConstants() {
    }

}
