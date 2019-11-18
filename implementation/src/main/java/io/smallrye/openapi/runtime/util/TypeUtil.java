/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.runtime.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.WildcardType;

import io.smallrye.openapi.api.OpenApiConstants;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeUtil {

    private static final DotName DOTNAME_OBJECT = DotName.createSimple(Object.class.getName());
    private static final Type OBJECT_TYPE = Type.create(DOTNAME_OBJECT, Type.Kind.CLASS);
    private static final String UUID_PATTERN = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    private static final TypeWithFormat STRING_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.NONE);
    private static final TypeWithFormat BYTE_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.BYTE);
    private static final TypeWithFormat CHAR_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.BYTE);
    private static final TypeWithFormat UUID_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.UUID, UUID_PATTERN);
    private static final TypeWithFormat URI_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.URI);
    private static final TypeWithFormat NUMBER_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.NONE); // We can't immediately tell if it's int, float, etc.
    private static final TypeWithFormat BIGDECIMAL_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.NONE);
    private static final TypeWithFormat DOUBLE_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.DOUBLE);
    private static final TypeWithFormat FLOAT_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.FLOAT);
    private static final TypeWithFormat BIGINTEGER_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.NONE);
    private static final TypeWithFormat INTEGER_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.INT32);
    private static final TypeWithFormat LONG_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.INT64);
    private static final TypeWithFormat SHORT_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.NONE);
    private static final TypeWithFormat BOOLEAN_FORMAT = new TypeWithFormat(SchemaType.BOOLEAN, DataFormat.NONE);
    // SPECIAL FORMATS
    private static final TypeWithFormat ARRAY_FORMAT = new TypeWithFormat(SchemaType.ARRAY, DataFormat.NONE);
    private static final TypeWithFormat OBJECT_FORMAT = new TypeWithFormat(SchemaType.OBJECT, DataFormat.NONE);
    private static final TypeWithFormat DATE_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.DATE);
    private static final TypeWithFormat DATE_TIME_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.DATE_TIME);

    private static final Map<DotName, TypeWithFormat> TYPE_MAP = new LinkedHashMap<>();
    private static final IndexView jdkIndex;

    // https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#dataTypeFormat
    static {
        // String
        TYPE_MAP.put(DotName.createSimple(String.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(StringBuffer.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(StringBuilder.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(CharSequence.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.net.URI.class.getName()), URI_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.util.UUID.class.getName()), UUID_FORMAT);

        // B64 String
        TYPE_MAP.put(DotName.createSimple(Byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Character.class.getName()), CHAR_FORMAT);
        TYPE_MAP.put(DotName.createSimple(char.class.getName()), CHAR_FORMAT);

        // Number
        TYPE_MAP.put(DotName.createSimple(Number.class.getName()), NUMBER_FORMAT);

        // Decimal
        TYPE_MAP.put(DotName.createSimple(BigDecimal.class.getName()), BIGDECIMAL_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Double.class.getName()), DOUBLE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(double.class.getName()), DOUBLE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Float.class.getName()), FLOAT_FORMAT);
        TYPE_MAP.put(DotName.createSimple(float.class.getName()), FLOAT_FORMAT);

        // Integer
        TYPE_MAP.put(DotName.createSimple(BigInteger.class.getName()), BIGINTEGER_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Integer.class.getName()), INTEGER_FORMAT);
        TYPE_MAP.put(DotName.createSimple(int.class.getName()), INTEGER_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Long.class.getName()), LONG_FORMAT);
        TYPE_MAP.put(DotName.createSimple(long.class.getName()), LONG_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Short.class.getName()), SHORT_FORMAT);
        TYPE_MAP.put(DotName.createSimple(short.class.getName()), SHORT_FORMAT);

        // Boolean
        TYPE_MAP.put(DotName.createSimple(Boolean.class.getName()), BOOLEAN_FORMAT);
        TYPE_MAP.put(DotName.createSimple(boolean.class.getName()), BOOLEAN_FORMAT);

        // Date
        TYPE_MAP.put(DotName.createSimple(Date.class.getName()), DATE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.sql.Date.class.getName()), DATE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.LocalDate.class.getName()), DATE_FORMAT);

        // Date Time
        TYPE_MAP.put(DotName.createSimple(java.time.LocalDateTime.class.getName()), DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.ZonedDateTime.class.getName()), DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.OffsetDateTime.class.getName()), DATE_TIME_FORMAT);

        Indexer indexer = new Indexer();
        index(indexer, java.lang.Enum.class);
        index(indexer, java.lang.Object.class);

        // Common, expected classes
        index(indexer, java.lang.Boolean.class);
        index(indexer, java.lang.Byte.class);
        index(indexer, java.lang.Character.class);
        index(indexer, java.lang.Double.class);
        index(indexer, java.lang.Float.class);
        index(indexer, java.lang.Integer.class);
        index(indexer, java.lang.Long.class);
        index(indexer, java.lang.Number.class);
        index(indexer, java.lang.Short.class);
        index(indexer, java.lang.String.class);
        index(indexer, java.lang.Void.class);
        index(indexer, java.util.UUID.class);

        // Collection Interfaces
        index(indexer, java.util.Collection.class);
        index(indexer, java.util.Deque.class);
        index(indexer, java.util.List.class);
        index(indexer, java.util.Map.class);
        index(indexer, java.util.NavigableMap.class);
        index(indexer, java.util.NavigableSet.class);
        index(indexer, java.util.Queue.class);
        index(indexer, java.util.Set.class);
        index(indexer, java.util.SortedMap.class);
        index(indexer, java.util.SortedSet.class);
        index(indexer, java.util.concurrent.BlockingDeque.class);
        index(indexer, java.util.concurrent.BlockingQueue.class);
        index(indexer, java.util.concurrent.ConcurrentMap.class);
        index(indexer, java.util.concurrent.ConcurrentNavigableMap.class);
        index(indexer, java.util.concurrent.TransferQueue.class);

        // Abstract Collections
        index(indexer, java.util.AbstractCollection.class);
        index(indexer, java.util.AbstractList.class);
        index(indexer, java.util.AbstractMap.class);
        index(indexer, java.util.AbstractQueue.class);
        index(indexer, java.util.AbstractSequentialList.class);
        index(indexer, java.util.AbstractSet.class);
        index(indexer, java.util.EnumSet.class);

        // Collections
        index(indexer, java.util.ArrayDeque.class);
        index(indexer, java.util.ArrayList.class);
        index(indexer, java.util.EnumMap.class);
        index(indexer, java.util.HashMap.class);
        index(indexer, java.util.HashSet.class);
        index(indexer, java.util.Hashtable.class);
        index(indexer, java.util.IdentityHashMap.class);
        index(indexer, java.util.LinkedHashMap.class);
        index(indexer, java.util.LinkedHashSet.class);
        index(indexer, java.util.LinkedList.class);
        index(indexer, java.util.PriorityQueue.class);
        index(indexer, java.util.Properties.class);
        index(indexer, java.util.Stack.class);
        index(indexer, java.util.TreeMap.class);
        index(indexer, java.util.TreeSet.class);
        index(indexer, java.util.Vector.class);
        index(indexer, java.util.concurrent.ArrayBlockingQueue.class);
        index(indexer, java.util.concurrent.ConcurrentHashMap.class);
        index(indexer, java.util.concurrent.ConcurrentLinkedDeque.class);
        index(indexer, java.util.concurrent.ConcurrentLinkedQueue.class);
        index(indexer, java.util.concurrent.ConcurrentSkipListMap.class);
        index(indexer, java.util.concurrent.ConcurrentSkipListSet.class);
        index(indexer, java.util.concurrent.CopyOnWriteArrayList.class);
        index(indexer, java.util.concurrent.CopyOnWriteArraySet.class);
        index(indexer, java.util.concurrent.DelayQueue.class);
        index(indexer, java.util.concurrent.LinkedBlockingDeque.class);
        index(indexer, java.util.concurrent.LinkedBlockingQueue.class);
        index(indexer, java.util.concurrent.LinkedTransferQueue.class);
        index(indexer, java.util.concurrent.PriorityBlockingQueue.class);
        index(indexer, java.util.concurrent.SynchronousQueue.class);

        jdkIndex = indexer.complete();
    }

    private static void index(Indexer indexer, Class<?> klazz) {
        try (InputStream stream = klazz.getResourceAsStream(klazz.getSimpleName() + ".class")) {
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private TypeUtil() {
    }

    /**
     * Retrieves the default schema attributes for the given type, wrapped in
     * a TypeWithFormat instance.
     *
     * XXX: Consider additional check for subclasses of java.lang.Number and
     * implementations of java.lang.CharSequence.
     *
     * @param type the type
     * @return the default schema attributes for the given type, wrapped in
     *         a TypeWithFormat instance
     */
    private static TypeWithFormat getTypeFormat(Type type) {
        if (type.kind() == Type.Kind.ARRAY) {
            return arrayFormat();
        }

        return TYPE_MAP.getOrDefault(getName(type), objectFormat());
    }

    /**
     * Determines if a type is eligible for registration. If the schema type is
     * array or object, the type must be in the provided index. Otherwise, only
     * those types with defined properties beyond 'type' and 'format' are
     * eligible.
     * 
     * @param index index of classes to consider
     * @param classType the type to check
     * @return true if the type may be registered in the SchemaRegistry, false otherwise.
     */
    public static boolean allowRegistration(IndexView index, Type classType) {
        TypeWithFormat typeFormat = getTypeFormat(classType);

        switch (typeFormat.getSchemaType()) {
            case ARRAY:
            case OBJECT:
                return index.getClassByName(classType.name()) != null;
            default:
                return typeFormat.getProperties().size() > 2;
        }
    }

    /**
     * Retrieves the read-only Map of schema attributes for the given type.
     * 
     * @param classType the type
     * @return Map of default schema attributes
     */
    public static Map<String, Object> getTypeAttributes(Type classType) {
        return getTypeFormat(classType).getProperties();
    }

    /**
     * Sets the default schema attributes for the given type on the provided schema
     * instance.
     * 
     * @param classType the type
     * @param schema a writable schema to be updated with the type's default schema attributes
     */
    public static void applyTypeAttributes(Type classType, Schema schema) {
        TypeWithFormat attrs = getTypeFormat(classType);
        schema.setType(attrs.getSchemaType());
        schema.setFormat(attrs.getFormat());
        schema.setPattern(attrs.getPattern());
    }

    /**
     * Removes the known default schema attributes from the fieldSchema if they are also
     * present and have the same value in the typeSchema. This method reduces any duplicate
     * attributes between the two schemas when they are in an 'allOf' composition.
     * 
     * @param fieldSchema the schema for a field of the type described by typeSchema
     * @param typeSchema the schema for a class type
     */
    public static void clearMatchingDefaultAttributes(Schema fieldSchema, Schema typeSchema) {
        if (Objects.equals(fieldSchema.getType(), typeSchema.getType())) {
            fieldSchema.setType(null);
        }
        if (Objects.equals(fieldSchema.getFormat(), typeSchema.getFormat())) {
            fieldSchema.setFormat(null);
        }
        if (Objects.equals(fieldSchema.getPattern(), typeSchema.getPattern())) {
            fieldSchema.setPattern(null);
        }
    }

    public static TypeWithFormat arrayFormat() {
        return ARRAY_FORMAT;
    }

    public static TypeWithFormat objectFormat() {
        return OBJECT_FORMAT;
    }

    static Class<?> getClass(DotName name) throws ClassNotFoundException {
        return Class.forName(name.toString(), true, Thread.currentThread().getContextClassLoader());
    }

    static boolean isAssignableFrom(DotName subject, DotName object) {
        try {
            Class<?> subjectKlazz = TypeUtil.getClass(subject);
            Class<?> objectKlazz = TypeUtil.getClass(object);
            return objectKlazz.isAssignableFrom(subjectKlazz);
        } catch (@SuppressWarnings("unused") ClassNotFoundException nfe) {
            return false;
        }
    }

    static ClassInfo getClassInfo(IndexView appIndex, Type type) {
        DotName className = getName(type);
        ClassInfo clazz = appIndex.getClassByName(className);
        if (clazz == null) {
            clazz = jdkIndex.getClassByName(className);
        }
        return clazz;
    }

    /**
     * Test whether testSubject is an instanceof type test.
     * <p>
     * For example, test whether List is a Collection.
     * <p>
     * Attempts to work with both Jandex and using standard class.
     *
     * @param index Jandex index
     * @param testSubject type to test
     * @param testObject type to test against
     * @return true if is of type
     */
    public static boolean isA(IndexView index, Type testSubject, Type testObject) {
        // The types may be the same -- short circuit looking in the index
        if (getName(testSubject).equals(getName(testObject))) {
            return true;
        }
        if (testSubject.kind() == Type.Kind.PRIMITIVE && testObject.kind() != Type.Kind.PRIMITIVE) {
            return false;
        }

        // First, look in Jandex, as target might not be in our classloader
        ClassInfo subJandexKlazz = getClassInfo(index, testSubject);

        if (subJandexKlazz != null) {
            return subJandexKlazz.interfaceNames().contains(getName(testObject)) || hasSuper(index, subJandexKlazz, testObject);
        }

        return isAssignableFrom(testSubject.name(), testObject.name());
    }

    private static boolean hasSuper(IndexView index, ClassInfo testSubject, Type testObject) {
        Type superKlazzType = testSubject.superClassType();

        while (superKlazzType != null) {
            if (getName(superKlazzType).equals(getName(testObject))) {
                return true;
            }
            if (DOTNAME_OBJECT.equals(getName(superKlazzType))) {
                return false;
            }

            ClassInfo superKlazz = getClassInfo(index, superKlazzType);

            if (superKlazz != null) {
                if (superKlazz.interfaceNames().contains(getName(testObject))) {
                    return true;
                }
            } else {
                return isAssignableFrom(testSubject.name(), testObject.name());
            }

            superKlazzType = superKlazz.superClassType();
        }
        return false;
    }

    public static boolean isTerminalType(Type type) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE ||
                type.kind() == Type.Kind.WILDCARD_TYPE ||
                type.kind() == Type.Kind.ARRAY) {
            return false;
        }

        if (type.kind() == Type.Kind.PRIMITIVE ||
                type.kind() == Type.Kind.VOID) {
            return true;
        }

        TypeWithFormat tf = getTypeFormat(type);

        // If is known type.
        return tf.getSchemaType() != Schema.SchemaType.OBJECT &&
                tf.getSchemaType() != Schema.SchemaType.ARRAY;
    }

    /**
     * Determine if a given type is one of the following types:
     * 
     * <ul>
     * <li><code>java.util.Optional</code>
     * <li><code>java.util.OptionalDouble</code>
     * <li><code>java.util.OptionalInt</code>
     * <li><code>java.util.OptionalLong</code>
     * </ul>
     * 
     * @param type the type to check
     * @return true if the type is one of the four optional types, otherwise false
     */
    public static boolean isOptional(Type type) {
        return type != null && OpenApiConstants.DOTNAME_OPTIONALS.contains(type.name());
    }

    /**
     * Unwraps the type parameter (generic or primitive) from the given optional
     * type.
     * 
     * @param type the type to unwrap
     * @return the generic type argument for <code>java.util.Optional</code>, otherwise the optional primitive double, int, or
     *         long
     */
    public static Type getOptionalType(Type type) {
        if (type == null) {
            return null;
        }
        if (OpenApiConstants.DOTNAME_OPTIONAL.equals(type.name())) {
            return type.asParameterizedType().arguments().get(0);
        }
        if (OpenApiConstants.DOTNAME_OPTIONAL_DOUBLE.equals(type.name())) {
            return PrimitiveType.DOUBLE;
        }
        if (OpenApiConstants.DOTNAME_OPTIONAL_INT.equals(type.name())) {
            return PrimitiveType.INT;
        }
        if (OpenApiConstants.DOTNAME_OPTIONAL_LONG.equals(type.name())) {
            return PrimitiveType.LONG;
        }
        return null;
    }

    public static DotName getName(Type type) {
        if (type.kind() == Type.Kind.ARRAY) {
            return type.asArrayType().component().name();
        }
        if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            return getBound(type.asWildcardType()).name();
        }
        return type.name();
    }

    public static Type getBound(WildcardType wct) {
        if (wct.extendsBound() != null) {
            return wct.extendsBound();
        } else {
            return OBJECT_TYPE;
        }
    }

    public static Type resolveWildcard(WildcardType wildcardType) {
        return TypeUtil.getBound(wildcardType);
    }

    public static Type resolveWildcard(Type type) {
        if (type.kind() != Type.Kind.WILDCARD_TYPE) {
            return type;
        }
        return TypeUtil.getBound(type.asWildcardType());
    }

    public static boolean equalTypes(Type type1, Type type2) {
        if (type1.name().equals(type2.name())) {
            return true;
        }
        return equalWrappedTypes(type1, type2) || equalWrappedTypes(type2, type1);
    }

    public static boolean equalWrappedTypes(Type primitiveCandidate, Type wrappedCandidate) {
        return primitiveCandidate.kind().equals(Type.Kind.PRIMITIVE) &&
                wrappedCandidate.kind().equals(Type.Kind.CLASS) &&
                isPrimitiveWrapper(primitiveCandidate.asPrimitiveType(), wrappedCandidate);
    }

    public static boolean isPrimitiveWrapper(PrimitiveType primitive, Type wrapped) {
        Class<?> wrapperType;

        switch (primitive.primitive()) {
            case BOOLEAN:
                wrapperType = Boolean.class;
                break;
            case BYTE:
                wrapperType = Byte.class;
                break;
            case CHAR:
                wrapperType = Character.class;
                break;
            case DOUBLE:
                wrapperType = Double.class;
                break;
            case FLOAT:
                wrapperType = Float.class;
                break;
            case INT:
                wrapperType = Integer.class;
                break;
            case LONG:
                wrapperType = Long.class;
                break;
            case SHORT:
                wrapperType = Short.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown primitive: " + primitive);
        }

        return DotName.createSimple(wrapperType.getName()).equals(wrapped.name());
    }

    public static AnnotationInstance getSchemaAnnotation(AnnotationTarget annotationTarget) {
        return getAnnotation(annotationTarget, OpenApiConstants.DOTNAME_SCHEMA);
    }

    public static AnnotationInstance getSchemaAnnotation(ClassInfo field) {
        return getAnnotation(field, OpenApiConstants.DOTNAME_SCHEMA);
    }

    public static AnnotationInstance getSchemaAnnotation(FieldInfo field) {
        return getAnnotation(field, OpenApiConstants.DOTNAME_SCHEMA);
    }

    public static AnnotationInstance getSchemaAnnotation(Type type) {
        return getAnnotation(type, OpenApiConstants.DOTNAME_SCHEMA);
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
        return getAnnotationValue(target, annotationName, OpenApiConstants.PROP_VALUE, null);
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
        return getAnnotationValue(target, annotationName, propertyName, null);
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

        AnnotationInstance annotation = getAnnotation(target, annotationName);

        if (annotation != null) {
            return JandexUtil.value(annotation, propertyName);
        }

        return defaultValue;
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
                break;
        }
        return Collections.emptyList();
    }

    public static ClassInfo getDeclaringClass(AnnotationTarget type) {
        switch (type.kind()) {
            case FIELD:
                return type.asField().declaringClass();
            case METHOD:
                return type.asMethod().declaringClass();
            case METHOD_PARAMETER:
                MethodParameterInfo parameter = type.asMethodParameter();
                return parameter.method().declaringClass();
            case CLASS:
            case TYPE:
                break;
        }

        return null;
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

    static final class TypeWithFormat {
        private final Map<String, Object> properties;

        public TypeWithFormat(@NotNull SchemaType schemaType,
                @NotNull String format) {
            this(schemaType, format, null);
        }

        public TypeWithFormat(@NotNull SchemaType schemaType,
                @NotNull String format,
                String pattern) {

            Map<String, Object> props = new HashMap<>(3);
            props.put(OpenApiConstants.PROP_TYPE, schemaType);
            props.put(OpenApiConstants.PROP_FORMAT, format);
            if (pattern != null) {
                props.put(OpenApiConstants.PROP_PATTERN, pattern);
            }

            this.properties = Collections.unmodifiableMap(props);
        }

        SchemaType getSchemaType() {
            return (SchemaType) properties.get(OpenApiConstants.PROP_TYPE);
        }

        String getFormat() {
            return (String) properties.get(OpenApiConstants.PROP_FORMAT);
        }

        String getPattern() {
            return (String) properties.get(OpenApiConstants.PROP_PATTERN);
        }

        Map<String, Object> getProperties() {
            return properties;
        }
    }

    private static class DataFormat {
        static final String NONE = null;
        static final String INT32 = "int32";
        static final String INT64 = "int64";
        static final String FLOAT = "float";
        static final String DOUBLE = "double";
        static final String BYTE = "byte";
        static final String DATE = "date";
        static final String DATE_TIME = "date-time";
        static final String URI = "uri";
        static final String UUID = "uuid";
    }

}
