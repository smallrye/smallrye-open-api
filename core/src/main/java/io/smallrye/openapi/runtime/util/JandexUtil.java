package io.smallrye.openapi.runtime.util;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Some utility methods for working with Jandex objects.
 *
 * @author eric.wittmann@gmail.com
 */
public class JandexUtil {

    /**
     * Constructor.
     */
    private JandexUtil() {
    }

    public static String createUniqueMethodReference(ClassInfo classInfo, MethodInfo methodInfo) {
        return "m" + classInfo.hashCode() + "_" + methodInfo.hashCode();
    }

    /**
     * Returns true if the given annotation instance is a "ref". An annotation is a ref if it has
     * a non-null value for the "ref" property.
     *
     * @param annotation AnnotationInstance
     * @return Whether it's a "ref"
     */
    public static boolean isRef(AnnotationInstance annotation) {
        return ReferenceType.isReference(annotation);
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
        String ref = annotation.value(ReferenceType.PROP_ANNOTATION).asString();
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
    public static boolean isSimpleArraySchema(AnnotationScannerContext context, AnnotationInstance annotation) {
        // May only have 'type' display property
        if (schemaDisplayValues(annotation).size() != 1) {
            return false;
        }

        return isArraySchema(context, annotation);
    }

    /**
     * Returns true if the given {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * annotation is an array schema. This is defined as a schema with a "type" field and "implementation"
     * field defined *and* the type must be array.
     *
     * @param annotation AnnotationInstance
     * @return Is it an array {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     */
    public static boolean isArraySchema(AnnotationScannerContext context, AnnotationInstance annotation) {
        if (!hasImplementation(annotation)) {
            return false;
        }

        org.eclipse.microprofile.openapi.models.media.Schema.SchemaType type = context.annotations().enumValue(annotation,
                SchemaConstant.PROP_TYPE, org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.class);

        return (type == org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.ARRAY);
    }

    /**
     * Returns true if the given {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * annotation is a "boolean schema". This is defined as a schema with an implementation that is equal
     * to either {@link org.eclipse.microprofile.openapi.annotations.media.Schema.True Schema.True} or
     * {@link org.eclipse.microprofile.openapi.annotations.media.Schema.False Schema.False}.
     *
     * @param annotation schema annotation instance
     * @return true if it has a boolean schema implementation, otherwise false.
     */
    public static boolean isBooleanSchema(AnnotationInstance annotation) {
        if (!hasImplementation(annotation)) {
            return false;
        }

        AnnotationValue impl = annotation.value(SchemaConstant.PROP_IMPLEMENTATION);

        if (impl == null) {
            return false;
        }

        DotName name = impl.asClass().name();
        return name.equals(SchemaConstant.DOTNAME_TRUE_SCHEMA) || name.equals(SchemaConstant.DOTNAME_FALSE_SCHEMA);
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
