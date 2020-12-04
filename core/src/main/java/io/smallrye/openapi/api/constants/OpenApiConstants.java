package io.smallrye.openapi.api.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.microprofile.openapi.OASConfig;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.io.schema.SchemaConstant;

/**
 * @author eric.wittmann@gmail.com
 */
public final class OpenApiConstants {
    public static final String VENDOR_NAME = "smallrye.";
    public static final String SUFFIX_SCAN_DEPENDENCIES_DISABLE = "scan-dependencies.disable";
    public static final String SUFFIX_SCAN_DEPENDENCIES_JARS = "scan-dependencies.jars";
    public static final String SUFFIX_SCHEMA_REFERENCES_ENABLE = "schema-references.enable";
    public static final String SUFFIX_ARRAY_REFERENCES_ENABLE = "array-references.enable";
    public static final String SUFFIX_CUSTOM_SCHEMA_REGISTRY_CLASS = "custom-schema-registry.class";
    public static final String SUFFIX_APP_PATH_DISABLE = "application-path.disable";

    public static final String SCAN_DEPENDENCIES_DISABLE = OASConfig.EXTENSIONS_PREFIX + SUFFIX_SCAN_DEPENDENCIES_DISABLE;
    public static final String SCAN_DEPENDENCIES_JARS = OASConfig.EXTENSIONS_PREFIX + SUFFIX_SCAN_DEPENDENCIES_JARS;
    public static final String SCHEMA_REFERENCES_ENABLE = OASConfig.EXTENSIONS_PREFIX + SUFFIX_SCHEMA_REFERENCES_ENABLE;
    public static final String CUSTOM_SCHEMA_REGISTRY_CLASS = OASConfig.EXTENSIONS_PREFIX + SUFFIX_CUSTOM_SCHEMA_REGISTRY_CLASS;
    public static final String APP_PATH_DISABLE = OASConfig.EXTENSIONS_PREFIX + SUFFIX_APP_PATH_DISABLE;

    public static final String SMALLRYE_SCAN_DEPENDENCIES_DISABLE = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME
            + SUFFIX_SCAN_DEPENDENCIES_DISABLE;
    public static final String SMALLRYE_SCAN_DEPENDENCIES_JARS = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME
            + SUFFIX_SCAN_DEPENDENCIES_JARS;
    public static final String SMALLRYE_SCHEMA_REFERENCES_ENABLE = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME
            + SUFFIX_SCHEMA_REFERENCES_ENABLE;
    public static final String SMALLRYE_ARRAY_REFERENCES_ENABLE = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME
            + SUFFIX_ARRAY_REFERENCES_ENABLE;
    public static final String SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME
            + SUFFIX_CUSTOM_SCHEMA_REGISTRY_CLASS;
    public static final String SMALLRYE_APP_PATH_DISABLE = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + SUFFIX_APP_PATH_DISABLE;

    public static final String VERSION = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "openapi";
    public static final String INFO_TITLE = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.title";
    public static final String INFO_VERSION = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.version";
    public static final String INFO_DESCRIPTION = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.description";
    public static final String INFO_TERMS = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.termsOfService";
    public static final String INFO_CONTACT_EMAIL = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.contact.email";
    public static final String INFO_CONTACT_NAME = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.contact.name";
    public static final String INFO_CONTACT_URL = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.contact.url";
    public static final String INFO_LICENSE_NAME = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.license.name";
    public static final String INFO_LICENSE_URL = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "info.license.url";
    public static final String OPERATION_ID_STRAGEGY = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "operationIdStrategy";
    public static final String DEFAULT_PRODUCES = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "defaultProduces";
    public static final String DEFAULT_CONSUMES = OASConfig.EXTENSIONS_PREFIX + VENDOR_NAME + "defaultConsumes";

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

    public static final String CLASS_SUFFIX = ".class";
    public static final String JAR_SUFFIX = ".jar";
    public static final String WEB_ARCHIVE_CLASS_PREFIX = "/WEB-INF/classes/";

    public static final String PROP_SERVER = "server";

    public static final String PROP_OPERATION_ID = "operationId";
    public static final String PROP_OPERATION_REF = "operationRef";

    public static final String PROP_MEDIA_TYPE = "mediaType";

    public static final Set<DotName> PROPERTY_ANNOTATIONS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(SchemaConstant.DOTNAME_SCHEMA,
                    JsonbConstants.JSONB_PROPERTY,
                    JacksonConstants.JSON_PROPERTY)));

    public static final String REF_PREFIX_API_RESPONSE = "#/components/responses/";
    public static final String REF_PREFIX_CALLBACK = "#/components/callbacks/";
    public static final String REF_PREFIX_EXAMPLE = "#/components/examples/";
    public static final String REF_PREFIX_HEADER = "#/components/headers/";
    public static final String REF_PREFIX_LINK = "#/components/links/";
    public static final String REF_PREFIX_PARAMETER = "#/components/parameters/";
    public static final String REF_PREFIX_REQUEST_BODY = "#/components/requestBodies/";
    public static final String REF_PREFIX_SCHEMA = "#/components/schemas/";
    public static final String REF_PREFIX_SECURITY_SCHEME = "#/components/securitySchemes/";

    public static final String VALUE = "value";
    public static final String REFS = "refs";
    // Shared public
    public static final String OPEN_API_VERSION = "3.0.3";
    public static final Supplier<String[]> DEFAULT_MEDIA_TYPES = () -> new String[] { "*/*" };
    // Used by both Jax-rs and openapi
    public static final String REF = "ref";

    /**
     * Constructor.
     */
    private OpenApiConstants() {
    }

}
