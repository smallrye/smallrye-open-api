package io.smallrye.openapi.runtime.util;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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

import io.smallrye.openapi.api.constants.OpenApiConstants;

public final class Annotations {

    private Annotations() {
    }

    public static <T> T value(AnnotationInstance annotation) {
        return annotation != null ? value(annotation, OpenApiConstants.VALUE) : null;
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
        T value = Annotations.value(annotation, name);
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

        AnnotationInstance annotation = Annotations.getAnnotation(target, singleAnnotationName);

        if (annotation != null) {
            annotations.add(annotation);
        }

        if (repeatableAnnotationName != null) {
            AnnotationInstance[] nestedArray = Annotations.getAnnotationValue(target,
                    repeatableAnnotationName,
                    OpenApiConstants.VALUE);

            if (nestedArray != null) {
                Arrays.stream(nestedArray)
                        .map(a -> AnnotationInstance.create(a.name(), target, a.values()))
                        .forEach(annotations::add);
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
        return method.annotations(annotationName)
                .stream()
                .filter(annotation -> annotation.target().kind() == Kind.METHOD_PARAMETER)
                .filter(annotation -> annotation.target().asMethodParameter().position() == parameterIndex)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds an annotation (if present) with the given name, on a particular parameter of a
     * method based on the identity of the parameterType. Returns null if not found.
     * 
     * @param method the method
     * @param parameterType the parameter type
     * @param annotationName name of annotation we are looking for
     * @return the Annotation instance
     */
    public static AnnotationInstance getMethodParameterAnnotation(MethodInfo method, Type parameterType,
            DotName annotationName) {
        // parameterType must be the same object as in the method's parameter type array
        int parameterIndex = method.parameterTypes().indexOf(parameterType);
        return getMethodParameterAnnotation(method, parameterIndex, annotationName);
    }

    /**
     * Finds all annotations on a particular parameter of a method based on the identity of the parameterType.
     * 
     * @param method the method
     * @param parameterType the parameter type
     * @return the list of annotations, never null
     */
    public static List<AnnotationInstance> getMethodParameterAnnotations(MethodInfo method, Type parameterType) {
        // parameterType must be the same object as in the method's parameter type array
        int parameterIndex = method.parameterTypes().indexOf(parameterType);
        return method.annotations()
                .stream()
                .filter(annotation -> annotation.target().kind() == Kind.METHOD_PARAMETER)
                .filter(annotation -> annotation.target().asMethodParameter().position() == parameterIndex)
                .collect(Collectors.toList());
    }

    public static boolean hasAnnotation(AnnotationTarget target, List<DotName> annotationNames) {
        for (DotName dn : annotationNames) {
            if (hasAnnotation(target, dn)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnnotation(AnnotationTarget target, DotName annotationName) {
        if (target == null) {
            return false;
        }
        switch (target.kind()) {
            case CLASS:
                return target.asClass().classAnnotation(annotationName) != null;
            case FIELD:
                return target.asField().hasAnnotation(annotationName);
            case METHOD:
                return target.asMethod().hasAnnotation(annotationName);
            case METHOD_PARAMETER:
                MethodParameterInfo parameter = target.asMethodParameter();
                return parameter.method()
                        .annotations()
                        .stream()
                        .filter(a -> a.target().kind() == Kind.METHOD_PARAMETER)
                        .filter(a -> a.target().asMethodParameter().position() == parameter.position())
                        .anyMatch(a -> a.name().equals(annotationName));
            case TYPE:
            case RECORD_COMPONENT:
                break;
        }

        return false;
    }

    public static AnnotationInstance getAnnotation(AnnotationTarget annotationTarget, DotName annotationName) {
        if (annotationTarget == null) {
            return null;
        }
        return getAnnotations(annotationTarget).stream()
                .filter(annotation -> annotation.name().equals(annotationName))
                .findFirst()
                .orElse(null);
    }

    public static AnnotationInstance getAnnotation(AnnotationTarget annotationTarget, Collection<DotName> annotationNames) {
        if (annotationTarget == null) {
            return null;
        }
        for (DotName dn : annotationNames) {
            AnnotationInstance ai = getAnnotation(annotationTarget, dn);
            if (ai != null)
                return ai;
        }
        return null;
    }

    /**
     * Convenience method to retrieve the "value" parameter from an annotation bound to the target.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param target the target object annotated with the annotation named by annotationName
     * @param annotationName name of the annotation from which to retrieve the value
     * @return an unwrapped annotation parameter value
     */
    public static <T> T getAnnotationValue(AnnotationTarget target, DotName annotationName) {
        return getAnnotationValue(target, Arrays.asList(annotationName), OpenApiConstants.VALUE);
    }

    public static <T> T getAnnotationValue(AnnotationTarget target, List<DotName> annotationNames) {
        return getAnnotationValue(target, annotationNames, OpenApiConstants.VALUE, null);
    }

    /**
     * Convenience method to retrieve the named parameter from an annotation bound to the target.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param target the target object annotated with the annotation named by annotationName
     * @param annotationName name of the annotation from which to retrieve the value
     * @param propertyName the name of the parameter/property in the annotation
     * @return an unwrapped annotation parameter value
     */
    public static <T> T getAnnotationValue(AnnotationTarget target, DotName annotationName, String propertyName) {
        return getAnnotationValue(target, Arrays.asList(annotationName), propertyName);
    }

    public static <T> T getAnnotationValue(AnnotationTarget target, List<DotName> annotationNames, String propertyName) {
        return getAnnotationValue(target, annotationNames, propertyName, null);
    }

    /**
     * Convenience method to retrieve the named parameter from an annotation bound to the target.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param target the target object annotated with the annotation named by annotationName
     * @param annotationNames names of the annotations from which to retrieve the value
     * @param propertyName the name of the parameter/property in the annotation
     * @param defaultValue a default value to return if either the annotation or the value are missing
     * @return an unwrapped annotation parameter value
     */
    public static <T> T getAnnotationValue(AnnotationTarget target,
            List<DotName> annotationNames,
            String propertyName,
            T defaultValue) {

        AnnotationInstance annotation = getAnnotation(target, annotationNames);
        T value = null;

        if (annotation != null) {
            value = value(annotation, propertyName);
        }

        return value != null ? value : defaultValue;
    }

    /**
     * Convenience method to retrieve the named parameter from an annotation bound to the target.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param target the target object annotated with the annotation named by annotationName
     * @param annotationName name of the annotation from which to retrieve the value
     * @param propertyName the name of the parameter/property in the annotation
     * @param defaultValue a default value to return if either the annotation or the value are missing
     * @return an unwrapped annotation parameter value
     */
    public static <T> T getAnnotationValue(AnnotationTarget target,
            DotName annotationName,
            String propertyName,
            T defaultValue) {

        return getAnnotationValue(target, Arrays.asList(annotationName), propertyName, defaultValue);
    }

    public static Collection<AnnotationInstance> getAnnotations(AnnotationTarget type) {
        switch (type.kind()) {
            case CLASS:
                return type.asClass().classAnnotations();
            case FIELD:
                return type.asField().annotations();
            case METHOD:
                return type.asMethod().annotations();
            case METHOD_PARAMETER:
                MethodParameterInfo parameter = type.asMethodParameter();
                return parameter
                        .method()
                        .annotations()
                        .stream()
                        .filter(a -> a.target().kind() == Kind.METHOD_PARAMETER)
                        .filter(a -> a.target().asMethodParameter().position() == parameter.position())
                        .collect(Collectors.toList());
            case TYPE:
            case RECORD_COMPONENT:
                break;
        }
        return Collections.emptyList();
    }

    public static <T> T getDeclaredAnnotationValue(AnnotationTarget type, DotName annotationName, String propertyName) {
        AnnotationInstance annotation = getDeclaredAnnotation(type, annotationName);
        T value = null;

        if (annotation != null) {
            value = value(annotation, propertyName);
        }

        return value;
    }

    public static <T> T getDeclaredAnnotationValue(AnnotationTarget type, DotName annotationName) {
        return getDeclaredAnnotationValue(type, annotationName, OpenApiConstants.VALUE);
    }

    public static AnnotationInstance getDeclaredAnnotation(AnnotationTarget type, DotName annotationName) {
        Function<DotName, AnnotationInstance> lookup;

        switch (type.kind()) {
            case CLASS:
                lookup = type.asClass()::classAnnotation;
                break;
            case FIELD:
                lookup = type.asField()::annotation;
                break;
            case METHOD:
                lookup = name -> type.asMethod().annotations(name).stream().filter(a -> type.equals(a.target())).findFirst()
                        .orElse(null);
                break;
            case METHOD_PARAMETER:
                MethodParameterInfo parameter = type.asMethodParameter();
                lookup = name -> parameter
                        .method()
                        .annotations(name)
                        .stream()
                        .filter(a -> a.target().kind() == Kind.METHOD_PARAMETER)
                        .filter(a -> a.target().asMethodParameter().position() == parameter.position())
                        .findFirst()
                        .orElse(null);
                break;
            case RECORD_COMPONENT:
                lookup = type.asRecordComponent()::annotation;
                break;
            case TYPE:
            default:
                lookup = name -> null;
                break;
        }

        return lookup.apply(annotationName);
    }

    public static AnnotationInstance getAnnotation(Type type, DotName annotationName) {
        return type.annotations().stream()
                .filter(annotation -> annotation.name().equals(annotationName))
                .findFirst()
                .orElse(null);
    }

    public static AnnotationInstance getAnnotation(ClassInfo field, DotName annotationName) {
        return field.classAnnotations().stream()
                .filter(annotation -> annotation.name().equals(annotationName))
                .findFirst()
                .orElse(null);
    }

    public static AnnotationInstance getAnnotation(FieldInfo field, DotName annotationName) {
        return field.annotations().stream()
                .filter(annotation -> annotation.name().equals(annotationName))
                .findFirst()
                .orElse(null);
    }

}
