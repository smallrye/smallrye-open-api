package io.smallrye.openapi.runtime.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.WildcardType;

import io.smallrye.openapi.api.constants.JDKConstants;
import io.smallrye.openapi.api.constants.JaxbConstants;
import io.smallrye.openapi.api.constants.KotlinConstants;
import io.smallrye.openapi.api.constants.MutinyConstants;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeUtil {

    private static final DotName DOTNAME_OBJECT = DotName.createSimple(Object.class.getName());
    private static final Type OBJECT_TYPE = Type.create(DOTNAME_OBJECT, Type.Kind.CLASS);

    private static final DotName DOTNAME_VOID = DotName.createSimple(Void.class.getName());

    private static final String UUID_PATTERN = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    private static final TypeWithFormat ANY = TypeWithFormat.anyType().build();
    private static final TypeWithFormat STRING_FORMAT = TypeWithFormat.of(SchemaType.STRING).build();
    private static final TypeWithFormat BINARY_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.BINARY).build();
    private static final TypeWithFormat BYTE_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.BYTE).build();
    private static final TypeWithFormat CHAR_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.BYTE).build();
    private static final TypeWithFormat UUID_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.UUID)
            .pattern(UUID_PATTERN).build();
    private static final TypeWithFormat URI_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.URI).build();
    private static final TypeWithFormat NUMBER_FORMAT = TypeWithFormat.of(SchemaType.NUMBER).build(); // We can't immediately tell if it's int, float, etc.
    private static final TypeWithFormat BIGDECIMAL_FORMAT = TypeWithFormat.of(SchemaType.NUMBER).build();
    private static final TypeWithFormat DOUBLE_FORMAT = TypeWithFormat.of(SchemaType.NUMBER).format(DataFormat.DOUBLE).build();
    private static final TypeWithFormat FLOAT_FORMAT = TypeWithFormat.of(SchemaType.NUMBER).format(DataFormat.FLOAT).build();
    private static final TypeWithFormat BIGINTEGER_FORMAT = TypeWithFormat.of(SchemaType.INTEGER).build();
    private static final TypeWithFormat INTEGER_FORMAT = TypeWithFormat.of(SchemaType.INTEGER).format(DataFormat.INT32).build();
    private static final TypeWithFormat LONG_FORMAT = TypeWithFormat.of(SchemaType.INTEGER).format(DataFormat.INT64).build();
    private static final TypeWithFormat SHORT_FORMAT = TypeWithFormat.of(SchemaType.INTEGER).build();
    private static final TypeWithFormat BOOLEAN_FORMAT = TypeWithFormat.of(SchemaType.BOOLEAN).build();
    // SPECIAL FORMATS
    private static final TypeWithFormat ARRAY_FORMAT = TypeWithFormat.of(SchemaType.ARRAY).build();
    private static final TypeWithFormat OBJECT_FORMAT = TypeWithFormat.of(SchemaType.OBJECT).build();

    private static final TypeWithFormat ARRAY_OPAQUE_FORMAT = TypeWithFormat.of(SchemaType.ARRAY).opaque().build();
    private static final TypeWithFormat OBJECT_OPAQUE_FORMAT = TypeWithFormat.of(SchemaType.OBJECT).opaque().build();

    private static final TypeWithFormat DATE_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.DATE)
            .example("2022-03-10").build();
    private static final TypeWithFormat LOCAL_DATE_TIME_FORMAT = TypeWithFormat.of(SchemaType.STRING)
            .format(DataFormat.DATE_TIME)
            .example("2022-03-10T12:15:50").build();
    private static final TypeWithFormat OFFSET_DATE_TIME_FORMAT = TypeWithFormat.of(SchemaType.STRING)
            .format(DataFormat.DATE_TIME)
            .example("2022-03-10T12:15:50-04:00").build();
    private static final TypeWithFormat INSTANT_DATE_TIME_FORMAT = TypeWithFormat.of(SchemaType.STRING)
            .format(DataFormat.DATE_TIME)
            .example("2022-03-10T16:15:50Z").build();
    /*
     * This is used both for Java Period and Duration according to
     * https://docs.oracle.com/javase/8/docs/api/java/time/Period.html
     * As they're both basically https://www.rfc-editor.org/rfc/rfc3339.html#section-5.6
     * Examples is parsed from both classes
     */
    private static final TypeWithFormat DURATION_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.DURATION)
            .example("P1D")
            .build();
    private static final TypeWithFormat TIME_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.TIME)
            .externalDocumentation("As defined by 'full-time' in RFC3339",
                    "https://www.rfc-editor.org/rfc/rfc3339.html#section-5.6")
            .example("13:45:30.123456789+02:00").build();
    private static final TypeWithFormat TIME_LOCAL_FORMAT = TypeWithFormat.of(SchemaType.STRING).format(DataFormat.TIME_LOCAL)
            .externalDocumentation("As defined by 'partial-time' in RFC3339",
                    "https://www.rfc-editor.org/rfc/rfc3339.html#section-5.6")
            .example("13:45:30.123456789").build();

    private static final Map<DotName, TypeWithFormat> TYPE_MAP = new LinkedHashMap<>();
    public static final IndexView jdkIndex;
    private static final Set<DotName> wrapperTypes = new HashSet<>();

    // https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#dataTypeFormat
    static {
        TYPE_MAP.put(DOTNAME_OBJECT, ANY);

        // String
        TYPE_MAP.put(DotName.createSimple(String.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(StringBuffer.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(StringBuilder.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(CharSequence.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.net.URI.class.getName()), URI_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.net.URL.class.getName()), STRING_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.util.UUID.class.getName()), UUID_FORMAT);

        // B64 String
        TYPE_MAP.put(DotName.createSimple(Byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Character.class.getName()), CHAR_FORMAT);
        TYPE_MAP.put(DotName.createSimple(char.class.getName()), CHAR_FORMAT);

        // Binary (any sequence of octets)
        TYPE_MAP.put(DotName.createSimple(byte[].class.getName()), BINARY_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.io.File.class.getName()), BINARY_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.io.InputStream.class.getName()), BINARY_FORMAT);
        TYPE_MAP.put(DotName.createSimple("org.jboss.resteasy.reactive.multipart.FileUpload"), BINARY_FORMAT);

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
        TYPE_MAP.put(DotName.createSimple(java.time.LocalDateTime.class.getName()), LOCAL_DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.ZonedDateTime.class.getName()), OFFSET_DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.OffsetDateTime.class.getName()), OFFSET_DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.Instant.class.getName()), INSTANT_DATE_TIME_FORMAT);

        // Duration
        TYPE_MAP.put(DotName.createSimple(java.time.Duration.class.getName()), DURATION_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.Period.class.getName()), DURATION_FORMAT);

        // Time
        TYPE_MAP.put(DotName.createSimple(java.time.LocalTime.class.getName()), TIME_LOCAL_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.OffsetTime.class.getName()), TIME_FORMAT);

        for (String qualifier : Arrays.asList("jakarta.", "javax.")) {
            TYPE_MAP.put(DotName.createSimple(qualifier + "json.JsonArray"), ARRAY_OPAQUE_FORMAT);
            TYPE_MAP.put(DotName.createSimple(qualifier + "json.JsonNumber"), NUMBER_FORMAT);
            TYPE_MAP.put(DotName.createSimple(qualifier + "json.JsonObject"), OBJECT_OPAQUE_FORMAT);
            TYPE_MAP.put(DotName.createSimple(qualifier + "json.JsonString"), STRING_FORMAT);
        }

        Indexer indexer = new Indexer();
        index(indexer, java.lang.Enum.class);
        index(indexer, java.lang.Object.class);

        // Interfaces commonly implemented by core JDK classes
        index(indexer, java.lang.AutoCloseable.class);
        index(indexer, java.lang.Cloneable.class);
        index(indexer, java.lang.Comparable.class);
        index(indexer, java.io.Serializable.class);
        index(indexer, java.util.RandomAccess.class);

        // Common, expected classes
        index(indexer, java.lang.Boolean.class);
        index(indexer, java.lang.Byte.class);
        index(indexer, java.lang.Character.class);
        index(indexer, java.lang.CharSequence.class);
        index(indexer, java.lang.Double.class);
        index(indexer, java.lang.Float.class);
        index(indexer, java.lang.Integer.class);
        index(indexer, java.lang.Long.class);
        index(indexer, java.lang.Number.class);
        index(indexer, java.lang.Short.class);
        index(indexer, java.lang.String.class);
        index(indexer, java.lang.Void.class);
        index(indexer, java.util.Date.class);
        index(indexer, java.util.UUID.class);

        // Java Time APIs
        index(indexer, java.time.Duration.class);
        index(indexer, java.time.Instant.class);
        index(indexer, java.time.LocalDate.class);
        index(indexer, java.time.LocalDateTime.class);
        index(indexer, java.time.LocalTime.class);
        index(indexer, java.time.OffsetDateTime.class);
        index(indexer, java.time.OffsetTime.class);
        index(indexer, java.time.Period.class);
        index(indexer, java.time.ZonedDateTime.class);
        index(indexer, java.time.chrono.ChronoLocalDate.class);
        index(indexer, java.time.chrono.ChronoLocalDateTime.class);
        index(indexer, java.time.chrono.ChronoPeriod.class);
        index(indexer, java.time.chrono.ChronoZonedDateTime.class);
        index(indexer, java.time.temporal.Temporal.class);
        index(indexer, java.time.temporal.TemporalAccessor.class);
        index(indexer, java.time.temporal.TemporalAdjuster.class);
        index(indexer, java.time.temporal.TemporalAmount.class);

        // Collection Interfaces
        index(indexer, java.lang.Iterable.class);
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
        index(indexer, java.util.Dictionary.class);
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

        // Streams
        index(indexer, java.util.stream.BaseStream.class);
        index(indexer, java.util.stream.Stream.class);
        index(indexer, java.util.stream.IntStream.class);
        index(indexer, java.util.stream.LongStream.class);
        index(indexer, java.util.stream.DoubleStream.class);

        // CompletionStage and implementation
        index(indexer, java.util.concurrent.CompletionStage.class);
        index(indexer, java.util.concurrent.CompletableFuture.class);
        index(indexer, java.util.concurrent.Future.class);

        // Index classes that may not be present in older Java versions
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Stream.of("java.lang.constant.Constable", "java.lang.constant.ConstantDesc")
                .forEach(className -> indexOptional(indexer, className, contextLoader));

        jdkIndex = indexer.complete();

        wrapperTypes.addAll(JaxbConstants.JAXB_ELEMENT);
        wrapperTypes.add(MutinyConstants.UNI_TYPE.name());
        wrapperTypes.add(JDKConstants.COMPLETION_STAGE_NAME);
        wrapperTypes.add(JDKConstants.COMPLETABLE_FUTURE_NAME);
        wrapperTypes.add(DotName.createSimple("io.reactivex.Single"));
    }

    private static void indexOptional(Indexer indexer, String className, ClassLoader contextLoader) {
        try {
            index(indexer, Class.forName(className, false, contextLoader));
        } catch (Exception e) {
            // Ignore anything
        }
    }

    private static void index(Indexer indexer, Class<?> klazz) {
        try {
            indexer.indexClass(klazz);
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
            return TYPE_MAP.getOrDefault(type.name(), arrayFormat());
        }

        return TYPE_MAP.getOrDefault(getName(type), objectFormat());
    }

    /**
     * Determines if a type is eligible for registration. If the schema type is
     * array or object, the type must be in the provided index. Otherwise, only
     * those types with defined properties beyond 'type' and 'format' are
     * eligible.
     *
     * @param context scanning context
     * @param classType the type to check
     * @return true if the type may be registered in the SchemaRegistry, false otherwise.
     */
    public static boolean allowRegistration(final AnnotationScannerContext context, Type classType) {
        TypeWithFormat typeFormat = getTypeFormat(classType);

        if (typeFormat.isSchemaType(SchemaType.ARRAY, SchemaType.OBJECT)) {
            return !typeFormat.isOpaque()
                    && !knownJavaType(classType.name())
                    && context.getIndex().getClassByName(classType.name()) != null;
        }

        return typeFormat.getProperties().size() > 2;
    }

    public static boolean knownJavaType(DotName name) {
        return jdkIndex.getClassByName(name) != null;
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
     * Check if the default schema type that applies to the provided classType
     * differs from any value specified by the user via schemaAnnotation.
     *
     * @param classType class type to find a default schema type
     * @param schemaAnnotation schema annotation (possibly null) which may have an overridden type value
     * @return true if the annotation has a type specified that is different from the default type for classType, otherwise
     *         false
     */
    public static boolean isTypeOverridden(AnnotationScannerContext context, Type classType,
            AnnotationInstance schemaAnnotation) {
        SchemaType providedType = context.annotations().enumValue(schemaAnnotation, SchemaConstant.PROP_TYPE, SchemaType.class);
        TypeWithFormat typeFormat = getTypeFormat(classType);
        return providedType != null && !typeFormat.isSchemaType(providedType);
    }

    /**
     * Sets the default schema attributes for the given type on the provided schema
     * instance.
     *
     * @param classType the type
     * @param schema a writable schema to be updated with the type's default schema attributes
     */
    public static void applyTypeAttributes(Type classType, Schema schema) {
        Map<String, Object> properties = getTypeAttributes(classType);

        SchemaSupport.setType(schema, (SchemaType) properties.get(SchemaConstant.PROP_TYPE));
        schema.setFormat((String) properties.get(SchemaConstant.PROP_FORMAT));
        schema.setPattern((String) properties.get(SchemaConstant.PROP_PATTERN));
        schema.setExamples(wrapInList(properties.get(SchemaConstant.PROP_EXAMPLE)));
        schema.setExternalDocs((ExternalDocumentation) properties.get(SchemaConstant.PROP_EXTERNAL_DOCS));
    }

    private static <E> List<E> wrapInList(E item) {
        if (item == null) {
            return null;
        } else {
            return Collections.singletonList(item);
        }
    }

    /**
     * Removes the known default schema attributes from the fieldSchema if they are also
     * present and have the same value in the typeSchema. This method reduces any duplicate
     * attributes between the two schemas when they are in an 'allOf' composition.
     *
     * @param fieldSchema the schema for a field of the type described by typeSchema
     * @param typeSchema the schema for a class type
     */
    @SuppressWarnings("deprecation")
    public static void clearMatchingDefaultAttributes(Schema fieldSchema, Schema typeSchema) {
        clearIfEqual(fieldSchema.getFormat(), typeSchema.getFormat(), fieldSchema::setFormat);
        clearIfEqual(fieldSchema.getPattern(), typeSchema.getPattern(), fieldSchema::setPattern);
        clearIfEqual(fieldSchema.getExamples(), typeSchema.getExamples(), fieldSchema::setExamples);
        clearIfEqual(fieldSchema.getExample(), typeSchema.getExample(), fieldSchema::setExample);
        clearIfEqual(fieldSchema.getExternalDocs(), typeSchema.getExternalDocs(), fieldSchema::setExternalDocs);
    }

    static <T> void clearIfEqual(T fieldSchemaVal, T typeSchemaVal, Consumer<T> setter) {
        if (Objects.equals(fieldSchemaVal, typeSchemaVal)) {
            setter.accept(null);
        }
    }

    private static TypeWithFormat arrayFormat() {
        return ARRAY_FORMAT;
    }

    private static TypeWithFormat objectFormat() {
        return OBJECT_FORMAT;
    }

    private static Class<?> getClass(DotName name, ClassLoader cl) throws ClassNotFoundException {
        return Class.forName(name.toString(), false, cl);
    }

    private static boolean isAssignableFrom(DotName subject, DotName object, ClassLoader cl) {
        try {
            Class<?> subjectKlazz = TypeUtil.getClass(subject, cl);
            Class<?> objectKlazz = TypeUtil.getClass(object, cl);
            UtilLogging.logger.reflectionInstanceOf(subjectKlazz, objectKlazz);
            return objectKlazz.isAssignableFrom(subjectKlazz);
        } catch (@SuppressWarnings("unused") ClassNotFoundException | LinkageError nfe) {
            return false;
        }
    }

    static ClassInfo getClassInfo(IndexView appIndex, DotName className) {
        ClassInfo clazz = appIndex.getClassByName(className);
        if (clazz == null) {
            clazz = jdkIndex.getClassByName(className);
        }
        return clazz;
    }

    /**
     * Test whether testSubject is an "instanceof" type testObject.
     * <p>
     * For example, test whether List is a Collection.
     * <p>
     * Attempts to work with both Jandex and using standard class.
     *
     * @param context scanning context
     * @param testSubject type to test
     * @param testObject type to test against
     * @return true if is of type
     */
    public static boolean isA(final AnnotationScannerContext context, Type testSubject, Type testObject) {
        IndexView index = context.getIndex();
        DotName testSubjectName = getName(testSubject);
        DotName testObjectName = getName(testObject);

        // The types may be the same -- short circuit looking in the index
        if (testSubjectName.equals(testObjectName)) {
            return true;
        }

        if (testSubject.kind() == Type.Kind.PRIMITIVE && testObject.kind() != Type.Kind.PRIMITIVE) {
            return false;
        }

        // First, look in Jandex, as target might not be in our classloader
        ClassInfo subJandexKlazz = getClassInfo(index, testSubjectName);

        if (subJandexKlazz != null) {
            Set<DotName> superTypes = new HashSet<>();
            AtomicBoolean indexComplete = new AtomicBoolean(true);
            loadSuperTypes(index, subJandexKlazz, superTypes, indexComplete);

            if (superTypes.contains(testObjectName)) {
                return true;
            }

            if (indexComplete.get()) {
                /*
                 * When indexComplete remains `true` during the search for super types of the
                 * `testSubject`, we can be sure that the testSubject is not an instance of the
                 * `testObject`. Otherwise, we must default to class-loading/reflection, below.
                 */
                return false;
            }
        }

        return isAssignableFrom(testSubject.name(), testObject.name(), context.getClassLoader());
    }

    private static void loadSuperTypes(IndexView index, ClassInfo testSubject, Set<DotName> superTypes,
            AtomicBoolean indexComplete) {
        testSubject.interfaceNames()
                .forEach(iface -> loadSuperType(index, iface, superTypes, indexComplete));

        DotName superName = testSubject.superName();

        if (superName != null) {
            loadSuperType(index, superName, superTypes, indexComplete);
        }
    }

    private static void loadSuperType(IndexView index, DotName superName, Set<DotName> superTypes,
            AtomicBoolean indexComplete) {
        superTypes.add(superName);

        ClassInfo superKlazz = getClassInfo(index, superName);

        if (superKlazz != null) {
            loadSuperTypes(index, superKlazz, superTypes, indexComplete);
        } else {
            indexComplete.set(false);
        }
    }

    public static boolean isTerminalType(Type type) {
        boolean terminal;

        switch (type.kind()) {
            case PRIMITIVE:
            case VOID:
                terminal = true;
                break;
            case TYPE_VARIABLE:
                terminal = false;
                break;
            case WILDCARD_TYPE:
                terminal = isTerminalType(resolveWildcard(type));
                break;
            case CLASS:
                if (DOTNAME_OBJECT.equals(type.name())) {
                    terminal = true;
                    break;
                }
                // Fall through
            default:
                // If is known type.
                terminal = !getTypeFormat(type).isSchemaType(SchemaType.ARRAY, SchemaType.OBJECT);
                break;
        }

        return terminal;
    }

    public static boolean isWrappedType(Type type) {
        if (type != null) {
            return isOptional(type) || wrapperTypes.contains(type.name());
        }
        return false;
    }

    public static Type unwrapType(Type type) {
        if (type != null) {
            if (isOptional(type)) {
                return getOptionalType(type);
            }
            if (wrapperTypes.contains(type.name())) {
                return type.asParameterizedType().arguments().get(0);
            }
        }

        return type;
    }

    public static boolean isVoid(Type type) {
        if (type == null) {
            return false;
        }

        switch (type.kind()) {
            case VOID:
                return true;
            case CLASS:
                return DOTNAME_VOID.equals(type.name());
            default:
                return false;
        }
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
        return type != null && JDKConstants.DOTNAME_OPTIONALS.contains(type.name());
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
        if (JDKConstants.DOTNAME_OPTIONAL.equals(type.name())) {
            return type.asParameterizedType().arguments().get(0);
        }
        if (JDKConstants.DOTNAME_OPTIONAL_DOUBLE.equals(type.name())) {
            return PrimitiveType.DOUBLE;
        }
        if (JDKConstants.DOTNAME_OPTIONAL_INT.equals(type.name())) {
            return PrimitiveType.INT;
        }
        if (JDKConstants.DOTNAME_OPTIONAL_LONG.equals(type.name())) {
            return PrimitiveType.LONG;
        }
        return null;
    }

    public static DotName getName(Type type) {
        if (type.kind() == Type.Kind.ARRAY) {
            return type.asArrayType().constituent().name();
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
                throw UtilMessages.msg.unknownPrimitive(primitive);
        }

        return DotName.createSimple(wrapperType.getName()).equals(wrapped.name());
    }

    public static AnnotationInstance getSchemaAnnotation(AnnotationScannerContext context, AnnotationTarget annotationTarget) {
        return context.annotations().getAnnotation(annotationTarget, SchemaConstant.DOTNAME_SCHEMA);
    }

    public static boolean isIncludedAllOf(AnnotationScannerContext context, ClassInfo annotatedClass, Type type) {
        Type[] allOfTypes = context.annotations().getAnnotationValue(annotatedClass, SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_ALL_OF);
        return allOfTypes != null && Arrays.stream(allOfTypes).map(Type::name).anyMatch(type.name()::equals);
    }

    public static boolean isAllOf(AnnotationScannerContext context, ClassInfo annotatedClass, Type type) {
        return isIncludedAllOf(context, annotatedClass, type) || isAutomaticAllOf(context, annotatedClass, type);
    }

    static boolean isAutomaticAllOf(AnnotationScannerContext context, ClassInfo annotatedClass, Type type) {
        if (context.annotations().getAnnotationValue(annotatedClass, SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_ALL_OF) == null) {
            switch (context.getConfig().getAutoInheritance()) {
                case NONE:
                    break;
                case BOTH:
                    return true;
                case PARENT_ONLY:
                    return Objects.equals(annotatedClass.superClassType(), type);
            }
        }
        return false;
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
            case RECORD_COMPONENT:
                break;
        }

        return null;
    }

    public static void mapDeprecated(AnnotationScannerContext context, AnnotationTarget target, Supplier<Boolean> getDeprecated,
            Consumer<Boolean> setDeprecated) {
        if (getDeprecated.get() != null) {
            return;
        }

        AnnotationInstance deprecated = context.annotations().getAnnotation(
                target,
                Arrays.asList(JDKConstants.DOTNAME_DEPRECATED, KotlinConstants.DEPRECATED));

        if (deprecated != null && JandexUtil.equals(deprecated.target(), target)) {
            setDeprecated.accept(Boolean.TRUE);
        }
    }

    static final class TypeWithFormat {
        static class Builder {
            private final Map<String, Object> properties = new HashMap<>();
            private boolean opaque;

            Builder(SchemaType schemaType) {
                properties.put(SchemaConstant.PROP_TYPE, schemaType);
            }

            Builder opaque(boolean opaque) {
                this.opaque = opaque;
                return this;
            }

            Builder opaque() {
                return opaque(true);
            }

            Builder format(String format) {
                properties.put(SchemaConstant.PROP_FORMAT, format);
                return this;
            }

            Builder pattern(String pattern) {
                properties.put(SchemaConstant.PROP_PATTERN, pattern);
                return this;
            }

            Builder example(Object example) {
                properties.put(SchemaConstant.PROP_EXAMPLE, example);
                return this;
            }

            Builder externalDocumentation(String description, String url) {
                ExternalDocumentation doc = OASFactory.createExternalDocumentation();
                doc.setDescription(description);
                doc.setUrl(url);
                properties.put(SchemaConstant.PROP_EXTERNAL_DOCS, doc);
                return this;
            }

            TypeWithFormat build() {
                return new TypeWithFormat(properties, opaque);
            }
        }

        static Builder of(SchemaType schemaType) {
            Objects.requireNonNull(schemaType);
            return new Builder(schemaType);
        }

        static Builder anyType() {
            return new Builder(null);
        }

        private final Map<String, Object> properties;
        private final boolean opaque;

        private TypeWithFormat(Map<String, Object> properties, boolean opaque) {
            this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
            this.opaque = opaque;
        }

        boolean isSchemaType(SchemaType... schemaTypes) {
            SchemaType type = (SchemaType) properties.get(SchemaConstant.PROP_TYPE);
            return type != null && Arrays.stream(schemaTypes).anyMatch(type::equals);
        }

        Map<String, Object> getProperties() {
            return properties;
        }

        public boolean isOpaque() {
            return opaque;
        }
    }

    private static class DataFormat {
        static final String INT32 = "int32";
        static final String INT64 = "int64";
        static final String FLOAT = "float";
        static final String DOUBLE = "double";
        static final String BINARY = "binary";
        static final String BYTE = "byte";
        static final String DATE = "date";
        static final String DATE_TIME = "date-time";
        static final String DURATION = "duration";
        static final String URI = "uri";
        static final String UUID = "uuid";
        static final String TIME = "time";
        static final String TIME_LOCAL = "local-time";
    }

}
