package io.smallrye.openapi.api.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.DotName;

/**
 * @author eric.wittmann@gmail.com
 */
public final class OpenApiConstants {
    public static final String SCAN_DEPENDENCIES_DISABLE = "mp.openapi.extensions.scan-dependencies.disable";
    public static final String SCAN_DEPENDENCIES_JARS = "mp.openapi.extensions.scan-dependencies.jars";
    public static final String SCHEMA_REFERENCES_ENABLE = "mp.openapi.extensions.schema-references.enable";
    public static final String CUSTOM_SCHEMA_REGISTRY_CLASS = "mp.openapi.extensions.custom-schema-registry.class";
    public static final String APP_PATH_DISABLE = "mp.openapi.extensions.application-path.disable";

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
            .unmodifiableSet(new HashSet<>(Arrays.asList(MPOpenApiConstants.SCHEMA.TYPE_SCHEMA,
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

    /**
     * Constructor.
     */
    private OpenApiConstants() {
    }

}
