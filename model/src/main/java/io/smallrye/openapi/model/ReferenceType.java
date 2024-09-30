package io.smallrye.openapi.model;

import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Reference;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * Simple enum to indicate the type of a $ref being read/written.
 *
 * @author eric.wittmann@gmail.com
 */
public enum ReferenceType {
    HEADER(Header.class, "headers"),
    SCHEMA(Schema.class, "schemas"),
    SECURITY_SCHEME(SecurityScheme.class, "securitySchemes"),
    CALLBACK(Callback.class, "callbacks"),
    LINK(Link.class, "links"),
    RESPONSE(APIResponse.class, "responses"),
    PARAMETER(Parameter.class, "parameters"),
    EXAMPLE(Example.class, "examples"),
    REQUEST_BODY(RequestBody.class, "requestBodies"),
    PATH_ITEM(PathItem.class, "pathItems");

    private static final Pattern COMPONENT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");
    public static final String PROP_ANNOTATION = "ref";
    public static final String PROP_STANDARD = "$ref";

    Class<? extends Reference<?>> modelType;
    String componentPath;

    ReferenceType(Class<? extends Reference<?>> modelType, String componentPath) {
        this.modelType = modelType;
        this.componentPath = componentPath;
    }

    public static ReferenceType fromModel(Reference<?> model) {
        for (ReferenceType ref : values()) {
            if (ref.modelType.isAssignableFrom(model.getClass())) {
                return ref;
            }
        }
        return null;
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
