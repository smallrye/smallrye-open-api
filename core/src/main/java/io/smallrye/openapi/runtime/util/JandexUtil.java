package io.smallrye.openapi.runtime.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Some utility methods for working with Jandex objects.
 *
 * @author eric.wittmann@gmail.com
 */
public class JandexUtil {

    private static final Pattern COMPONENT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");

    /**
     * Simple enum to indicate the type of a $ref being read/written.
     *
     * @author eric.wittmann@gmail.com
     */
    public enum RefType {
        HEADER("headers"),
        SCHEMA("schemas"),
        SECURITY_SCHEME("securitySchemes"),
        CALLBACK("callbacks"),
        LINK("links"),
        RESPONSE("responses"),
        PARAMETER("parameters"),
        EXAMPLE("examples"),
        REQUEST_BODY("requestBodies");

        String componentPath;

        RefType(String componentPath) {
            this.componentPath = componentPath;
        }

        public static RefType fromComponentPath(String path) {
            for (RefType ref : values()) {
                if (ref.componentPath.equals(path)) {
                    return ref;
                }
            }
            return null;
        }
    }

    /**
     * Constructor.
     */
    private JandexUtil() {
    }

    public static String createUniqueAnnotationTargetRef(AnnotationTarget annotationTarget) {
        switch (annotationTarget.kind()) {
            case FIELD:
                return JandexUtil.createUniqueFieldRef(annotationTarget.asField());
            case METHOD:
                ClassInfo classInfo = annotationTarget.asMethod().declaringClass();
                return JandexUtil.createUniqueMethodReference(classInfo, annotationTarget.asMethod());
            case METHOD_PARAMETER:
                return JandexUtil.createUniqueMethodParameterRef(annotationTarget.asMethodParameter());
            default:
                return null;
        }
    }

    public static String createUniqueFieldRef(FieldInfo fieldInfo) {
        ClassInfo classInfo = fieldInfo.declaringClass();
        return "f" + classInfo.hashCode() + "_" + fieldInfo.hashCode();
    }

    public static String createUniqueMethodReference(ClassInfo classInfo, MethodInfo methodInfo) {
        return "m" + classInfo.hashCode() + "_" + methodInfo.hashCode();
    }

    public static String createUniqueMethodParameterRef(MethodParameterInfo methodParameter) {
        final MethodInfo methodInfo = methodParameter.method();
        final ClassInfo classInfo = methodInfo.declaringClass();
        return "p" + classInfo.hashCode() + "_" + methodInfo.hashCode() + "_" + methodParameter.position();
    }

    /**
     * Reads a string property named "ref" value from the given annotation and converts it
     * to a value appropriate for setting on a model's "$ref" property.
     *
     * @param annotation AnnotationInstance
     * @param refType RefType
     * @return String value
     */
    public static String refValue(AnnotationInstance annotation, RefType refType) {
        AnnotationValue value = annotation.value(OpenApiConstants.REF);
        if (value == null) {
            return null;
        }

        String ref = value.asString();

        if (!COMPONENT_KEY_PATTERN.matcher(ref).matches()) {
            return ref;
        }

        if (refType != null) {
            ref = "#/components/" + refType.componentPath + "/" + ref;
        } else {
            throw UtilMessages.msg.refTypeNotNull();
        }

        return ref;
    }

    /**
     * Returns true if the given annotation instance is a "ref". An annotation is a ref if it has
     * a non-null value for the "ref" property.
     *
     * @param annotation AnnotationInstance
     * @return Whether it's a "ref"
     */
    public static boolean isRef(AnnotationInstance annotation) {
        return annotation != null && annotation.value(OpenApiConstants.REF) != null;
    }

    /**
     * Returns true if the given annotation is void of any values (and thus is "empty"). An example
     * of this would be if a jax-rs method were annotated with @Tag()
     *
     * @param annotation AnnotationInstance
     * @return Whether it's empty
     */
    public static boolean isEmpty(AnnotationInstance annotation) {
        return annotation.values() == null || annotation.values().isEmpty();
    }

