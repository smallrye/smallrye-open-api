package io.smallrye.openapi.runtime.io.schema;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.smallrye.openapi.api.constants.JDKConstants;
import io.smallrye.openapi.api.constants.MutinyConstants;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.model.BaseModel;
import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.dataobject.EnumProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class SchemaFactory {

    private SchemaFactory() {
    }

    /**
     * Reads a Schema annotation into a model.
     *
     * @param context scanning context
     * @param value the annotation value
     * @return Schema model
     */
    public static Schema readSchema(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }

        return readSchema(context, value.asNested());
    }

    /**
     * Reads a Schema annotation into a model.
     *
     * @param context scanning context
     * @param schemaAnnotation the {@code @Schema} annotation
     * @return Schema model
     */
    public static Schema readSchema(final AnnotationScannerContext context, AnnotationInstance schemaAnnotation) {
        if (isAnnotationMissingOrHidden(context, schemaAnnotation, Collections.emptyMap())) {
            return null;
        }

        return readSchema(context, OASFactory.createSchema(), schemaAnnotation, Collections.emptyMap());
    }

    /**
     * Populates the schema using the {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * on the provided class. If the schema has already been registered (in components), the existing
     * registration will be replaced.
     *
     * @param context scanning context
     * @param schema schema model to populate
     * @param annotation schema annotation to read
     * @param clazz the class annotated with {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * @return the schema, possibly replaced if <code>implementation</code> has been specified in the annotation
     */
    public static Schema readSchema(final AnnotationScannerContext context,
            Schema schema,
            AnnotationInstance annotation,
            ClassInfo clazz) {
        return readSchema(context, schema, annotation, clazz, Collections.emptyMap());
    }

    /**
     * Populates the schema using the {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * on the provided class. If the schema has already been registered (in components), the existing
     * registration will be replaced.
     *
     * @param context scanning context
     * @param schema schema model to populate
     * @param annotation schema annotation to read
     * @param clazz the class annotated with {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * @param defaults default values to be set on the schema when not present in the annotation
     * @return the schema, possibly replaced if <code>implementation</code> has been specified in the annotation
     */
    static Schema readSchema(final AnnotationScannerContext context,
            Schema schema,
            AnnotationInstance annotation,
            ClassInfo clazz,
            Map<String, Object> defaults) {

        if (isAnnotationMissingOrHidden(context, annotation, defaults)) {
            return schema;
        }

        schema = readSchema(context, schema, annotation, defaults);
        ClassType clazzType = (ClassType) Type.create(clazz.name(), Type.Kind.CLASS);

        /*
         * The registry may already contain the type from earlier in the scan if the
         * type has been referenced as a field, etc. The schema here is "fuller" as it
         * now contains information gathered from the @Schema annotation on the class.
         *
         * Ignore the reference returned by register, the caller expects the full schema.
         */
        schemaRegistration(context, clazzType, schema);

        return schema;
    }

    public static Schema readSchema(final AnnotationScannerContext context,
            Schema schema,
            AnnotationInstance annotation,
            Map<String, Object> defaults) {

        if (isAnnotationMissingOrHidden(context, annotation, defaults)) {
            return schema;
        }

        String ref = readAttr(context, annotation, SchemaConstant.REF, defaults);
        if (ref != null) {
            schema.setRef(ref);
            // If the annotation sets a ref, ignore any defaults passed in
            defaults = Collections.emptyMap();
        }

        schema.setNot(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_NOT,
                types -> readClassSchema(context, types, true), defaults));
        schema.setOneOf(SchemaFactory.<Type[], List<Schema>> readAttr(context, annotation, SchemaConstant.PROP_ONE_OF,
                types -> readClassSchemas(context, types, false), defaults));
        schema.setAnyOf(SchemaFactory.<Type[], List<Schema>> readAttr(context, annotation, SchemaConstant.PROP_ANY_OF,
                types -> readClassSchemas(context, types, false), defaults));
        schema.setAllOf(SchemaFactory.<Type[], List<Schema>> readAttr(context, annotation, SchemaConstant.PROP_ALL_OF,
                types -> readClassSchemas(context, types, true), defaults));
        schema.setIfSchema(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_IF_SCHEMA,
                types -> readClassSchema(context, types, true), defaults));
        schema.setThenSchema(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_THEN_SCHEMA,
                types -> readClassSchema(context, types, true), defaults));
        schema.setElseSchema(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_ELSE_SCHEMA,
                types -> readClassSchema(context, types, true), defaults));
        schema.setTitle(readAttr(context, annotation, SchemaConstant.PROP_TITLE, defaults));
        schema.setMultipleOf(SchemaFactory.<Double, BigDecimal> readAttr(context, annotation, SchemaConstant.PROP_MULTIPLE_OF,
                BigDecimal::valueOf, defaults));
        BigDecimal maximum = SchemaFactory.readAttr(context, annotation, SchemaConstant.PROP_MAXIMUM,
                SchemaFactory::tolerantParseBigDecimal, defaults);
        BigDecimal minimum = SchemaFactory.readAttr(context, annotation, SchemaConstant.PROP_MINIMUM,
                SchemaFactory::tolerantParseBigDecimal, defaults);
        if (maximum != null) {
            if (Boolean.TRUE.equals(readAttr(context, annotation, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, defaults))) {
                schema.setExclusiveMaximum(maximum);
            } else {
                schema.setMaximum(maximum);
            }
        }
        if (minimum != null) {
            if (Boolean.TRUE.equals(readAttr(context, annotation, SchemaConstant.PROP_EXCLUSIVE_MINIMUM, defaults))) {
                schema.setExclusiveMinimum(minimum);
            } else {
                schema.setMinimum(minimum);
            }
        }
        schema.setMaxLength(readAttr(context, annotation, SchemaConstant.PROP_MAX_LENGTH, defaults));
        schema.setMinLength(readAttr(context, annotation, SchemaConstant.PROP_MIN_LENGTH, defaults));
        schema.setPattern(readAttr(context, annotation, SchemaConstant.PROP_PATTERN, defaults));
        schema.setMaxProperties(readAttr(context, annotation, SchemaConstant.PROP_MAX_PROPERTIES, defaults));
        schema.setMinProperties(readAttr(context, annotation, SchemaConstant.PROP_MIN_PROPERTIES, defaults));
        schema.setRequired(readAttr(context, annotation, SchemaConstant.PROP_REQUIRED_PROPERTIES, defaults));
        schema.setDescription(readAttr(context, annotation, SchemaConstant.PROP_DESCRIPTION, defaults));
        schema.setFormat(readAttr(context, annotation, SchemaConstant.PROP_FORMAT, defaults));
        SchemaSupport.setNullable(schema, readAttr(context, annotation, SchemaConstant.PROP_NULLABLE, defaults));
        schema.setReadOnly(readAttr(context, annotation, SchemaConstant.PROP_READ_ONLY, defaults));
        schema.setWriteOnly(readAttr(context, annotation, SchemaConstant.PROP_WRITE_ONLY, defaults));
        schema.setExternalDocs(context.io().extDocIO().read(annotation.value(SchemaConstant.PROP_EXTERNAL_DOCS)));
        schema.setDeprecated(readAttr(context, annotation, SchemaConstant.PROP_DEPRECATED, defaults));

        final SchemaType type = readSchemaType(context, annotation, schema, defaults);
        SchemaSupport.setType(schema, readSchemaType(context, annotation, schema, defaults));

        Object example = parseSchemaAttr(context, annotation, SchemaConstant.PROP_EXAMPLE, defaults, type);
        List<Object> examples = SchemaFactory.<String[], List<Object>> readAttr(context, annotation,
                SchemaConstant.PROP_EXAMPLES,
                egs -> Arrays.stream(egs)
                        .map(e -> parseValue(context, e, type))
                        .collect(Collectors.toCollection(ArrayList::new)),
                defaults);
        if (examples != null) {
            if (example != null) {
                examples.add(example);
            }
            schema.setExamples(examples);
        } else {
            schema.setExamples(wrapInList(example));
        }

        schema.setDefaultValue(
                parseSchemaAttr(context, annotation, SchemaConstant.PROP_DEFAULT_VALUE, defaults, type));
        schema.setDiscriminator(context.io().discriminatorIO().read(annotation));
        schema.setMaxItems(readAttr(context, annotation, SchemaConstant.PROP_MAX_ITEMS, defaults));
        schema.setMinItems(readAttr(context, annotation, SchemaConstant.PROP_MIN_ITEMS, defaults));
        schema.setUniqueItems(readAttr(context, annotation, SchemaConstant.PROP_UNIQUE_ITEMS, defaults));
        schema.setExtensions(context.io().extensionIO().readExtensible(annotation));
        schema.setComment(readAttr(context, annotation, SchemaConstant.PROP_COMMENT_FIELD, defaults));
        schema.setConstValue(parseSchemaAttr(context, annotation, SchemaConstant.PROP_CONST_VALUE, defaults, type));

        schema.setProperties(SchemaFactory.<AnnotationInstance[], Map<String, Schema>> readAttr(context, annotation,
                SchemaConstant.PROP_PROPERTIES, properties -> {
                    if (properties == null || properties.length == 0) {
                        return null;
                    }
                    Map<String, Schema> propertySchemas = new LinkedHashMap<>(properties.length);
                    for (AnnotationInstance propAnnotation : properties) {
                        String key = context.annotations().value(propAnnotation, SchemaConstant.PROP_NAME);
                        Schema value = readSchema(context, OASFactory.createSchema(), propAnnotation, Collections.emptyMap());
                        propertySchemas.put(key, value);
                    }

                    return propertySchemas;
                }, defaults));

        schema.setAdditionalPropertiesSchema(
                SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_ADDITIONAL_PROPERTIES,
                        types -> readClassSchema(context, types, true), defaults));

        schema.setDependentRequired(SchemaFactory.<AnnotationInstance[], Map<String, List<String>>> readAttr(context,
                annotation, SchemaConstant.PROP_DEPENDENT_REQUIRED,
                annos -> readDependentRequired(context, annos), defaults));

        schema.setDependentSchemas(SchemaFactory.<AnnotationInstance[], Map<String, Schema>> readAttr(context, annotation,
                SchemaConstant.PROP_DEPENDENT_SCHEMAS,
                annos -> readDependentSchemas(context, annos), defaults));

        schema.setContentEncoding(readAttr(context, annotation, SchemaConstant.PROP_CONTENT_ENCODING, defaults));
        schema.setContentMediaType(readAttr(context, annotation, SchemaConstant.PROP_CONTENT_MEDIA_TYPE, defaults));
        schema.setContentSchema(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_CONTENT_SCHEMA,
                types -> readClassSchema(context, types, true), defaults));

        schema.setContains(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_CONTAINS,
                types -> readClassSchema(context, types, true), defaults));
        schema.setMaxContains(readAttr(context, annotation, SchemaConstant.PROP_MAX_CONTAINS, defaults));
        schema.setMinContains(readAttr(context, annotation, SchemaConstant.PROP_MIN_CONTAINS, defaults));
        schema.setPrefixItems(
                SchemaFactory.<Type[], List<Schema>> readAttr(context, annotation, SchemaConstant.PROP_PREFIX_ITEMS,
                        types -> readClassSchemas(context, types, true), defaults));
        schema.setPropertyNames(SchemaFactory.<Type, Schema> readAttr(context, annotation, SchemaConstant.PROP_PROPERTY_NAMES,
                types -> readClassSchema(context, types, true), defaults));
        schema.setPatternProperties(SchemaFactory.<AnnotationInstance[], Map<String, Schema>> readAttr(context, annotation,
                SchemaConstant.PROP_PATTERN_PROPERTIES,
                annos -> readPatternProperties(context, annos), defaults));

        List<Object> enumeration = readAttr(context, annotation, SchemaConstant.PROP_ENUMERATION, (Object[] values) -> {
            List<Object> parsed = new ArrayList<>(values.length);

            if (type == Schema.SchemaType.STRING) {
                parsed.addAll(Arrays.asList(values));
            } else {
                Arrays.stream(values)
                        .map(String.class::cast)
                        .map(v -> parseValue(context, v, type))
                        .forEach(parsed::add);
            }

            return parsed;
        }, defaults);

        if (enumeration != null && !enumeration.isEmpty()) {
            schema.setEnumeration(enumeration);
        }

        boolean namedComponent = Extensions.getName(schema) != null;

        if (JandexUtil.isSimpleClassSchema(annotation)) {
            Schema implSchema = readClassSchema(context,
                    context.annotations().value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    !namedComponent);
            schema = MergeUtil.mergeObjects(implSchema, schema);
        } else if (JandexUtil.isSimpleArraySchema(context, annotation)) {
            Schema implSchema = readClassSchema(context,
                    context.annotations().value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    !namedComponent);
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else {
            schema = includeTypeSchema(context, schema,
                    context.annotations().value(annotation, SchemaConstant.PROP_IMPLEMENTATION));
        }

        return schema;
    }

    public static Schema includeTypeSchema(AnnotationScannerContext context, Schema schema, Type type) {
        Schema implSchema = null;

        if (type != null /* && type.kind() == Kind.CLASS */) {
            implSchema = readClassSchema(context, type, false);
        }

        if (schema.getType() != null && schema.getType().contains(Schema.SchemaType.ARRAY) && implSchema != null) {
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else if (implSchema != null) {
            // If there is an impl class - merge the @Schema properties *onto* the schema
            // generated from the Class so that the annotation properties override the class
            // properties (as required by the MP+OAI spec).
            schema = MergeUtil.mergeObjects(implSchema, schema);
        }

        return schema;
    }

    static boolean isAnnotationMissingOrHidden(AnnotationScannerContext context, AnnotationInstance annotation,
            Map<String, Object> defaults) {
        if (annotation == null) {
            return true;
        }

        // Schemas can be hidden. Skip if that's the case.
        return Boolean.TRUE.equals(readAttr(context, annotation, SchemaConstant.PROP_HIDDEN, defaults));
    }

    /**
     * Reads the attribute named by propertyName from annotation. If no value was specified,
     * an optional default value is retrieved from the defaults map using the propertyName as
     * they key. Array-typed annotation values will be converted to List.
     *
     * @param <T> the type of the annotation attribute value
     * @param annotation the annotation to read
     * @param propertyName the name of the attribute to read
     * @param defaults map of default values
     * @return the annotation attribute value, a default value, or null
     */
    @SuppressWarnings("unchecked")
    static <T> T readAttr(AnnotationScannerContext context, AnnotationInstance annotation, String propertyName,
            Map<String, Object> defaults) {
        return readAttr(context, annotation, propertyName, (T) defaults.get(propertyName));
    }

    /**
     * Reads the attribute named by propertyName from annotation. If no value was specified,
     * an optional default value is retrieved from the defaults map using the propertyName as
     * they key. Array-typed annotation values will be converted to List.
     *
     * @param <T> the type of the annotation attribute value
     * @param annotation the annotation to read
     * @param propertyName the name of the attribute to read
     * @param defaults the default value
     * @return the annotation attribute value, a default value, or null
     */
    @SuppressWarnings("unchecked")
    static <T> T readAttr(AnnotationScannerContext context, AnnotationInstance annotation, String propertyName,
            T defaultValue) {
        Object value = context.annotations().value(annotation, propertyName);

        if (value == null) {
            value = defaultValue;
        } else if (value.getClass().isArray()) {
            value = Arrays.stream((T[]) value).collect(Collectors.toList());
        }

        return (T) value;
    }

    /**
     * Reads the attribute named by propertyName from annotation, and parses it to identified type. If no value was specified,
     * an optional default value is retrieved from the defaults map using the propertyName as
     * they key. Array-typed annotation values will be converted to List.
     *
     * @param <T> the type of the annotation attribute value
     * @param context scanning context
     * @param annotation the annotation to read
     * @param propertyName the name of the attribute to read
     * @param defaults map of default values
     * @param schemaType related schema type for this attribute
     * @return the annotation attribute value, a default value, or null
     */
    static Object parseSchemaAttr(AnnotationScannerContext context, AnnotationInstance annotation, String propertyName,
            Map<String, Object> defaults, SchemaType schemaType) {
        return readAttr(context, annotation, propertyName, value -> {
            if (value instanceof String) {
                return parseValue(context, (String) value, schemaType);
            }
            return value;
        }, defaults);
    }

    static Object parseValue(AnnotationScannerContext context, String stringValue, SchemaType schemaType) {
        if (schemaType != SchemaType.STRING) {
            Object parsedValue;
            for (AnnotationScannerExtension e : context.getExtensions()) {
                parsedValue = e.parseValue(stringValue);
                if (parsedValue != null) {
                    return parsedValue;
                }
            }
        }
        return stringValue;
    }

    /**
     * Reads the attribute named by propertyName from annotation and converts a non-null value using the
     * provided converter function. If no value was specified, an optional default value is retrieved
     * from the defaults map using the propertyName as they key.
     *
     * @param <R> the type of the annotation attribute value
     * @param <T> the type into which the annotation is to be converted
     * @param annotation the annotation to read
     * @param propertyName the name of the attribute to read
     * @param defaults map of default values
     * @param converter function used to convert from the raw attribute value to the desired type
     * @return the converted annotation attribute value, a default value, or null
     */
    @SuppressWarnings("unchecked")
    static <R, T> T readAttr(AnnotationScannerContext context, AnnotationInstance annotation, String propertyName,
            Function<R, T> converter,
            Map<String, Object> defaults) {
        R rawValue = context.annotations().value(annotation, propertyName);
        T value;

        if (rawValue == null) {
            value = (T) defaults.get(propertyName);
        } else {
            value = converter.apply(rawValue);
        }

        return value;
    }

    /**
     * Read the <code>type</code> property from the provided <code>@Schema<code> annotation.
     * When null, the value previously set on the given schema object (if any) will be returned.
     *
     * @param annotation schema annotation being processed
     * @param schema schema model being populated
     * @param defaults default schema property values or empty
     * @return the value of the type property if not null, otherwise the current schema type
     */
    static SchemaType readSchemaType(AnnotationScannerContext context, AnnotationInstance annotation, Schema schema,
            Map<String, Object> defaults) {
        SchemaType type = readAttr(context, annotation, SchemaConstant.PROP_TYPE, SchemaFactory::parseSchemaType, defaults);
        if (type != null) {
            return type;
        }
        return SchemaSupport.getNonNullType(schema);
    }

    /**
     * Convert the string value to the enum equivalent <code>SchemaType</code>
     *
     * @param value string value from the <code>@Schema<code> annotation.
     * @return the equivalent SchemaType value, or null if not set or no match
     */
    static SchemaType parseSchemaType(String value) {
        try {
            return SchemaType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // This will only occur for `org.eclipse.microprofile.openapi.annotations.enums.SchemaType#DEFAULT`.
            return null;
        }
    }

    static <T> List<T> wrapInList(T value) {
        if (value == null) {
            return null;
        } else {
            return Collections.singletonList(value);
        }
    }

    /**
     * Introspect into the given Class to generate a Schema model. The boolean indicates
     * whether this class type should be turned into a reference.
     *
     * @param context scanning context
     * @param type the implementation type of the item to scan
     * @param schemaReferenceSupported
     */
    static Schema readClassSchema(final AnnotationScannerContext context, Type type, boolean schemaReferenceSupported) {
        if (type == null) {
            return null;
        }
        Schema schema;
        if (type.name().equals(SchemaConstant.DOTNAME_TRUE_SCHEMA)) {
            schema = OASFactory.createSchema().booleanSchema(true);
        } else if (type.name().equals(SchemaConstant.DOTNAME_FALSE_SCHEMA)) {
            schema = OASFactory.createSchema().booleanSchema(false);
        } else if (type.kind() == Type.Kind.ARRAY) {
            schema = OASFactory.createSchema().addType(SchemaType.ARRAY);
            ArrayType array = type.asArrayType();
            int dimensions = array.dimensions();
            Type componentType = array.constituent();

            if (dimensions > 1) {
                // Recurse using a new array type with dimensions decremented
                schema.setItems(
                        readClassSchema(context, ArrayType.create(componentType, dimensions - 1), schemaReferenceSupported));
            } else {
                // Recurse using the type of the array elements
                schema.setItems(readClassSchema(context, componentType, schemaReferenceSupported));
            }
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            schema = introspectClassToSchema(context, type.asClassType(), schemaReferenceSupported);
        }
        return schema;
    }

    /**
     * Converts a Jandex type to a {@link Schema} model.
     *
     * @param context scanning context
     * @param type the implementation type of the item to scan
     * @return Schema model
     */
    public static Schema typeToSchema(AnnotationScannerContext context, Type type) {
        return typeToSchema(context, type, null, context.getExtensions());
    }

    /**
     * Converts a Jandex type to a {@link Schema} model.
     *
     * @param context scanning context
     * @param type the implementation type of the item to scan
     * @param extensions list of AnnotationScannerExtensions
     * @return Schema model
     */
    public static Schema typeToSchema(final AnnotationScannerContext context, Type type,
            AnnotationInstance schemaAnnotation,
            List<AnnotationScannerExtension> extensions) {
        Schema schema = null;
        Schema fromAnnotation = null;

        if (schemaAnnotation != null) {
            fromAnnotation = SchemaFactory.readSchema(context, schemaAnnotation);

            if (fromAnnotation == null) {
                // hidden
                return null;
            }
        }

        Optional<AnnotationScanner> currentScanner = context.getCurrentScanner();

        if (TypeUtil.isWrappedType(type)) {
            // Recurse using the optional's type
            schema = typeToSchema(context, TypeUtil.unwrapType(type), null, extensions);
        } else if (currentScanner.isPresent() && currentScanner.get().isWrapperType(type)) {
            // Recurse using the wrapped type
            schema = typeToSchema(context, currentScanner.get().unwrapType(type), null,
                    extensions);
        } else if (TypeUtil.isTerminalType(type)) {
            schema = OASFactory.createSchema();
            TypeUtil.applyTypeAttributes(type, schema);
            schema = schemaRegistration(context, type, schema);
        } else if (type.kind() == Type.Kind.ARRAY) {
            schema = OASFactory.createSchema().addType(SchemaType.ARRAY);
            ArrayType array = type.asArrayType();
            int dimensions = array.dimensions();
            Type componentType = array.constituent();

            if (dimensions > 1) {
                // Recurse using a new array type with dimensions decremented
                schema.setItems(
                        typeToSchema(context, ArrayType.create(componentType, dimensions - 1), null, extensions));
            } else {
                // Recurse using the type of the array elements
                schema.setItems(typeToSchema(context, componentType, null, extensions));
            }
        } else if (type.kind() == Type.Kind.CLASS) {
            schema = introspectClassToSchema(context, type.asClassType(), true);
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            schema = otherTypeToSchema(context, type, extensions);
        }

        if (fromAnnotation != null) {
            // Generate `allOf` ?
            schema = MergeUtil.mergeObjects(schema, fromAnnotation);
        }

        return schema;
    }

    /**
     * Convert a Jandex enum class type to a {@link Schema} model. Adds each enum constant
     * name to the list of the given schema's enumeration list.
     *
     * Enumeration values are obtained preferring values from any method annotated with
     * Jackson's `@JsonValue`, if present. The enum class must be loaded in the context's
     * ClassLoader to perform the value extraction.
     *
     * If the annotation is not present, is not located on a supplier method, or
     * any reflection error occurs, the enumeration values will default to the enum
     * constant names listed in the ClassInfo.
     *
     * The given type must be found in the index.
     *
     * @param context scanning context
     * @param enumType type containing Java Enum constants
     * @return Schema model
     *
     * @see java.lang.reflect.Field#isEnumConstant()
     */
    public static Schema enumToSchema(final AnnotationScannerContext context, Type enumType) {
        IoLogging.logger.enumProcessing(enumType);
        List<Object> enumeration = EnumProcessor.enumConstants(context, enumType);
        ClassInfo enumKlazz = context.getIndex().getClassByName(TypeUtil.getName(enumType));
        AnnotationInstance schemaAnnotation = context.annotations().getAnnotation(enumKlazz, SchemaConstant.DOTNAME_SCHEMA);
        Schema enumSchema = OASFactory.createSchema();
        Type enumValueType = enumeration.isEmpty() ? Type.create(String.class) : Type.create(enumeration.get(0).getClass());

        if (schemaAnnotation != null) {
            Map<String, Object> defaults = new LinkedHashMap<>(TypeUtil.getTypeAttributes(enumValueType));
            defaults.put(SchemaConstant.PROP_ENUMERATION, enumeration);

            enumSchema = readSchema(context, enumSchema, schemaAnnotation, enumKlazz, defaults);
        } else {
            TypeUtil.applyTypeAttributes(enumValueType, enumSchema);
            enumSchema.setEnumeration(enumeration);
        }

        return enumSchema;
    }

    /**
     * Introspect the given class type to generate a Schema model. The boolean indicates
     * whether this class type should be turned into a reference.
     *
     * @param context scanning context
     * @param ctype
     * @param schemaReferenceSupported
     */
    private static Schema introspectClassToSchema(final AnnotationScannerContext context, ClassType ctype,
            boolean schemaReferenceSupported) {

        Optional<AnnotationScanner> currentScanner = context.getCurrentScanner();

        if (currentScanner.isPresent() && currentScanner.get().isScannerInternalResponse(ctype)) {
            return null;
        }

        ClassInfo classInfo = context.getAugmentedIndex().getClass(ctype);
        if (classInfo != null) {
            AnnotationInstance schemaAnnotation = context.annotations().getAnnotation(classInfo, SchemaConstant.DOTNAME_SCHEMA);
            if (schemaAnnotation != null
                    && Boolean.TRUE.equals(readAttr(context, schemaAnnotation, SchemaConstant.PROP_HIDDEN, false))) {
                return null;
            }
        }
        SchemaRegistry schemaRegistry = context.getSchemaRegistry();

        if (schemaRegistry.hasSchema(ctype, context.getJsonViews())) {
            if (schemaReferenceSupported) {
                return schemaRegistry.lookupRef(ctype, context.getJsonViews());
            } else {
                // Clone the schema from the registry
                Schema registered = schemaRegistry.lookupSchema(ctype, context.getJsonViews());
                return BaseModel.deepCopy(registered, Schema.class);
            }
        } else if (context.getScanStack().contains(ctype)) {
            // Protect against stack overflow when the type is in the process of being scanned.
            return schemaRegistry.registerReference(ctype, context.getJsonViews(), null, OASFactory.createSchema());
        } else {
            Schema schema = OpenApiDataObjectScanner.process(context, ctype);

            if (schemaReferenceSupported) {
                return schemaRegistration(context, ctype, schema);
            } else {
                return schema;
            }
        }
    }

    /**
     * Register the provided schema in the SchemaRegistry if allowed.
     *
     * @param context scanning context
     * @param type the type of the schema to register
     * @param schema a schema
     * @return a reference to the registered schema or the input schema when registration is not allowed/possible
     */
    public static Schema schemaRegistration(final AnnotationScannerContext context, Type type, Schema schema) {
        SchemaRegistry schemaRegistry = context.getSchemaRegistry();

        if (allowRegistration(context, schemaRegistry, type, schema)) {
            schema = schemaRegistry.register(type, context.getJsonViews(), schema);
        } else if (schemaRegistry != null && schemaRegistry.hasRef(type, context.getJsonViews())) {
            schema = schemaRegistry.lookupRef(type, context.getJsonViews());
        }

        return schema;
    }

    /**
     * Determines if the give schema may be registered. Schemas may only be registered
     * if non-null; the type is allowed for registration; and a schema for the given type
     * is not already in the registry or the schema being registered is not already a
     * reference that has been registered.
     *
     * @param context scanning context
     * @param registry
     * @param type
     * @param schema
     * @return true if the schema may be registered, otherwise false
     */
    static boolean allowRegistration(final AnnotationScannerContext context, SchemaRegistry registry, Type type,
            Schema schema) {
        if (schema == null || registry.isDisabled() || !registry.isTypeRegistrationSupported(type, schema)) {
            return false;
        }

        /*
         * Only register if the type is not already registered
         */
        return !registry.hasSchema(type, context.getJsonViews());
    }

    /**
     * Reads an array of Class annotations to produce a list of {@link Schema} models.
     *
     * @param context scanning context
     * @param types the implementation types of the items to scan, never null
     */
    private static List<Schema> readClassSchemas(final AnnotationScannerContext context, Type[] types, boolean removeCurrent) {
        if (Arrays.stream(types).allMatch(type -> type.kind() == Kind.VOID)) {
            return null; // NOSONAR - intentionally return null
        }

        IoLogging.logger.annotationsList("schema Class");

        Type introspectedClassType = removeCurrent ? context.getScanStack().peek() : null;

        return Arrays.stream(types)
                .filter(type -> !type.equals(introspectedClassType))
                .map(type -> readClassSchema(context, type, true))
                .collect(Collectors.toList());
    }

    private static Schema otherTypeToSchema(final AnnotationScannerContext context, Type type,
            List<AnnotationScannerExtension> extensions) {
        if (TypeUtil.isA(context, type, MutinyConstants.MULTI_TYPE)) {
            // Treat as an Array
            Schema schema = OASFactory.createSchema().addType(SchemaType.ARRAY);
            Type componentType = type.asParameterizedType().arguments().get(0);

            // Recurse using the type of the array elements
            schema.setItems(typeToSchema(context, componentType, null, extensions));
            return schema;
        } else {
            Type asyncType = resolveAsyncType(context, type, extensions);
            return schemaRegistration(context, asyncType, OpenApiDataObjectScanner.process(context, asyncType));
        }
    }

    static Type resolveAsyncType(final AnnotationScannerContext context, Type type,
            List<AnnotationScannerExtension> extensions) {
        if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType pType = type.asParameterizedType();
            if (pType.arguments().size() == 1 &&
                    (TypeUtil.isA(context, type, JDKConstants.COMPLETION_STAGE_TYPE))) {
                return pType.arguments().get(0);
            }
        }
        for (AnnotationScannerExtension extension : extensions) {
            Type asyncType = extension.resolveAsyncType(type);
            if (asyncType != null)
                return asyncType;
        }
        return type;
    }

    private static BigDecimal tolerantParseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, List<String>> readDependentRequired(AnnotationScannerContext context,
            AnnotationInstance[] requireds) {
        if (requireds == null || requireds.length == 0) {
            return null;
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (AnnotationInstance required : requireds) {
            String name = context.annotations().value(required, SchemaConstant.PROP_NAME);
            String[] requires = context.annotations().value(required, SchemaConstant.PROP_REQUIRES);
            result.put(name, new ArrayList<>(asList(requires)));
        }

        return result;
    }

    private static Map<String, Schema> readDependentSchemas(AnnotationScannerContext context,
            AnnotationInstance[] dependentSchemas) {
        if (dependentSchemas == null || dependentSchemas.length == 0) {
            return null;
        }

        Map<String, Schema> result = new LinkedHashMap<>();
        for (AnnotationInstance dependentSchema : dependentSchemas) {
            String name = context.annotations().value(dependentSchema, SchemaConstant.PROP_NAME);
            Type schemaClass = context.annotations().value(dependentSchema, SchemaConstant.PROP_SCHEMA);
            Schema schema = readClassSchema(context, schemaClass, true);
            if (schema != null) {
                result.put(name, schema);
            }
        }
        return result;
    }

    private static Map<String, Schema> readPatternProperties(AnnotationScannerContext context,
            AnnotationInstance[] patternProperties) {
        if (patternProperties == null || patternProperties.length == 0) {
            return null;
        }

        Map<String, Schema> result = new LinkedHashMap<>();
        for (AnnotationInstance patternProperty : patternProperties) {
            String regex = context.annotations().value(patternProperty, SchemaConstant.PROP_REGEX);
            Type schemaClass = context.annotations().value(patternProperty, SchemaConstant.PROP_SCHEMA);
            Schema schema = readClassSchema(context, schemaClass, true);
            if (schema != null) {
                result.put(regex, schema);
            }
        }
        return result;
    }
}
