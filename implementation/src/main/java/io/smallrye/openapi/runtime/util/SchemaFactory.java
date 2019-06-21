package io.smallrye.openapi.runtime.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.api.models.media.DiscriminatorImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class SchemaFactory {

    private static Logger LOG = Logger.getLogger(SchemaFactory.class);

    private SchemaFactory() {
    }

    /**
     * Reads a Schema annotation into a model.
     *
     * @param index
     * @param value
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
     * @param index
     * @param annotation
     */
    public static Schema readSchema(IndexView index, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Schema annotation.");

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_HIDDEN);

        if (Boolean.TRUE.equals(isHidden)) {
            return null;
        }

        return readSchema(index, new SchemaImpl(), annotation, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public static Schema readSchema(IndexView index,
            Schema schema,
            AnnotationInstance annotation,
            Map<String, Object> overrides) {
        if (annotation == null) {
            return schema;
        }

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_HIDDEN);

        if (Boolean.TRUE.equals(isHidden)) {
            return schema;
        }

        schema.setNot((Schema) overrides.getOrDefault(OpenApiConstants.PROP_NOT,
                readClassSchema(index, annotation.value(OpenApiConstants.PROP_NOT), true)));
        schema.setOneOf((List<Schema>) overrides.getOrDefault(OpenApiConstants.PROP_ONE_OF,
                readClassSchemas(index, annotation.value(OpenApiConstants.PROP_ONE_OF))));
        schema.setAnyOf((List<Schema>) overrides.getOrDefault(OpenApiConstants.PROP_ANY_OF,
                readClassSchemas(index, annotation.value(OpenApiConstants.PROP_ANY_OF))));
        schema.setAllOf((List<Schema>) overrides.getOrDefault(OpenApiConstants.PROP_ALL_OF,
                readClassSchemas(index, annotation.value(OpenApiConstants.PROP_ALL_OF))));
        schema.setTitle((String) overrides.getOrDefault(OpenApiConstants.PROP_TITLE,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_TITLE)));
        schema.setMultipleOf((BigDecimal) overrides.getOrDefault(OpenApiConstants.PROP_MULTIPLE_OF,
                JandexUtil.bigDecimalValue(annotation, OpenApiConstants.PROP_MULTIPLE_OF)));
        schema.setMaximum((BigDecimal) overrides.getOrDefault(OpenApiConstants.PROP_MAXIMUM,
                JandexUtil.bigDecimalValue(annotation, OpenApiConstants.PROP_MAXIMUM)));
        schema.setExclusiveMaximum((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_EXCLUSIVE_MAXIMUM,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_EXCLUSIVE_MAXIMUM)));
        schema.setMinimum((BigDecimal) overrides.getOrDefault(OpenApiConstants.PROP_MINIMUM,
                JandexUtil.bigDecimalValue(annotation, OpenApiConstants.PROP_MINIMUM)));
        schema.setExclusiveMinimum((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_EXCLUSIVE_MINIMUM,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_EXCLUSIVE_MINIMUM)));
        schema.setMaxLength((Integer) overrides.getOrDefault(OpenApiConstants.PROP_MAX_LENGTH,
                JandexUtil.intValue(annotation, OpenApiConstants.PROP_MAX_LENGTH)));
        schema.setMinLength((Integer) overrides.getOrDefault(OpenApiConstants.PROP_MIN_LENGTH,
                JandexUtil.intValue(annotation, OpenApiConstants.PROP_MIN_LENGTH)));
        schema.setPattern((String) overrides.getOrDefault(OpenApiConstants.PROP_PATTERN,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_PATTERN)));
        schema.setMaxProperties((Integer) overrides.getOrDefault(OpenApiConstants.PROP_MAX_PROPERTIES,
                JandexUtil.intValue(annotation, OpenApiConstants.PROP_MAX_PROPERTIES)));
        schema.setMinProperties((Integer) overrides.getOrDefault(OpenApiConstants.PROP_MIN_PROPERTIES,
                JandexUtil.intValue(annotation, OpenApiConstants.PROP_MIN_PROPERTIES)));
        schema.setRequired((List<String>) overrides.getOrDefault(OpenApiConstants.PROP_REQUIRED_PROPERTIES,
                JandexUtil.stringListValue(annotation, OpenApiConstants.PROP_REQUIRED_PROPERTIES)));
        schema.setDescription((String) overrides.getOrDefault(OpenApiConstants.PROP_DESCRIPTION,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION)));
        schema.setFormat((String) overrides.getOrDefault(OpenApiConstants.PROP_FORMAT,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_FORMAT)));
        schema.setRef((String) overrides.getOrDefault(OpenApiConstants.PROP_REF,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_REF)));
        schema.setNullable((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_NULLABLE,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_NULLABLE)));
        schema.setReadOnly((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_READ_ONLY,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_READ_ONLY)));
        schema.setWriteOnly((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_WRITE_ONLY,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_WRITE_ONLY)));
        schema.setExample(overrides.getOrDefault(OpenApiConstants.PROP_EXAMPLE,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXAMPLE)));
        schema.setExternalDocs(readExternalDocs(annotation.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        schema.setDeprecated((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_DEPRECATED,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_DEPRECATED)));
        schema.setType((Schema.SchemaType) overrides.getOrDefault(OpenApiConstants.PROP_TYPE,
                JandexUtil.enumValue(annotation, OpenApiConstants.PROP_TYPE, Schema.SchemaType.class)));
        schema.setDefaultValue(overrides.getOrDefault(OpenApiConstants.PROP_DEFAULT_VALUE,
                JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DEFAULT_VALUE)));
        schema.setDiscriminator(
                readDiscriminator(index, JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DISCRIMINATOR_PROPERTY),
                        annotation.value(OpenApiConstants.PROP_DISCRIMINATOR_MAPPING)));
        schema.setMaxItems((Integer) overrides.getOrDefault(OpenApiConstants.PROP_MAX_ITEMS,
                JandexUtil.intValue(annotation, OpenApiConstants.PROP_MAX_ITEMS)));
        schema.setMinItems((Integer) overrides.getOrDefault(OpenApiConstants.PROP_MIN_ITEMS,
                JandexUtil.intValue(annotation, OpenApiConstants.PROP_MIN_ITEMS)));
        schema.setUniqueItems((Boolean) overrides.getOrDefault(OpenApiConstants.PROP_UNIQUE_ITEMS,
                JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_UNIQUE_ITEMS)));

        List<Object> enumeration = (List<Object>) overrides.getOrDefault(OpenApiConstants.PROP_ENUMERATION,
                JandexUtil.stringListValue(annotation, OpenApiConstants.PROP_ENUMERATION));
        if (enumeration != null && !enumeration.isEmpty()) {
            schema.setEnumeration(enumeration);
        }

        if (schema instanceof SchemaImpl) {
            ((SchemaImpl) schema).setName((String) overrides.getOrDefault(OpenApiConstants.PROP_NAME,
                    JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME)));
        }

        if (JandexUtil.isSimpleClassSchema(annotation)) {
            Schema implSchema = readClassSchema(index, annotation.value(OpenApiConstants.PROP_IMPLEMENTATION), true);
            schema = MergeUtil.mergeObjects(implSchema, schema);
        } else if (JandexUtil.isSimpleArraySchema(annotation)) {
            Schema implSchema = readClassSchema(index, annotation.value(OpenApiConstants.PROP_IMPLEMENTATION), true);
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else {
            Schema implSchema = readClassSchema(index, annotation.value(OpenApiConstants.PROP_IMPLEMENTATION), false);

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
     * Introspect into the given Class to generate a Schema model. The boolean indicates
     * whether this class type should be turned into a reference.
     *
     * @param index the index of classes being scanned
     * @param value annotation
     * @param schemaReferenceSupported
     */
    public static Schema readClassSchema(IndexView index, AnnotationValue value, boolean schemaReferenceSupported) {
        if (value == null) {
            return null;
        }
        ClassType ctype = (ClassType) value.asClass();
        return introspectClassToSchema(index, ctype, schemaReferenceSupported);
    }

    /**
     * Converts a jandex type to a {@link Schema} model.
     * 
     * @param index
     * @param type
     * @param extensions
     */
    public static Schema typeToSchema(IndexView index, Type type, List<AnnotationScannerExtension> extensions) {
        Schema schema = null;
        if (type.kind() == Type.Kind.CLASS) {
            schema = introspectClassToSchema(index, type.asClassType(), true);
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            Type asyncType = resolveAsyncType(type, extensions);
            schema = OpenApiDataObjectScanner.process(index, asyncType);

            if (schema != null && index.getClassByName(asyncType.name()) != null) {
                SchemaRegistry schemaRegistry = SchemaRegistry.currentInstance();
                schema = schemaRegistry.register(asyncType, schema);
            }
        }

        return schema;
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
        if (ctype.name().equals(OpenApiConstants.DOTNAME_RESPONSE)) {
            return null;
        }

        SchemaRegistry schemaRegistry = SchemaRegistry.currentInstance();

        if (schemaReferenceSupported && schemaRegistry.has(ctype)) {
            return schemaRegistry.lookupRef(ctype);
        } else {
            Schema schema = OpenApiDataObjectScanner.process(index, ctype);
            if (schemaReferenceSupported && schema != null && index.getClassByName(ctype.name()) != null) {
                return schemaRegistry.register(ctype, schema);
            } else {
                return schema;
            }
        }
    }

    /**
     * Reads an array of Class annotations to produce a list of {@link Schema} models.
     * 
     * @param index the index of classes being scanned
     * @param value
     */
    private static List<Schema> readClassSchemas(IndexView index, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of schema Class annotations.");
        Type[] classArray = value.asClassArray();
        List<Schema> schemas = new ArrayList<>(classArray.length);
        for (Type type : classArray) {
            ClassType ctype = (ClassType) type;
            Schema schema = introspectClassToSchema(index, ctype, true);
            schemas.add(schema);
        }
        return schemas;
    }

    private static Type resolveAsyncType(Type type, List<AnnotationScannerExtension> extensions) {
        if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType pType = type.asParameterizedType();
            if (pType.name().equals(OpenApiConstants.COMPLETION_STAGE_NAME)
                    && pType.arguments().size() == 1)
                return pType.arguments().get(0);
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
            AnnotationValue annotation) {

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
            LOG.debug("Processing a list of @DiscriminatorMapping annotations.");

            for (AnnotationInstance nested : annotation.asNestedArray()) {
                String propertyValue = JandexUtil.stringValue(nested, OpenApiConstants.PROP_VALUE);

                AnnotationValue schemaValue = nested.value(OpenApiConstants.PROP_SCHEMA);
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

    private static ExternalDocumentation readExternalDocs(AnnotationValue externalDocAnno) {
        if (externalDocAnno == null) {
            return null;
        }
        AnnotationInstance nested = externalDocAnno.asNested();
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        externalDoc.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return externalDoc;
    }
}
