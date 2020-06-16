package io.smallrye.openapi.runtime.io.schema;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.JDKConstants;
import io.smallrye.openapi.api.constants.MutinyConstants;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.media.DiscriminatorImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.CurrentScannerInfo;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;
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
     * @param index the index
     * @param value the annotation value
     * @return Schema model
     */
    public static Schema readSchema(IndexView index, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readSchema(index, value.asNested());
    }

    /**
     * Reads a Schema annotation into a model.
     *
     * @param index the index
     * @param annotation the annotation instance
     * @return Schema model
     */
    public static Schema readSchema(IndexView index, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        IoLogging.log.singleAnnotation("@Schema");

        // Schemas can be hidden. Skip if that's the case.
        Optional<Boolean> isHidden = JandexUtil.booleanValue(annotation, SchemaConstant.PROP_HIDDEN);

        if (isHidden.isPresent() && isHidden.get()) {
            return null;
        }

        return readSchema(index, new SchemaImpl(), annotation, Collections.emptyMap());
    }

    /**
     * Populates the schema using the {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * on the provided class. If the schema has already been registered (in components), the existing
     * registration will be replaced.
     * 
     * @param index application class index
     * @param schema schema model to populate
     * @param annotation schema annotation to read
     * @param clazz the class annotated with {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * @return the schema, possibly replaced if <code>implementation</code> has been specified in the annotation
     */
    public static Schema readSchema(IndexView index,
            Schema schema,
            AnnotationInstance annotation,
            ClassInfo clazz) {
        return readSchema(index, schema, annotation, clazz, Collections.emptyMap());
    }

    /**
     * Populates the schema using the {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * on the provided class. If the schema has already been registered (in components), the existing
     * registration will be replaced.
     * 
     * @param index application class index
     * @param schema schema model to populate
     * @param annotation schema annotation to read
     * @param clazz the class annotated with {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * @param defaults default values to be set on the schema when not present in the annotation
     * @return the schema, possibly replaced if <code>implementation</code> has been specified in the annotation
     */
    static Schema readSchema(IndexView index,
            Schema schema,
            AnnotationInstance annotation,
            ClassInfo clazz,
            Map<String, Object> defaults) {

        if (annotation == null) {
            return schema;
        }

        // Schemas can be hidden. Skip if that's the case.
        Optional<Boolean> isHidden = JandexUtil.booleanValue(annotation, SchemaConstant.PROP_HIDDEN);

        if (isHidden.isPresent() && isHidden.get()) {
            return schema;
        }

        schema = readSchema(index, schema, annotation, defaults);
        ClassType clazzType = (ClassType) Type.create(clazz.name(), Type.Kind.CLASS);

        /*
         * The registry may already contain the type from earlier in the scan if the
         * type has been referenced as a field, etc. The schema here is "fuller" as it
         * now contains information gathered from the @Schema annotation on the class.
         * 
         * Ignore the reference returned by register, the caller expects the full schema.
         */
        schemaRegistration(index, clazzType, schema);

        return schema;
    }

    public static Schema readSchema(IndexView index,
            Schema schema,
            AnnotationInstance annotation,
            Map<String, Object> defaults) {
        if (annotation == null) {
            return schema;
        }

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = readAttr(annotation, SchemaConstant.PROP_HIDDEN, defaults);

        if (Boolean.TRUE.equals(isHidden)) {
            return schema;
        }

        schema.setNot(SchemaFactory.<Type, Schema> readAttr(annotation, SchemaConstant.PROP_NOT,
                type -> readClassSchema(index, type, true), defaults));
        schema.setOneOf(SchemaFactory.<Type[], List<Schema>> readAttr(annotation, SchemaConstant.PROP_ONE_OF,
                type -> readClassSchemas(index, type), defaults));
        schema.setAnyOf(SchemaFactory.<Type[], List<Schema>> readAttr(annotation, SchemaConstant.PROP_ANY_OF,
                type -> readClassSchemas(index, type), defaults));
        schema.setAllOf(SchemaFactory.<Type[], List<Schema>> readAttr(annotation, SchemaConstant.PROP_ALL_OF,
                type -> readClassSchemas(index, type), defaults));
        schema.setTitle(readAttr(annotation, SchemaConstant.PROP_TITLE, defaults));
        schema.setMultipleOf(SchemaFactory.<Double, BigDecimal> readAttr(annotation, SchemaConstant.PROP_MULTIPLE_OF,
                BigDecimal::valueOf, defaults));
        schema.setMaximum(SchemaFactory.<String, BigDecimal> readAttr(annotation, SchemaConstant.PROP_MAXIMUM,
                BigDecimal::new, defaults));
        schema.setMinimum(SchemaFactory.<String, BigDecimal> readAttr(annotation, SchemaConstant.PROP_MINIMUM,
                BigDecimal::new, defaults));
        schema.setExclusiveMaximum(readAttr(annotation, SchemaConstant.PROP_EXCLUSIVE_MAXIMUM, defaults));
        schema.setExclusiveMinimum(readAttr(annotation, SchemaConstant.PROP_EXCLUSIVE_MINIMUM, defaults));
        schema.setMaxLength(readAttr(annotation, SchemaConstant.PROP_MAX_LENGTH, defaults));
        schema.setMinLength(readAttr(annotation, SchemaConstant.PROP_MIN_LENGTH, defaults));
        schema.setPattern(readAttr(annotation, SchemaConstant.PROP_PATTERN, defaults));
        schema.setMaxProperties(readAttr(annotation, SchemaConstant.PROP_MAX_PROPERTIES, defaults));
        schema.setMinProperties(readAttr(annotation, SchemaConstant.PROP_MIN_PROPERTIES, defaults));
        schema.setRequired(readAttr(annotation, SchemaConstant.PROP_REQUIRED_PROPERTIES, defaults));
        schema.setDescription(readAttr(annotation, SchemaConstant.PROP_DESCRIPTION, defaults));
        schema.setFormat(readAttr(annotation, SchemaConstant.PROP_FORMAT, defaults));
        schema.setRef(readAttr(annotation, OpenApiConstants.REF, defaults));
        schema.setNullable(readAttr(annotation, SchemaConstant.PROP_NULLABLE, defaults));
        schema.setReadOnly(readAttr(annotation, SchemaConstant.PROP_READ_ONLY, defaults));
        schema.setWriteOnly(readAttr(annotation, SchemaConstant.PROP_WRITE_ONLY, defaults));
        AnnotationInstance annotationInstance = JandexUtil.value(annotation, ExternalDocsConstant.PROP_EXTERNAL_DOCS);
        schema.setExternalDocs(ExternalDocsReader.readExternalDocs(annotationInstance));
        schema.setDeprecated(readAttr(annotation, SchemaConstant.PROP_DEPRECATED, defaults));
        final SchemaType schemaType = SchemaFactory.<String, SchemaType> readAttr(annotation, SchemaConstant.PROP_TYPE,
                value -> JandexUtil.enumValue(value, SchemaType.class), defaults);
        schema.setType(schemaType);
        schema.setExample(parseSchemaAttr(annotation, SchemaConstant.PROP_EXAMPLE, defaults, schemaType));
        schema.setDefaultValue(readAttr(annotation, SchemaConstant.PROP_DEFAULT_VALUE, defaults));
        schema.setDiscriminator(
                readDiscriminator(index,
                        JandexUtil.value(annotation, SchemaConstant.PROP_DISCRIMINATOR_PROPERTY),
                        JandexUtil.value(annotation, SchemaConstant.PROP_DISCRIMINATOR_MAPPING)));
        schema.setMaxItems(readAttr(annotation, SchemaConstant.PROP_MAX_ITEMS, defaults));
        schema.setMinItems(readAttr(annotation, SchemaConstant.PROP_MIN_ITEMS, defaults));
        schema.setUniqueItems(readAttr(annotation, SchemaConstant.PROP_UNIQUE_ITEMS, defaults));

        List<Object> enumeration = readAttr(annotation, SchemaConstant.PROP_ENUMERATION, defaults);

        if (enumeration != null && !enumeration.isEmpty()) {
            schema.setEnumeration(enumeration);
        }

        if (schema instanceof SchemaImpl) {
            ((SchemaImpl) schema).setName(readAttr(annotation, SchemaConstant.PROP_NAME, defaults));
        }

        if (JandexUtil.isSimpleClassSchema(annotation)) {
            Schema implSchema = readClassSchema(index,
                    JandexUtil.value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    true);
            schema = MergeUtil.mergeObjects(implSchema, schema);
        } else if (JandexUtil.isSimpleArraySchema(annotation)) {
            Schema implSchema = readClassSchema(index,
                    JandexUtil.value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    true);
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else {
            Schema implSchema = readClassSchema(index,
                    JandexUtil.value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    false);

            if (schema.getType() == Schema.SchemaType.ARRAY && implSchema != null) {
                // If the @Schema annotation indicates an array type, then use the Schema
                // generated from the implementation Class as the "items" for the array.
                schema.setItems(implSchema);
            } else if (implSchema != null) {
                // If there is an impl class - merge the @Schema properties *onto* the schema
                // generated from the Class so that the annotation properties override the class
                // properties (as required by the MP+OAI spec).
                schema = MergeUtil.mergeObjects(implSchema, schema);
            }
        }

        return schema;
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
    static <T> T readAttr(AnnotationInstance annotation, String propertyName, Map<String, Object> defaults) {
        Object value = JandexUtil.value(annotation, propertyName);

        if (value == null) {
            value = defaults.get(propertyName);
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
     * @param annotation the annotation to read
     * @param propertyName the name of the attribute to read
     * @param defaults map of default values
     * @param schemaType related schema type for this attribute
     * @return the annotation attribute value, a default value, or null
     */
    static <T> T parseSchemaAttr(AnnotationInstance annotation, String propertyName, Map<String, Object> defaults,
            SchemaType schemaType) {
        return (T) readAttr(annotation, propertyName, value -> {
            if (!(value instanceof String)) {
                return value;
            }
            String stringValue = ((String) value);
            if (schemaType != SchemaType.STRING) {
                return JsonUtil.parseValue(stringValue);
            }
            return stringValue;
        }, defaults);
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
    static <R, T> T readAttr(AnnotationInstance annotation, String propertyName, Function<R, T> converter,
            Map<String, Object> defaults) {
        R rawValue = JandexUtil.value(annotation, propertyName);
        T value;

        if (rawValue == null) {
            value = (T) defaults.get(propertyName);
        } else {
            value = converter.apply(rawValue);
        }

        return value;
    }

    /**
     * Introspect into the given Class to generate a Schema model. The boolean indicates
     * whether this class type should be turned into a reference.
     *
     * @param index the index of classes being scanned
     * @param type the implementation type of the item to scan
     * @param schemaReferenceSupported
     */
    static Schema readClassSchema(IndexView index, Type type, boolean schemaReferenceSupported) {
        if (type == null) {
            return null;
        }
        Schema schema;
        if (type.kind() == Type.Kind.ARRAY) {
            schema = new SchemaImpl().type(SchemaType.ARRAY);
            ArrayType array = type.asArrayType();
            int dimensions = array.dimensions();
            Type componentType = array.component();

            if (dimensions > 1) {
                // Recurse using a new array type with dimensions decremented
                schema.items(readClassSchema(index, ArrayType.create(componentType, dimensions - 1), schemaReferenceSupported));
            } else {
                // Recurse using the type of the array elements
                schema.items(readClassSchema(index, componentType, schemaReferenceSupported));
            }
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            schema = introspectClassToSchema(index, type.asClassType(), schemaReferenceSupported);
        }
        return schema;
    }

    /**
     * Converts a Jandex type to a {@link Schema} model.
     * 
     * @param index the index of classes being scanned
     * @param type the implementation type of the item to scan
     * @param extensions list of AnnotationScannerExtensions
     * @return Schema model
     */
    public static Schema typeToSchema(IndexView index, Type type, List<AnnotationScannerExtension> extensions) {
        Schema schema = null;

        AnnotationScanner annotationScanner = CurrentScannerInfo.getCurrentAnnotationScanner();

        if (TypeUtil.isOptional(type)) {
            // Recurse using the optional's type
            return typeToSchema(index, TypeUtil.getOptionalType(type), extensions);
        } else if (annotationScanner.isWrapperType(type)) {
            // Recurse using the wrapped type
            return typeToSchema(index, annotationScanner.unwrapType(type), extensions);
        } else if (type.kind() == Type.Kind.ARRAY) {
            schema = new SchemaImpl().type(SchemaType.ARRAY);
            ArrayType array = type.asArrayType();
            int dimensions = array.dimensions();
            Type componentType = array.component();

            if (dimensions > 1) {
                // Recurse using a new array type with dimensions decremented
                schema.items(typeToSchema(index, ArrayType.create(componentType, dimensions - 1), extensions));
            } else {
                // Recurse using the type of the array elements
                schema.items(typeToSchema(index, componentType, extensions));
            }
        } else if (type.kind() == Type.Kind.CLASS) {
            schema = introspectClassToSchema(index, type.asClassType(), true);
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            schema = otherTypeToSchema(index, type, extensions);
        }

        return schema;
    }

    /**
     * Convert a Jandex enum class type to a {@link Schema} model.Adds each enum constant name to the list of the given schema's
     * enumeration list.
     * 
     * The given type must be found in the index.
     *
     * @param index Jandex index containing the ClassInfo for the given enum type
     * @param enumType type containing Java Enum constants
     * @return Schema model
     *
     * @see java.lang.reflect.Field#isEnumConstant()
     */
    public static Schema enumToSchema(IndexView index, Type enumType) {
        IoLogging.log.enumProcessing(enumType);
        final int ENUM = 0x00004000; // see java.lang.reflect.Modifier#ENUM
        ClassInfo enumKlazz = index.getClassByName(TypeUtil.getName(enumType));
        AnnotationInstance schemaAnnotation = enumKlazz.classAnnotation(SchemaConstant.DOTNAME_SCHEMA);
        Schema enumSchema = new SchemaImpl();
        List<Object> enumeration = enumKlazz.fields()
                .stream()
                .filter(field -> (field.flags() & ENUM) != 0)
                .map(FieldInfo::name)
                .sorted() // Make the order determinate
                .collect(Collectors.toList());

        if (schemaAnnotation != null) {
            Map<String, Object> defaults = new HashMap<>(2);
            defaults.put(SchemaConstant.PROP_TYPE, SchemaType.STRING);
            defaults.put(SchemaConstant.PROP_ENUMERATION, enumeration);

            enumSchema = readSchema(index, enumSchema, schemaAnnotation, enumKlazz, defaults);
        } else {
            enumSchema.setType(SchemaType.STRING);
            enumSchema.setEnumeration(enumeration);
        }

        return enumSchema;
    }

    /**
     * Introspect the given class type to generate a Schema model. The boolean indicates
     * whether this class type should be turned into a reference.
     *
     * @param index the index of classes being scanned
     * @param ctype
     * @param schemaReferenceSupported
     */
    private static Schema introspectClassToSchema(IndexView index, ClassType ctype, boolean schemaReferenceSupported) {
        AnnotationScanner annotationScanner = CurrentScannerInfo.getCurrentAnnotationScanner();

        if (annotationScanner.isScannerInternalResponse(ctype)) {
            return null;
        }

        SchemaRegistry schemaRegistry = SchemaRegistry.currentInstance();

        if (schemaReferenceSupported && schemaRegistry.has(ctype)) {
            return schemaRegistry.lookupRef(ctype);
        } else {
            Schema schema = OpenApiDataObjectScanner.process(index, ctype);
            if (schemaReferenceSupported) {
                return schemaRegistration(index, ctype, schema);
            } else {
                return schema;
            }
        }
    }

    /**
     * Register the provided schema in the SchemaRegistry if allowed.
     * 
     * @param index the index of classes being scanned
     * @param type the type of the schema to register
     * @param schema a schema
     * @return a reference to the registered schema or the input schema when registration is not allowed/possible
     */
    static Schema schemaRegistration(IndexView index, Type type, Schema schema) {
        SchemaRegistry schemaRegistry = SchemaRegistry.currentInstance();

        if (allowRegistration(index, schemaRegistry, type, schema)) {
            schema = schemaRegistry.register(type, schema);
        }

        return schema;
    }

    /**
     * Determines if the give schema may be registered. Schemas may only be registered
     * if non-null; the type is allowed for registration; and a schema for the given type
     * is not already in the registry or the schema being registered is not already a
     * reference that has been registered.
     * 
     * @param index
     * @param registry
     * @param type
     * @param schema
     * @return true if the schema may be registered, otherwise false
     */
    static boolean allowRegistration(IndexView index, SchemaRegistry registry, Type type, Schema schema) {
        if (schema == null || registry == null || !TypeUtil.allowRegistration(index, type)) {
            return false;
        }

        /*
         * Only register if the type is not already registered or the schema
         * being registered is not the same as an existing reference already
         * in the registry. Such a situation may occur if a downstream process
         * registered the schema now being checked.
         */
        return !registry.has(type) || !registry.lookupRef(type).equals(schema);
    }

    /**
     * Reads an array of Class annotations to produce a list of {@link Schema} models.
     * 
     * @param index the index of classes being scanned
     * @param types the implementation types of the items to scan, never null
     */
    private static List<Schema> readClassSchemas(IndexView index, Type[] types) {
        IoLogging.log.annotationsList("schema Class");

        return Arrays.stream(types)
                .map(type -> readClassSchema(index, type, true))
                .collect(Collectors.toList());
    }

    private static Schema otherTypeToSchema(IndexView index, Type type, List<AnnotationScannerExtension> extensions) {
        if (TypeUtil.isA(index, type, MutinyConstants.MULTI_TYPE)) {
            // Treat as an Array
            Schema schema = new SchemaImpl().type(SchemaType.ARRAY);
            Type componentType = type.asParameterizedType().arguments().get(0);

            // Recurse using the type of the array elements
            schema.items(typeToSchema(index, componentType, extensions));
            return schema;
        } else {
            Type asyncType = resolveAsyncType(index, type, extensions);
            return schemaRegistration(index, asyncType, OpenApiDataObjectScanner.process(index, asyncType));
        }
    }

    static Type resolveAsyncType(IndexView index, Type type, List<AnnotationScannerExtension> extensions) {
        if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType pType = type.asParameterizedType();
            if (pType.arguments().size() == 1 &&
                    (TypeUtil.isA(index, type, JDKConstants.COMPLETION_STAGE_TYPE) ||
                            TypeUtil.isA(index, type, MutinyConstants.UNI_TYPE))) {
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

    /**
     * Reads a discriminator property name and an optional array of
     * {@link org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping @DiscriminatorMapping}
     * annotations into a {@link Discriminator} model.
     *
     * @param index set of scanned classes to be used for further introspection
     * @param propertyName the OAS required value specified by the
     *        {@link org.eclipse.microprofile.openapi.annotations.media.Schema#discriminatorProperty() discriminatorProperty}
     *        attribute.
     * @param annotation reference to the array of
     *        {@link org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping @DiscriminatorMapping} annotations
     *        given by {@link org.eclipse.microprofile.openapi.annotations.media.Schema#discriminatorMapping()
     *        discriminatorMapping}
     */
    private static Discriminator readDiscriminator(IndexView index,
            String propertyName,
            AnnotationInstance[] annotation) {

        if (propertyName == null && annotation == null) {
            return null;
        }

        Discriminator discriminator = new DiscriminatorImpl();

        /*
         * The name is required by OAS, however MP OpenAPI allows for a default
         * (blank) name. This results in an invalid OpenAPI document if
         * considering annotation scanning in isolation.
         */
        if (propertyName != null) {
            discriminator.setPropertyName(propertyName);
        }

        if (annotation != null) {
            IoLogging.log.annotationsList("@DiscriminatorMapping");

            for (AnnotationInstance nested : annotation) {
                String propertyValue = JandexUtil.stringValue(nested, SchemaConstant.PROP_VALUE);

                AnnotationValue schemaValue = nested.value(SchemaConstant.PROP_SCHEMA);
                String schemaRef;

                if (schemaValue != null) {
                    ClassType schemaType = schemaValue.asClass().asClassType();
                    Schema schema = introspectClassToSchema(index, schemaType, true);
                    schemaRef = schema != null ? schema.getRef() : null;
                } else {
                    schemaRef = null;
                }

                if (propertyValue == null && schemaRef != null) {
                    // No mapping key provided, use the implied value.
                    propertyValue = ModelUtil.nameFromRef(schemaRef);
                }

                discriminator.addMapping(propertyValue, schemaRef);
            }
        }

        return discriminator;
    }
}
