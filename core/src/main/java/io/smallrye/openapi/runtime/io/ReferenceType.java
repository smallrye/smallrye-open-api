package io.smallrye.openapi.runtime.io;

import java.util.regex.Pattern;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.constants.OpenApiConstants;

/**
 * Simple enum to indicate the type of a $ref being read/written.
 *
 * @author eric.wittmann@gmail.com
 */
public enum ReferenceType {
    HEADER("headers"),
    SCHEMA("schemas"),
    SECURITY_SCHEME("securitySchemes"),
    CALLBACK("callbacks"),
    LINK("links"),
    RESPONSE("responses"),
    PARAMETER("parameters"),
    EXAMPLE("examples"),
    REQUEST_BODY("requestBodies");

    private static final Pattern COMPONENT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");
    String componentPath;

    ReferenceType(String componentPath) {
        this.componentPath = componentPath;
    }

    public static ReferenceType fromComponentPath(String path) {
        for (ReferenceType ref : values()) {
            if (ref.componentPath.equals(path)) {
                return ref;
            }
        }
        return null;
    }

    /**
     * Reads a string property named "ref" value from the given annotation and converts it
     * to a value appropriate for setting on a model's "$ref" property.
     *
     * @param annotation AnnotationInstance
     * @return String value
     */
    public String refValue(AnnotationInstance annotation) {
        AnnotationValue value = annotation.value(OpenApiConstants.REF);
        if (value == null) {
            return null;
        }

        String ref = value.asString();

        if (!COMPONENT_KEY_PATTERN.matcher(ref).matches()) {
            return ref;
        }

        return "#/components/" + componentPath + "/" + ref;
    }
}
