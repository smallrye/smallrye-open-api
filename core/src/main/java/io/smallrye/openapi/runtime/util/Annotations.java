package io.smallrye.openapi.runtime.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.PrimitiveType.Primitive;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public final class Annotations {

    /**
     * Default annotation property name
     */
    private static final String VALUE = "value";
    private static final Map<PrimitiveType.Primitive, AnnotationValue.Kind> PRIMITIVES;
    private static final DotName CLASS_NAME = DotName.createSimple("java.lang.Class");
    private static final Type ENUM_TYPE = Type.create(DotName.ENUM_NAME, Type.Kind.CLASS);
    private static final Type ANNOTATION_TYPE = Type.create(DotName.createSimple("java.lang.annotation.Annotation"),
            Type.Kind.CLASS);

    static {
        PRIMITIVES = new EnumMap<>(PrimitiveType.Primitive.class);
        PRIMITIVES.put(Primitive.BOOLEAN, AnnotationValue.Kind.BOOLEAN);
        PRIMITIVES.put(Primitive.BYTE, AnnotationValue.Kind.BYTE);
        PRIMITIVES.put(Primitive.CHAR, AnnotationValue.Kind.CHARACTER);
        PRIMITIVES.put(Primitive.DOUBLE, AnnotationValue.Kind.DOUBLE);
        PRIMITIVES.put(Primitive.FLOAT, AnnotationValue.Kind.FLOAT);
        PRIMITIVES.put(Primitive.INT, AnnotationValue.Kind.INTEGER);
        PRIMITIVES.put(Primitive.LONG, AnnotationValue.Kind.LONG);
        PRIMITIVES.put(Primitive.SHORT, AnnotationValue.Kind.SHORT);
    }

    private final AnnotationScannerContext context;
    private final Set<String> excludedPackages;

    public Annotations(AnnotationScannerContext context) {
        this.context = context;
        this.excludedPackages = context.getConfig()
                .getScanCompositionExcludePackages()
                .stream()
                .map(pkg -> pkg.concat("."))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("deprecation")
    private static List<AnnotationInstance> declaredAnnotations(ClassInfo target) {
        return new ArrayList<>(target.classAnnotations());
    }

    private static List<AnnotationInstance> filter(AnnotationTarget target, List<AnnotationInstance> annotations) {
        return annotations.stream()
                .filter(a -> target.equals(a.target()))
                .collect(Collectors.toList());
    }

    private static List<AnnotationInstance> getDeclaredAnnotations(AnnotationTarget target) {
        switch (target.kind()) {
            case CLASS:
                return declaredAnnotations(target.asClass());

            case FIELD:
                return filter(target, target.asField().annotations());

            case METHOD:
                return filter(target, target.asMethod().annotations());

            case METHOD_PARAMETER:
                return filter(target, target.asMethodParameter().method().annotations());

            case RECORD_COMPONENT:
                return filter(target, target.asRecordComponent().annotations());

            case TYPE:
                return filter(target, Optional.ofNullable(target.asType().target())
                        .map(Type::annotations)
                        .orElseGet(Collections::emptyList));

            default:
                return Collections.emptyList();
        }
    }

    private boolean composable(DotName annotation) {
        String name = annotation.toString();

        for (String pkg : this.excludedPackages) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }

        return true;
    }

    private Stream<AnnotationInstance> getComposedAnnotation(Collection<AnnotationInstance> declaredAnnotations,
            DotName name,
            Set<DotName> scanned) {

        return declaredAnnotations
                .stream()
                .filter(AnnotationInstance::runtimeVisible)
                .map(AnnotationInstance::name)
                .filter(this::composable)
                .map(context.getAugmentedIndex()::getClassByName)
                .filter(Objects::nonNull)
                .flatMap(annotationClass -> {
                    if (scanned.contains(annotationClass.name())) {
                        return null;
                    }

                    scanned.add(annotationClass.name());
                    UtilLogging.logger.composedAnnotationSearch(name, annotationClass.name());
                    return getDeclaredAnnotation(annotationClass, name, scanned);
                })
                .filter(Objects::nonNull);
    }

    private Stream<AnnotationInstance> getDeclaredAnnotation(AnnotationTarget target, DotName name, Set<DotName> scanned) {
        if (target == null) {
            return Stream.empty();
        }

        List<AnnotationInstance> declaredAnnotations = getDeclaredAnnotations(target);

        if (declaredAnnotations.isEmpty()) {
            return Stream.empty();
        }

        Stream<AnnotationInstance> direct = declaredAnnotations.stream().filter(a -> name.equals(a.name()));
        Stream<AnnotationInstance> composed = getComposedAnnotation(declaredAnnotations, name, scanned);

        return Stream.concat(direct, composed);
    }

    private Stream<AnnotationInstance> getDeclaredAnnotation(AnnotationTarget target, DotName name) {
        return getDeclaredAnnotation(target, name, new HashSet<>());
    }

    public <T> T value(AnnotationInstance annotation) {
        return annotation != null ? value(annotation, VALUE) : null;
    }

    private AnnotationValue.Kind valueKind(AnnotationInstance annotation, AnnotationValue value) {
        final boolean isArray = (AnnotationValue.Kind.ARRAY == value.kind());
        AnnotationValue.Kind kind = (isArray ? value.componentKind() : value.kind());
        AugmentedIndexView index = context.getAugmentedIndex();
        ClassInfo annoClass = index.getClassByName(annotation.name());

        if (kind == AnnotationValue.Kind.UNKNOWN && annoClass != null) {
            MethodInfo valueMethod = annoClass.method(value.name());
            Type valueType = valueMethod.returnType().asArrayType().constituent();

            switch (valueType.kind()) {
                case PRIMITIVE:
                    return PRIMITIVES.get(valueType.asPrimitiveType().primitive());

                case CLASS:
                case PARAMETERIZED_TYPE:
                    if (valueType.name().equals(DotName.STRING_NAME)) {
                        return AnnotationValue.Kind.STRING;
                    } else if (valueType.name().equals(CLASS_NAME)) {
                        return AnnotationValue.Kind.CLASS;
                    } else if (TypeUtil.isA(context, valueType, ENUM_TYPE)) {
                        return AnnotationValue.Kind.ENUM;
                    } else if (TypeUtil.isA(context, valueType, ANNOTATION_TYPE)) {
                        return AnnotationValue.Kind.NESTED;
                    }
                    break;
                default:
                    break;
            }
        }

        return kind;
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
    public <T> T value(AnnotationInstance annotation, String name) {
        final AnnotationValue value = annotation.value(name);

        if (value == null) {
            return null;
        }

        return value(annotation, value);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T value(AnnotationInstance annotation, AnnotationValue value) {
        final AnnotationValue.Kind valueKind = valueKind(annotation, value);
        final boolean isArray = (AnnotationValue.Kind.ARRAY == value.kind());

        switch (valueKind) {
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
    public <T> T value(AnnotationInstance annotation, String name, T defaultValue) {
        T value = value(annotation, name);
        return value != null ? value : defaultValue;
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
    public <T extends Enum<T>> T enumValue(AnnotationInstance annotation, String propertyName, Class<T> clazz) {
        return enumValue(clazz, annotation != null ? (String) value(annotation, propertyName) : null);
    }

    public <T extends Enum<T>> T enumValue(Class<T> clazz, AnnotationValue value) {
        return enumValue(clazz, value != null ? value.asEnum() : null);
    }

    public <T extends Enum<T>> T enumValue(Class<T> clazz, String value) {
        if (value == null) {
            return null;
        }

        return Stream.of(clazz.getEnumConstants())
                .filter(c -> c.name().equals(value))
                .findFirst()
                .orElse(null);
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
    public List<AnnotationInstance> getRepeatableAnnotation(AnnotationTarget target,
            DotName singleAnnotationName,
            DotName repeatableAnnotationName) {

        Stream<AnnotationInstance> single = getDeclaredAnnotation(target, singleAnnotationName);
        Stream<AnnotationInstance> wrapped = getDeclaredAnnotation(target, repeatableAnnotationName)
                .map(a -> this.<AnnotationInstance[]> value(a, VALUE))
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .map(a -> AnnotationInstance.create(a.name(), target, a.values()));

        return Stream.concat(single, wrapped).collect(Collectors.toList());
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
    public AnnotationInstance getMethodParameterAnnotation(MethodInfo method, int parameterIndex,
            DotName annotationName) {
        return getDeclaredAnnotation(MethodParameterInfo.create(method, (short) parameterIndex), annotationName)
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
    public AnnotationInstance getMethodParameterAnnotation(MethodInfo method, Type parameterType,
            DotName annotationName) {
        // parameterType must be the same object as in the method's parameter type array
        int parameterIndex = method.parameterTypes().indexOf(parameterType);
        return getMethodParameterAnnotation(method, parameterIndex, annotationName);
    }

    /**
     * Returns all annotations configured for a single parameter of a method.
     *
     * @param method MethodInfo
     * @param parameterIndex parameter position
     * @return List of AnnotationInstance's
     */
    public List<AnnotationInstance> getMethodParameterAnnotations(MethodInfo method, int parameterIndex) {
        return getDeclaredAnnotations(MethodParameterInfo.create(method, (short) parameterIndex));
    }

    /**
     * Finds all annotations on a particular parameter of a method based on the identity of the parameterType.
     *
     * @param method the method
     * @param parameterType the parameter type
     * @return the list of annotations, never null
     */
    public List<AnnotationInstance> getMethodParameterAnnotations(MethodInfo method, Type parameterType) {
        // parameterType must be the same object as in the method's parameter type array
        int parameterIndex = method.parameterTypes().indexOf(parameterType);
        return getMethodParameterAnnotations(method, parameterIndex);
    }

    public boolean hasAnnotation(AnnotationTarget target, Collection<DotName> annotationNames) {
        return Objects.nonNull(getAnnotation(target, annotationNames));
    }

    public boolean hasAnnotation(AnnotationTarget target, DotName... annotationNames) {
        return Objects.nonNull(getAnnotation(target, annotationNames));
    }

    public AnnotationInstance getAnnotation(AnnotationTarget annotationTarget, DotName... annotationName) {
        return getAnnotation(annotationTarget, Arrays.asList(annotationName));
    }

    public AnnotationInstance getAnnotation(AnnotationTarget annotationTarget, Collection<DotName> annotationNames) {
        return annotationNames.stream()
                .flatMap(annotationName -> getDeclaredAnnotation(annotationTarget, annotationName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Convenience method to retrieve the "value" parameter from an annotation bound to the target.
     * The value will be unwrapped from its containing {@link AnnotationValue}.
     *
     * @param <T> the type of the parameter being retrieved
     * @param target the target object annotated with the annotation named by annotationName
     * @param annotationNames names of annotations from which to retrieve the value
     * @return an unwrapped annotation parameter value
     */
    public <T> T getAnnotationValue(AnnotationTarget target, DotName... annotationNames) {
        return getAnnotationValue(target, Arrays.asList(annotationNames), VALUE, null);
    }

    public <T> T getAnnotationValue(AnnotationTarget target, List<DotName> annotationNames) {
        return getAnnotationValue(target, annotationNames, VALUE, null);
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
    public <T> T getAnnotationValue(AnnotationTarget target, DotName annotationName, String propertyName) {
        return getAnnotationValue(target, Arrays.asList(annotationName), propertyName);
    }

    public <T> T getAnnotationValue(AnnotationTarget target, List<DotName> annotationNames, String propertyName) {
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
    public <T> T getAnnotationValue(AnnotationTarget target,
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

}
