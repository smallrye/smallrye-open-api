package io.smallrye.openapi.model;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.Components;
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
    HEADER(Header.class, "headers", Components::getHeaders, Components::removeHeader),
    SCHEMA(Schema.class, "schemas", Components::getSchemas, Components::removeSchema),
    SECURITY_SCHEME(SecurityScheme.class, "securitySchemes", Components::getSecuritySchemes, Components::removeSecurityScheme),
    CALLBACK(Callback.class, "callbacks", Components::getCallbacks, Components::removeCallback),
    LINK(Link.class, "links", Components::getLinks, Components::removeLink),
    RESPONSE(APIResponse.class, "responses", Components::getResponses, Components::removeResponse),
    PARAMETER(Parameter.class, "parameters", Components::getParameters, Components::removeParameter),
    EXAMPLE(Example.class, "examples", Components::getExamples, Components::removeExample),
    REQUEST_BODY(RequestBody.class, "requestBodies", Components::getRequestBodies, Components::removeRequestBody),
    PATH_ITEM(PathItem.class, "pathItems", Components::getPathItems, Components::removePathItem);

    private static final Pattern COMPONENT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");
    public static final String PROP_ANNOTATION = "ref";
    public static final String PROP_STANDARD = "$ref";

    Class<? extends Reference<?>> modelType;
    String componentPath;
    Function<Components, Map<String, ? extends Reference<?>>> extractor;
    BiConsumer<Components, String> remover;

    ReferenceType(
            Class<? extends Reference<?>> modelType,
            String componentPath,
            Function<Components, Map<String, ? extends Reference<?>>> extractor,
            BiConsumer<Components, String> remover) {
        this.modelType = modelType;
        this.componentPath = componentPath;
        this.extractor = extractor;
        this.remover = remover;
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

    public String componentPath() {
        return componentPath;
    }

    public String referencePrefix() {
        return "#/components/" + componentPath;
    }

    public String referenceOf(String ref) {
        return referencePrefix() + "/" + ref;
    }

    /**
     * Takes the value from a ref property from an annotation, and converts it to a JSON Pointer, suitable for use as a
     * reference in an OpenAPI model.
     *
     * @param ref the ref value read from an annotation
     * @return a value suitable for use in an OpenAPI model.
     */
    public String parseRefValue(String ref) {
        if (ref == null) {
            return null;
        }

        if (!COMPONENT_KEY_PATTERN.matcher(ref).matches()) {
            return ref;
        }

        return referenceOf(ref);
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
        return parseRefValue(ref);
    }

    @SuppressWarnings("unchecked")
    public <T extends Reference<T>> Map<String, T> get(Components components) {
        return (Map<String, T>) extractor.apply(components);
    }

    public void remove(Components components, String name) {
        remover.accept(components, name);
    }
}
