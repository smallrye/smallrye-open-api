package io.smallrye.openapi.runtime.util;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
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
     * Convenience method to retrieve the named parameter from an annotation.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param annotation the annotation from which to fetch the parameter
     * @param name the name of the parameter
     * @return an unwrapped annotation parameter value
     */
    @SuppressWarnings({ "unchecked", "squid:S3776" })
    public static <T> T value(AnnotationInstance annotation, String name) {
        final AnnotationValue value = annotation.value(name);

        if (value == null) {
            return null;
        }

        final boolean isArray = (AnnotationValue.Kind.ARRAY == value.kind());

        switch (isArray ? value.componentKind() : value.kind()) {
            case BOOLEAN:
                return (T) (isArray ? value.asBooleanArray() : value.asBoolean());
            case BYTE:
                return (T) (isArray ? value.asByteArray() : value.asByte());
            case CHARACTER:
                return (T) (isArray ? value.asCharArray() : value.asChar());
            case CLASS:
                return (T) (isArray ? value.asClassArray() : value.asClass());
            case DOUBLE:
                return (T) (isArray ? value.asDoubleArray() : value.asDouble());
            case ENUM:
                return (T) (isArray ? value.asEnumArray() : value.asEnum());
            case FLOAT:
                return (T) (isArray ? value.asFloatArray() : value.asFloat());
            case INTEGER:
                return (T) (isArray ? value.asIntArray() : value.asInt());
            case LONG:
                return (T) (isArray ? value.asLongArray() : value.asLong());
            case NESTED:
                return (T) (isArray ? value.asNestedArray() : value.asNested());
            case SHORT:
                return (T) (isArray ? value.asShortArray() : value.asShort());
            case STRING:
                return (T) (isArray ? value.asStringArray() : value.asString());
            case UNKNOWN:
            default:
                return null;
        }
    }

    /**
     * Convenience method to retrieve the named parameter from an annotation.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param annotation the annotation from which to fetch the parameter
     * @param name the name of the parameter
     * @param defaultValue a default value to return if the parameter is not defined
     * @return an unwrapped annotation parameter value
     */
    public static <T> T value(AnnotationInstance annotation, String name, T defaultValue) {
        T value = JandexUtil.value(annotation, name);
        return value != null ? value : defaultValue;
    }

    /**
     * Reads a String property value from the given annotation instance. If no value is found
     * this will return null.
     * 
     * @param annotation AnnotationInstance
     * @param propertyName String
     * @return String value
     */
    public static String stringValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        } else {
            return value.asString();
        }
    }

    public static Optional<String> optionalStringValue(AnnotationInstance annotation, String propertyName) {
        String value = stringValue(annotation, propertyName);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    /**
     * Reads a Boolean property value from the given annotation instance. If no value is found
     * this will return null.
     * 
     * @param annotation AnnotationInstance
     * @param propertyName String
     * @return Boolean value
     */
    public static Optional<Boolean> booleanValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value != null) {
            return Optional.of(value.asBoolean());
        }
        return Optional.empty();
    }

    /**
     * Reads a Boolean property from the given annotation instance. If no value is found
     * this will return false.
     * 
     * @param annotation AnnotationInstance
     * @param propertyName String
     * @return Boolean value
     */
    public static boolean booleanValueWithDefault(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        return value != null && value.asBoolean();
    }

    /**
     * Reads a Integer property value from the given annotation instance. If no value is found
     * this will return null.
     * 
     * @param annotation AnnotationInstance
     * @param propertyName String
     * @return Integer value
     */
    public static Integer intValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        } else {
            return value.asInt();
        }
    }

    /**
     * Reads a String array property value from the given annotation instance. If no value is found
     * this will return null.
     * 
     * @param annotation AnnotationInstance
     * @param propertyName String
     * @return List of Strings
     */
    public static Optional<List<String>> stringListValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value != null) {
            return Optional.of(new ArrayList<>(Arrays.asList(value.asStringArray())));
        }
        return Optional.empty();
    }

    /**
     * Reads a String property value from the given annotation instance. If no value is found
     * this will return null.
     * 
     * @param annotation AnnotationInstance
     * @param propertyName String
     * @param clazz Class type of the Enum
     * @param <T> Type parameter
     * @return Value of property
     */
    public static <T extends Enum<?>> T enumValue(AnnotationInstance annotation, String propertyName, Class<T> clazz) {
        AnnotationValue value = annotation != null ? annotation.value(propertyName) : null;
        if (value == null) {
            return null;
        }
        return enumValue(value.asString(), clazz);
    }

    /**
     * Converts a string value to the given enum type. If the string does not match
     * one of the the enum's values name (case-insensitive) or toString value, null
     * will be returned.
     * 
     * @param strVal String
     * @param clazz Class type of the Enum
     * @param <T> Type parameter
     * @return Value of property
     */
    public static <T extends Enum<?>> T enumValue(String strVal, Class<T> clazz) {
        T[] constants = clazz.getEnumConstants();
        for (T t : constants) {
            if (t.toString().equals(strVal)) {
                return t;
            }
        }
        for (T t : constants) {
            if (t.name().equalsIgnoreCase(strVal)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns true if the given annotation instance is a "ref". An annotation is a ref if it has
     * a non-null value for the "ref" property.
     * 
     * @param annotation AnnotationInstance
     * @return Whether it's a "ref"
     */
    public static boolean isRef(AnnotationInstance annotation) {
        return annotation.value(OpenApiConstants.REF) != null;
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
     * Gets a single class annotation from the given class. Returns null if no matching annotation
     * is found.
     * 
     * @param ct ClassInfo
     * @param name DotName
     * @return AnnotationInstance
     */
    public static AnnotationInstance getClassAnnotation(ClassInfo ct, DotName name) {
        return getClassAnnotation(ct, Arrays.asList(name));
    }

    /**
     * Gets a single class annotation from the given class. Returns null if no matching annotation
     * is found.
     * 
     * @param ct ClassInfo
     * @param names List of DotNames
     * @return AnnotationInstance
     */
    public static AnnotationInstance getClassAnnotation(ClassInfo ct, Collection<DotName> names) {
        if (names == null || names.isEmpty()) {
            return null;
        }

        for (DotName dn : names) {
            AnnotationInstance classAnnotation = ct.classAnnotation(dn);
            if (classAnnotation != null) {
                return classAnnotation;
            }
        }

        return null;
    }

    public static AnnotationInstance getAnnotation(MethodInfo mi, DotName... names) {
        return getAnnotation(mi, Arrays.asList(names));
    }

    public static AnnotationInstance getAnnotation(FieldInfo field, DotName name) {
        return getAnnotation(field, Arrays.asList(name));
    }

    /**
     * Gets a single annotation from the given field. Returns null if no matching annotation
     * is found.
     * 
     * @param field FieldInfo
     * @param names DotName
     * @return AnnotationInstance
     */
    public static AnnotationInstance getAnnotation(FieldInfo field, Collection<DotName> names) {
        if (names == null || names.isEmpty()) {
            return null;
        }

        for (DotName dn : names) {
            AnnotationInstance annotation = field.annotation(dn);
            if (annotation != null)
                return annotation;
        }

        return null;
    }

    /**
     * Gets a single annotation from the given method. Returns null if no matching annotation
     * is found.
     * 
     * @param mi MethodInfo
     * @param names DotName
     * @return AnnotationInstance
     */
    public static AnnotationInstance getAnnotation(MethodInfo mi, Collection<DotName> names) {
        if (names == null || names.isEmpty()) {
            return null;
        }

        for (DotName dn : names) {
            AnnotationInstance annotation = mi.annotation(dn);
            if (annotation != null)
                return annotation;
        }

        return null;
    }

    /**
     * Return if any one of the listed annotations exist
     * 
     * @param method
     * @param annotations
     * @return
     */
    public static boolean hasAnyOneOfAnnotation(final MethodInfo method, DotName... annotations) {
        return hasAnyOneOfAnnotation(method, Arrays.asList(annotations));
    }

    /**
     * Return if any one of the listed annotations exist
     * 
     * @param method
     * @param annotations
     * @return
     */
    public static boolean hasAnyOneOfAnnotation(final MethodInfo method, Collection<DotName> annotations) {
        for (DotName dotName : annotations) {
            if (method.hasAnnotation(dotName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all annotations configured for a single parameter of a method.
     * 
     * @param method MethodInfo
     * @param paramPosition parameter position
     * @return List of AnnotationInstance's
     */
    public static List<AnnotationInstance> getParameterAnnotations(MethodInfo method, short paramPosition) {
        return method.annotations()
                .stream()
                .filter(annotation -> {
                    AnnotationTarget target = annotation.target();
                    return target != null && target.kind() == Kind.METHOD_PARAMETER
                            && target.asMethodParameter().position() == paramPosition;
                })
                .collect(toList());
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

    /**
     * Many OAI annotations can either be found singly or as a wrapped array. This method will
     * look for both and return a list of all found. Both the single and wrapper annotation names
     * must be provided.
     * 
     * @param target the annotated target (e.g. ClassInfo, MethodInfo)
     * @param singleAnnotationName DotName
     * @param repeatableAnnotationName DotName
     * @return List of AnnotationInstance's
     */
    public static List<AnnotationInstance> getRepeatableAnnotation(AnnotationTarget target,
            DotName singleAnnotationName,
            DotName repeatableAnnotationName) {

        List<AnnotationInstance> annotations = new ArrayList<>();

        AnnotationInstance annotation = TypeUtil.getAnnotation(target, singleAnnotationName);

        if (annotation != null) {
            annotations.add(annotation);
        }

        if (repeatableAnnotationName != null) {
            AnnotationInstance[] nestedArray = TypeUtil.getAnnotationValue(target,
                    repeatableAnnotationName,
                    OpenApiConstants.VALUE);

            if (nestedArray != null) {
                annotations.addAll(Arrays.asList(nestedArray));
            }
        }

        return annotations;
    }

    /**
     * Returns the class type of the method parameter at the given position.
     * 
     * @param method MethodInfo
     * @param position parameter position
     * @return Type
     */
    public static Type getMethodParameterType(MethodInfo method, short position) {
        return method.parameterType(position);
    }

    /**
     * Returns the class type of the method parameter.
     *
     * @param parameter the {@link MethodParameterInfo parameter}
     * @return Type
     */
    public static Type getMethodParameterType(MethodParameterInfo parameter) {
        return parameter.method().parameterType(parameter.position());
    }

    /**
     * Finds an annotation (if present) with the given name, on a particular parameter of a
     * method.Returns null if not found.
     * 
     * @param method the method
     * @param parameterIndex the parameter index
     * @param annotationName name of annotation we are looking for
     * @return the Annotation instance
     */
    public static AnnotationInstance getMethodParameterAnnotation(MethodInfo method, int parameterIndex,
            DotName annotationName) {
        for (AnnotationInstance annotation : method.annotations()) {
            if (annotation.target().kind() == Kind.METHOD_PARAMETER &&
                    annotation.target().asMethodParameter().position() == parameterIndex &&
                    annotation.name().equals(annotationName)) {
                return annotation;
            }
        }
        return null;
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

        org.eclipse.microprofile.openapi.models.media.Schema.SchemaType type = JandexUtil.enumValue(annotation,
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

    /**
     * Builds an insertion-order map of a class's inheritance chain, starting
     * with the klazz argument.
     *
     * @param index index for superclass retrieval
     * @param klazz the class to retrieve inheritance
     * @param type type of the klazz
     * @return map of a class's inheritance chain/ancestry
     */
    public static Map<ClassInfo, Type> inheritanceChain(IndexView index, ClassInfo klazz, Type type) {
        Map<ClassInfo, Type> chain = new LinkedHashMap<>();

        do {
            chain.put(klazz, type);
        } while ((type = klazz.superClassType()) != null &&
                (klazz = index.getClassByName(TypeUtil.getName(type))) != null);

        return chain;
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

    /**
     * Retrieve the unique <code>Type</code>s that the given <code>ClassInfo</code>
     * implements.
     * 
     * @param index
     * @param klass
     * @return the <code>Set</code> of interfaces
     * 
     */
    public static Set<Type> interfaces(AugmentedIndexView index, ClassInfo klass) {
        Set<Type> interfaces = new LinkedHashSet<>();

        for (Type type : klass.interfaceTypes()) {
            interfaces.add(type);

            if (index.containsClass(type)) {
                interfaces.addAll(interfaces(index, index.getClass(type)));
            }
        }

        return interfaces;
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
