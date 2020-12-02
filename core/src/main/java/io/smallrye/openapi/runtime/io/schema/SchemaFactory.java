package io.smallrye.openapi.runtime.io.schema;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsReader;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
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
     * @param context scanning context
     * @param value the annotation value
     * @return Schema model
     */
    public static Schema readSchema(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }

        AnnotationInstance schemaAnnotation = value.asNested();

        if (isAnnotationMissingOrHidden(schemaAnnotation, Collections.emptyMap())) {
            return null;
        }

        return readSchema(context, new SchemaImpl(), schemaAnnotation, Collections.emptyMap());
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

        if (isAnnotationMissingOrHidden(annotation, defaults)) {
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

        if (isAnnotationMissingOrHidden(annotation, defaults)) {
            return schema;
        }

        schema.setNot(SchemaFactory.<Type, Schema> readAttr(annotation, SchemaConstant.PROP_NOT,
                type -> readClassSchema(context, type, true), defaults));
        schema.setOneOf(SchemaFactory.<Type[], List<Schema>> readAttr(annotation, SchemaConstant.PROP_ONE_OF,
                type -> readClassSchemas(context, type), defaults));
        schema.setAnyOf(SchemaFactory.<Type[], List<Schema>> readAttr(annotation, SchemaConstant.PROP_ANY_OF,
                type -> readClassSchemas(context, type), defaults));
        schema.setAllOf(SchemaFactory.<Type[], List<Schema>> readAttr(annotation, SchemaConstant.PROP_ALL_OF,
                type -> readClassSchemas(context, type), defaults));
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
        AnnotationInstance externalDocsAnnotation = JandexUtil.value(annotation, ExternalDocsConstant.PROP_EXTERNAL_DOCS);
        schema.setExternalDocs(ExternalDocsReader.readExternalDocs(context, externalDocsAnnotation));
        schema.setDeprecated(readAttr(annotation, SchemaConstant.PROP_DEPRECATED, defaults));
        final SchemaType schemaType = SchemaFactory.<String, SchemaType> readAttr(annotation, SchemaConstant.PROP_TYPE,
                value -> JandexUtil.enumValue(value, SchemaType.class), defaults);
        schema.setType(schemaType);
        schema.setExample(parseSchemaAttr(annotation, SchemaConstant.PROP_EXAMPLE, defaults, schemaType));
        schema.setDefaultValue(readAttr(annotation, SchemaConstant.PROP_DEFAULT_VALUE, defaults));
        schema.setDiscriminator(
                readDiscriminator(context,
                        JandexUtil.value(annotation, SchemaConstant.PROP_DISCRIMINATOR_PROPERTY),
                        JandexUtil.value(annotation, SchemaConstant.PROP_DISCRIMINATOR_MAPPING)));
        schema.setMaxItems(readAttr(annotation, SchemaConstant.PROP_MAX_ITEMS, defaults));
        schema.setMinItems(readAttr(annotation, SchemaConstant.PROP_MIN_ITEMS, defaults));
        schema.setUniqueItems(readAttr(annotation, SchemaConstant.PROP_UNIQUE_ITEMS, defaults));
        schema.setExtensions(ExtensionReader.readExtensions(context, annotation));

        schema.setProperties(SchemaFactory.<AnnotationInstance[], Map<String, Schema>> readAttr(annotation,
                SchemaConstant.PROP_PROPERTIES, properties -> {
                    if (properties == null || properties.length == 0) {
                        return null;
                    }
                    Map<String, Schema> propertySchemas = new LinkedHashMap<>(properties.length);
                    for (AnnotationInstance propAnnotation : properties) {
                        String key = JandexUtil.value(propAnnotation, SchemaConstant.PROP_NAME);
                        Schema value = readSchema(context, new SchemaImpl(), propAnnotation, Collections.emptyMap());
                        propertySchemas.put(key, value);
                    }

                    return propertySchemas;
                }, defaults));

        List<Object> enumeration = readAttr(annotation, SchemaConstant.PROP_ENUMERATION, defaults);

        if (enumeration != null && !enumeration.isEmpty()) {
            schema.setEnumeration(enumeration);
        }

        boolean namedComponent = SchemaImpl.isNamed(schema);

        if (JandexUtil.isSimpleClassSchema(annotation)) {
            Schema implSchema = readClassSchema(context,
                    JandexUtil.value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    !namedComponent);
            schema = MergeUtil.mergeObjects(implSchema, schema);
        } else if (JandexUtil.isSimpleArraySchema(annotation)) {
            Schema implSchema = readClassSchema(context,
                    JandexUtil.value(annotation, SchemaConstant.PROP_IMPLEMENTATION),
                    !namedComponent);
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else {
            Schema implSchema = readClassSchema(context,
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

    static boolean isAnnotationMissingOrHidden(AnnotationInstance annotation, Map<String, Object> defaults) {
        if (annotation == null) {
            return true;
        }

        // Schemas can be hidden. Skip if that's the case.
        return Boolean.TRUE.equals(readAttr(annotation, SchemaConstant.PROP_HIDDEN, defaults));
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
    static Object parseSchemaAttr(AnnotationInstance annotation, String propertyName, Map<String, Object> defaults,
            SchemaType schemaType) {
        return readAttr(annotation, propertyName, value -> {
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
     * @param context scanning context
     * @param type the implementation type of the item to scan
     * @param schemaReferenceSupported
     */
    static Schema readClassSchema(final AnnotationScannerContext context, Type type, boolean schemaReferenceSupported) {
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
                schema.items(
                        readClassSchema(context, ArrayType.create(componentType, dimensions - 1), schemaReferenceSupported));
            } else {
                // Recurse using the type of the array elements
                schema.items(readClassSchema(context, componentType, schemaReferenceSupported));
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
     * @param extensions list of AnnotationScannerExtensions
     * @return Schema model
     */
    public static Schema typeToSchema(final AnnotationScannerContext context, Type type,
            List<AnnotationScannerExtension> extensions) {
        Schema schema = null;

        if (TypeUtil.isOptional(type)) {
            // Recurse using the optional's type
            return typeToSchema(context, TypeUtil.getOptionalType(type), extensions);
        } else if (CurrentScannerInfo.isWrapperType(type)) {
            // Recurse using the wrapped type
            return typeToSchema(context, CurrentScannerInfo.getCurrentAnnotationScanner().unwrapType(type), extensions);
        } else if (type.kind() == Type.Kind.ARRAY) {
            schema = new SchemaImpl().type(SchemaType.ARRAY);
            ArrayType array = type.asArrayType();
            int dimensions = array.dimensions();
            Type componentType = array.component();

            if (dimensions > 1) {
                // Recurse using a new array type with dimensions decremented
                schema.items(typeToSchema(context, ArrayType.create(componentType, dimensions - 1), extensions));
            } else {
                // Recurse using the type of the array elements
                schema.items(typeToSchema(context, componentType, extensions));
            }
        } else if (type.kind() == Type.Kind.CLASS) {
            schema = introspectClassToSchema(context, type.asClassType(), true);
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            schema = otherTypeToSchema(context, type, extensions);
        }

        return schema;
    }

    /**
     * Convert a Jandex enum class type to a {@link Schema} model.Adds each enum constant name to the list of the given schema's
     * enumeration list.
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
        final int ENUM = 0x00004000; // see java.lang.reflect.Modifier#ENUM
        ClassInfo enumKlazz = context.getIndex().getClassByName(TypeUtil.getName(enumType));
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

            enumSchema = readSchema(context, enumSchema, schemaAnnotation, enumKlazz, defaults);
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
     * @param context scanning context
     * @param ctype
     * @param schemaReferenceSupported
     */
    private static Schema introspectClassToSchema(final AnnotationScannerContext context, ClassType ctype,
            boolean schemaReferenceSupported) {

        if (CurrentScannerInfo.isScannerInternalResponse(ctype)) {
            return null;
        }

        SchemaRegistry schemaRegistry = SchemaRegistry.currentInstance();

        if (schemaReferenceSupported && schemaRegistry.hasRef(ctype)) {
            return schemaRegistry.lookupRef(ctype);
        } else if (!schemaReferenceSupported && schemaRegistry != null && schemaRegistry.hasSchema(ctype)) {
            // Clone the schema from the registry using mergeObjects
            return MergeUtil.mergeObjects(new SchemaImpl(), schemaRegistry.lookupSchema(ctype));
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
        SchemaRegistry schemaRegistry = SchemaRegistry.currentInstance();

        if (allowRegistration(context, schemaRegistry, type, schema)) {
            schema = schemaRegistry.register(type, schema);
        } else if (schemaRegistry != null && schemaRegistry.hasRef(type)) {
            schema = schemaRegistry.lookupRef(type);
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
        if (schema == null || registry == null || !registry.isTypeRegistrationSupported(type, schema)) {
            return false;
        }

        /*
         * Only register if the type is not already registered
         */
        return !registry.hasSchema(type);
    }

    /**
     * Reads an array of Class annotations to produce a list of {@link Schema} models.
     * 
     * @param context scanning context
     * @param types the implementation types of the items to scan, never null
     */
    private static List<Schema> readClassSchemas(final AnnotationScannerContext context, Type[] types) {
        IoLogging.logger.annotationsList("schema Class");

        return Arrays.stream(types)
                .map(type -> readClassSchema(context, type, true))
                .collect(Collectors.toList());
    }

    private static Schema otherTypeToSchema(final AnnotationScannerContext context, Type type,
            List<AnnotationScannerExtension> extensions) {
        if (TypeUtil.isA(context, type, MutinyConstants.MULTI_TYPE)) {
            // Treat as an Array
            Schema schema = new SchemaImpl().type(SchemaType.ARRAY);
            Type componentType = type.asParameterizedType().arguments().get(0);

            // Recurse using the type of the array elements
            schema.items(typeToSchema(context, componentType, extensions));
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
                    (TypeUtil.isA(context, type, JDKConstants.COMPLETION_STAGE_TYPE) ||
                            TypeUtil.isA(context, type, MutinyConstants.UNI_TYPE))) {
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
     * @param context scanning context
     * @param propertyName the OAS required value specified by the
     *        {@link org.eclipse.microprofile.openapi.annotations.media.Schema#discriminatorProperty() discriminatorProperty}
     *        attribute.
     * @param annotation reference to the array of
     *        {@link org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping @DiscriminatorMapping} annotations
     *        given by {@link org.eclipse.microprofile.openapi.annotations.media.Schema#discriminatorMapping()
     *        discriminatorMapping}
     */
    private static Discriminator readDiscriminator(final AnnotationScannerContext context,
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
            IoLogging.logger.annotationsList("@DiscriminatorMapping");

            for (AnnotationInstance nested : annotation) {
                String propertyValue = JandexUtil.stringValue(nested, SchemaConstant.PROP_VALUE);

                AnnotationValue schemaValue = nested.value(SchemaConstant.PROP_SCHEMA);
                String schemaRef;

                if (schemaValue != null) {
                    ClassType schemaType = schemaValue.asClass().asClassType();
                    Schema schema = introspectClassToSchema(context, schemaType, true);
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
