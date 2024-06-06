package io.smallrye.openapi.runtime.io;

import java.util.Optional;
import java.util.regex.Pattern;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

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
    REQUEST_BODY("requestBodies"),
    PATH_ITEMS("pathItems");

    private static final Pattern COMPONENT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");
    public static final String PROP_ANNOTATION = "ref";
    public static final String PROP_STANDARD = "$ref";

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

    public static boolean isReference(AnnotationInstance annotation) {
        return annotation != null && annotation.value(PROP_ANNOTATION) != null;
    }

    public static String referenceValue(AnnotationInstance annotation) {
        return Optional.ofNullable(annotation.value(PROP_ANNOTATION))
                .map(AnnotationValue::asString)
                .orElse(null);
    }

    public String referencePrefix() {
        return "#/components/" + componentPath;
    }

    public String referenceOf(String ref) {
        return referencePrefix() + "/" + ref;
    }

    /**
     * Reads a string property named "ref" value from the given annotation and converts it
     * to a value appropriate for setting on a model's "$ref" property.
     *
     * @param annotation AnnotationInstance
     * @return String value
     */
    public String refValue(AnnotationInstance annotation) {
        String ref = referenceValue(annotation);

        if (ref == null) {
            return null;
        }

        if (!COMPONENT_KEY_PATTERN.matcher(ref).matches()) {
            return ref;
        }

        return referenceOf(ref);
    }
}