    /**
     * Gets the name of an item from its ref. For example, the ref might be "#/components/parameters/departureDate"
     * which would result in a name of "departureDate".
     *
     * @param annotation AnnotationInstance
     * @return Name of item from ref
     */
    public static String nameFromRef(AnnotationInstance annotation) {
        String ref = annotation.value(OpenApiConstants.REF).asString();
        return ModelUtil.nameFromRef(ref);
    }

    public static List<AnnotationValue> schemaDisplayValues(AnnotationInstance annotation) {
        return annotation.values()
                .stream()
                .filter(value -> !SchemaConstant.PROPERTIES_NONDISPLAY.contains(value.name()))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the given @Schema annotation is a simple class schema. This means that
     * the annotation only has one field defined, and that field is "implementation".
     *
     * @param annotation AnnotationInstance
     * @return Is it a simple class @Schema
     */
    public static boolean isSimpleClassSchema(AnnotationInstance annotation) {
        return schemaDisplayValues(annotation).isEmpty() && hasImplementation(annotation);
    }

    /**
     * Returns true if the given @Schema annotation is a simple array schema. This is defined
     * as a schema with only a "type" field and "implementation" field defined *and* the type must
     * be array.
     *
     * @param annotation AnnotationInstance
     * @return Is it a simple array @Schema
     */
    public static boolean isSimpleArraySchema(AnnotationInstance annotation) {
        // May only have 'type' display property
        if (schemaDisplayValues(annotation).size() != 1) {
            return false;
        }

        return isArraySchema(annotation);
    }

    /**
     * Returns true if the given {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * annotation is an array schema. This is defined as a schema with a "type" field and "implementation"
     * field defined *and* the type must be array.
     *
     * @param annotation AnnotationInstance
     * @return Is it an array {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     */
    public static boolean isArraySchema(AnnotationInstance annotation) {
        if (!hasImplementation(annotation)) {
            return false;
        }

        org.eclipse.microprofile.openapi.models.media.Schema.SchemaType type = Annotations.enumValue(annotation,
                SchemaConstant.PROP_TYPE, org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.class);

        return (type == org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.ARRAY);
    }

    /**
     * Returns true if the given {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * annotation has defined an "implementation" field.
     *
     * @param annotation AnnotationInstance
     * @return true if the annotation defines an implementation, otherwise false
     */
    public static boolean hasImplementation(AnnotationInstance annotation) {
        return annotation.value(SchemaConstant.PROP_IMPLEMENTATION) != null;
    }

    public static boolean equals(AnnotationTarget t1, AnnotationTarget t2) {
        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null || t1.kind() != t2.kind()) {
            return false;
        }

        switch (t1.kind()) {
            case CLASS:
                return equals(t1.asClass(), t2.asClass());
            case FIELD:
                return equals(t1.asField(), t2.asField());
            case METHOD_PARAMETER:
                return equals(t1.asMethodParameter(), t2.asMethodParameter());
            default:
                return t1.equals(t2);
        }
    }

    public static boolean equals(ClassInfo c1, ClassInfo c2) {
        return c1.name().equals(c2.name());
    }

    public static boolean equals(FieldInfo f1, FieldInfo f2) {
        return equals(f1.declaringClass(), f2.declaringClass()) && f1.name().equals(f2.name());
    }

    public static boolean equals(MethodParameterInfo p1, MethodParameterInfo p2) {
        return p1.method().equals(p2.method()) && p1.position() == p2.position();
    }

    public static List<FieldInfo> fields(AnnotationScannerContext context, ClassInfo currentClass) {
        if (context.getConfig().sortedPropertiesEnable()) {
            return currentClass.fields();
        }
        return currentClass.unsortedFields();
    }

    public static boolean isSupplier(AnnotationTarget target) {
        if (target.kind() != Kind.METHOD) {
            return false;
        }

        MethodInfo method = target.asMethod();

        return method.returnType().kind() != Type.Kind.VOID && method.parameterTypes().isEmpty();
    }

}
