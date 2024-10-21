package io.smallrye.openapi.runtime.scanner.dataobject;

import static io.smallrye.openapi.api.constants.JaxbConstants.PROP_NAME;
import static io.smallrye.openapi.api.constants.JaxbConstants.XML_ATTRIBUTE;
import static io.smallrye.openapi.api.constants.JaxbConstants.XML_ELEMENT;
import static io.smallrye.openapi.api.constants.JaxbConstants.XML_WRAPPERELEMENT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner.RequirementHandler;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Process annotation targets such as {@link FieldInfo}.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class AnnotationTargetProcessor implements RequirementHandler {

    private final AnnotationScannerContext context;
    private final DataObjectDeque objectStack;
    private final DataObjectDeque.PathEntry parentPathEntry;
    private final TypeResolver typeResolver;
    private final Type entityType;

    // May be null if field is unannotated.
    private final AnnotationTarget annotationTarget;

    private AnnotationTargetProcessor(final AnnotationScannerContext context,
            DataObjectDeque objectStack,
            DataObjectDeque.PathEntry parentPathEntry,
            TypeResolver typeResolver,
            AnnotationTarget annotationTarget,
            Type entityType) {

        this.context = context;
        this.objectStack = objectStack;
        this.parentPathEntry = parentPathEntry;
        this.typeResolver = typeResolver;
        this.entityType = entityType;
        this.annotationTarget = annotationTarget;
    }

    public static Schema process(final AnnotationScannerContext context,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry) {

        AnnotationTargetProcessor fp = new AnnotationTargetProcessor(context, objectStack, parentPathEntry, typeResolver,
                typeResolver.getAnnotationTarget(), typeResolver.getUnresolvedType());
        return fp.processField();
    }

    public static Schema process(final AnnotationScannerContext context,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry,
            Type type) {
        AnnotationTargetProcessor fp = new AnnotationTargetProcessor(context, objectStack, parentPathEntry, typeResolver,
                context.getAugmentedIndex().getClass(type), type);
        return fp.processField();
    }

    @Override
    public void setRequired(AnnotationTarget target, String propertyKey) {
        List<String> requiredProperties = parentPathEntry.getSchema().getRequired();

        if (requiredProperties == null || !requiredProperties.contains(propertyKey)) {
            AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(context, target);

            if (schemaAnnotation == null ||
                    schemaAnnotation.value(SchemaConstant.PROP_REQUIRED) == null) {
                /*
                 * Only mark the schema as required in the parent schema if it has not
                 * already been specified.
                 */
                parentPathEntry.getSchema().addRequired(propertyKey);
            }
        }
    }

    /**
     * This method will generate a schema for the {@link #annotationTarget} containing one
     * of the following :
     *
     * <ol>
     * <li>A schema composed (using {@link Schema#allOf(List) allOf}) of a <code>$ref</code> to the schema of the
     * {@link #entityType}
     * and the schema attributes scanned or derived from the {@link #annotationTarget} itself that do not
     * generally apply to the {@link #entityType}'s schema such as a field-specific <code>description</code>.
     * </li>
     * <li>A schema containing a <code>$ref</code> to the schema of the {@link #entityType}. This occurs when
     * the field does not contribute any additional or different attributes that are not defined by the base
     * schema of the {@link #entityType}.
     * </li>
     * <li>A schema containing only the attributes scanned or derived from the {@link #annotationTarget} which will include
     * attributes
     * of the {@link #entityType} if it is not able to be registered via
     * {@link SchemaRegistry#registerReference(Type, TypeResolver, Schema) checkRegistration}.
     * </li>
     * </ol>
     *
     * @return the individual or composite schema for the annotationTarget used to create this {@link AnnotationTargetProcessor}
     */
    Schema processField() {
        final AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(context, annotationTarget);
        final String propertyKey = typeResolver.getPropertyName();
        final SchemaRegistry schemaRegistry = context.getSchemaRegistry();

        final TypeProcessor typeProcessor;
        final Schema typeSchema;
        final Type fieldType;
        final Type registrationType;
        final boolean registrationCandidate;

        if (schemaAnnotation != null && JandexUtil.hasImplementation(schemaAnnotation)) {
            typeProcessor = null;
            typeSchema = null;
            fieldType = context.annotations().value(schemaAnnotation, SchemaConstant.PROP_IMPLEMENTATION);
            registrationType = null;
            registrationCandidate = false;
        } else {
            // Process the type of the field to derive the typeSchema
            typeProcessor = new TypeProcessor(context, objectStack, parentPathEntry, typeResolver, entityType,
                    OASFactory.createSchema(), annotationTarget);

            // Type could be replaced (e.g. generics)
            fieldType = typeProcessor.processType();

            Schema initTypeSchema = typeProcessor.getSchema();

            // Set any default values that apply to the type schema as a result of the TypeProcessor
            if (!TypeUtil.isTypeOverridden(context, fieldType, schemaAnnotation)) {
                TypeUtil.applyTypeAttributes(fieldType, initTypeSchema);
            }

            // The registeredTypeSchema will be a reference to typeSchema if registration occurs
            registrationType = TypeUtil.isWrappedType(entityType) ? fieldType : entityType;
            registrationCandidate = !JandexUtil.isRef(schemaAnnotation) &&
                    schemaRegistry.register(registrationType, context.getJsonViews(), typeResolver,
                            initTypeSchema,
                            (reg, key) -> null) != initTypeSchema;

            if (registrationCandidate && schemaRegistry.hasSchema(registrationType, context.getJsonViews(), typeResolver)) {
                typeSchema = schemaRegistry
                        .lookupSchema(TypeResolver.resolve(registrationType, typeResolver), context.getJsonViews());
            } else {
                typeSchema = initTypeSchema;
            }
        }

        Schema fieldSchema;

        if (schemaAnnotation != null) {
            // Handle field annotated with @Schema.
            fieldSchema = readSchemaAnnotatedField(propertyKey, schemaAnnotation, fieldType);
        } else if (registrationCandidate) {
            // The type schema was registered, start with empty schema for the field using the type from the field type's schema
            fieldSchema = OASFactory.createSchema().type(typeSchema.getType());
        } else {
            // Use the type's schema for the field as a starting point (poor man's clone)
            fieldSchema = MergeUtil.mergeObjects(OASFactory.createSchema(), typeSchema);
        }

        Optional<BeanValidationScanner> constraintScanner = context.getBeanValidationScanner();

        if (constraintScanner.isPresent()) {
            for (AnnotationTarget contraintTarget : typeResolver.getConstraintTargets()) {
                // Apply constraints from all bean properties associated with the schema property
                constraintScanner.get().applyConstraints(contraintTarget, fieldSchema, propertyKey, this);
            }
        }

        if (SchemaSupport.getNullable(fieldSchema) == null && TypeUtil.isOptional(entityType)) {
            SchemaSupport.setNullable(fieldSchema, Boolean.TRUE);
        }

        if (fieldSchema.getReadOnly() == null && typeResolver.isReadOnly()) {
            fieldSchema.setReadOnly(Boolean.TRUE);
        }

        if (fieldSchema.getWriteOnly() == null && typeResolver.isWriteOnly()) {
            fieldSchema.setWriteOnly(Boolean.TRUE);
        }

        TypeUtil.mapDeprecated(context, annotationTarget, fieldSchema::getDeprecated, fieldSchema::setDeprecated);

        processFieldAnnotations(fieldSchema, typeResolver);

        Schema parentSchema = parentPathEntry.getSchema();
        Schema existingFieldSchema = ModelUtil.getPropertySchema(parentSchema, propertyKey);

        if (existingFieldSchema != null) {
            // Existing schema (from @SchemaProperty) overrides @Schema on field
            fieldSchema = MergeUtil.mergeObjects(fieldSchema, existingFieldSchema);
        }

        if (registrationCandidate) {
            if (fieldAssertionConflicts(fieldSchema, typeSchema)) {
                fieldSchema = SchemaFactory.includeTypeSchema(context, fieldSchema, fieldType);
            } else {
                typeProcessor.pushObjectStackInput();
                Schema registeredTypeSchema;
                List<Schema.SchemaType> typeList = typeSchema.getType();

                if (typeList != null && !typeList.contains(SchemaType.ARRAY)) {
                    // Only register a reference to the type schema. The full schema will be added by subsequent
                    // items on the stack (if not already present in the registry).
                    registeredTypeSchema = schemaRegistry.registerReference(registrationType, context.getJsonViews(),
                            typeResolver, typeSchema);
                } else {
                    // Allow registration of arrays since we may not encounter a List<CurrentType> again.
                    registeredTypeSchema = schemaRegistry.checkRegistration(registrationType, context.getJsonViews(),
                            typeResolver, typeSchema);
                }

                // Field schema may be replaced by a reference, so check now if it permits null
                boolean fieldSchemaNullable = isNullable(fieldSchema);

                if (fieldSchema.getRef() == null && (fieldAssertionsOverrideType(fieldSchema, typeSchema)
                        || fieldSpecifiesAnnotation(fieldSchema))) {
                    // Field declaration overrides a schema annotation (non-validating), add referenced type to `allOf` if not user-provided
                    TypeUtil.clearMatchingDefaultAttributes(fieldSchema, typeSchema);
                    fieldSchema.setRef(registeredTypeSchema.getRef());
                    SchemaSupport.addTypeObserver(typeSchema, fieldSchema);
                } else {
                    fieldSchema = registeredTypeSchema; // Reference to the type schema
                }

                // If the field should allow null but the type schema doesn't, use anyOf to allow null
                if (fieldSchemaNullable && !isNullable(typeSchema)) {
                    // Move reference to type into its own subschema
                    Schema refSchema = OASFactory.createSchema().ref(fieldSchema.getRef());
                    fieldSchema.setRef(null);
                    if (fieldSchema.getAnyOf() == null) {
                        fieldSchema.addAnyOf(refSchema).addAnyOf(SchemaSupport.nullSchema());
                    } else {
                        Schema anyOfSchema = OASFactory.createSchema().addAnyOf(refSchema)
                                .addAnyOf(SchemaSupport.nullSchema());
                        fieldSchema.addAllOf(anyOfSchema);
                    }
                }
            }
        } else if (!JandexUtil.isRef(schemaAnnotation)) {
            /*
             * Registration did not occur and the user did not indicate this schema is a simple reference,
             * overlay anything defined by the field on the type's schema
             */
            if (typeProcessor != null) {
                typeProcessor.pushObjectStackInput();
            }
            fieldSchema = MergeUtil.mergeObjects(typeSchema, fieldSchema);
        }

        parentSchema.addProperty(propertyKey, fieldSchema);

        return fieldSchema;
    }

    private boolean isNullable(Schema schema) {
        return Boolean.TRUE.equals(SchemaSupport.getNullable(schema));
    }

    private void processFieldAnnotations(Schema fieldSchema, TypeResolver typeResolver) {
        String name = typeResolver.getBeanPropertyName();

        for (AnnotationTarget target : Arrays.asList(typeResolver.getField(), typeResolver.getReadMethod(),
                typeResolver.getWriteMethod())) {
            if (target != null && processXmlAttr(name, fieldSchema, target)) {
                break;
            }
        }
    }

    private boolean processXmlAttr(String name, Schema fieldSchema, AnnotationTarget target) {
        AnnotationInstance xmlAttr = context.annotations().getAnnotation(target, XML_ATTRIBUTE);
        AnnotationInstance xmlElement = context.annotations().getAnnotation(target, XML_ELEMENT);
        AnnotationInstance xmlWrapper = context.annotations().getAnnotation(target, XML_WRAPPERELEMENT);

        if (xmlAttr == null && xmlWrapper == null && xmlElement == null) {
            return false;
        }

        if (xmlAttr != null) {
            setXmlIfEmpty(fieldSchema);
            fieldSchema.getXml().attribute(true);
            setXmlName(fieldSchema, name, xmlAttr);
        }

        if (xmlWrapper != null) {
            setXmlIfEmpty(fieldSchema);
            fieldSchema.getXml().wrapped(true);
            setXmlName(fieldSchema, name, xmlWrapper);
            if (xmlElement != null) {
                setXmlName(fieldSchema.getItems(), name, xmlElement);
                return true;
            }
        }

        if (xmlElement != null) {
            setXmlName(fieldSchema, name, xmlElement);
        }

        return true;
    }

    private void setXmlIfEmpty(Schema schema) {
        if (schema.getXml() != null) {
            return;
        }

        schema.setXml(OASFactory.createXML());
    }

    private void setXmlName(Schema fieldSchema, String realName, AnnotationInstance xmlAttr) {
        AnnotationValue name = xmlAttr.value(PROP_NAME);
        if (fieldSchema != null && name != null) {
            String annName = name.asString();
            if (!annName.equals(realName)) {
                setXmlIfEmpty(fieldSchema);
                fieldSchema.getXml().name(annName);
            }
        }
    }

    private Schema readSchemaAnnotatedField(String propertyKey, AnnotationInstance annotation, Type postProcessedField) {
        DataObjectLogging.logger.processingFieldAnnotation(annotation, propertyKey);

        // If "required" attribute is on field. It should be applied to the *parent* schema.
        // Required is false by default.
        if (context.annotations().value(annotation, SchemaConstant.PROP_REQUIRED, Boolean.FALSE).booleanValue()) {
            parentPathEntry.getSchema().addRequired(propertyKey);
        }

        // TypeFormat pair contains mappings for Java <-> OAS types and formats.
        // Provide inferred type and format if relevant.
        Map<String, Object> defaults;

        if (JandexUtil.isBooleanSchema(annotation)
                || JandexUtil.isArraySchema(context, annotation)
                || TypeUtil.isTypeOverridden(context, postProcessedField, annotation)) {
            defaults = Collections.emptyMap();
        } else {
            defaults = TypeUtil.getTypeAttributes(postProcessedField);
        }

        // readSchema *may* replace the existing schema, so we must assign.
        return SchemaFactory.readSchema(context, OASFactory.createSchema(), annotation, defaults);
    }

    boolean fieldAssertionConflicts(Schema fieldSchema, Schema typeSchema) {
        return SCHEMA_ASSERTION_PROVIDERS.stream()
                .map(provider -> {
                    Object fieldAttr = provider.apply(fieldSchema);

                    if (fieldAttr != null) {
                        Object typeAttr = provider.apply(typeSchema);

                        if (typeAttr != null && !fieldAttr.equals(typeAttr)) {
                            return true;
                        }
                    }

                    return false;
                })
                .anyMatch(Boolean.TRUE::equals);
    }

    boolean fieldAssertionsOverrideType(Schema fieldSchema, Schema typeSchema) {
        return SCHEMA_ASSERTION_PROVIDERS.stream()
                .map(provider -> {
                    Object fieldAttr = provider.apply(fieldSchema);

                    if (fieldAttr != null) {
                        return !fieldAttr.equals(provider.apply(typeSchema));
                    }

                    return false;
                })
                .anyMatch(Boolean.TRUE::equals);
    }

    boolean fieldSpecifiesAnnotation(Schema fieldSchema) {
        for (Function<Schema, Object> provider : SCHEMA_ANNOTATION_PROVIDERS) {
            if (provider.apply(fieldSchema) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.7.6
     */
    private static final List<Function<Schema, Object>> SCHEMA_ASSERTION_PROVIDERS = Arrays.asList(
            Schema::getAdditionalPropertiesSchema,
            Schema::getAllOf,
            Schema::getAnyOf,
            Schema::getDiscriminator,
            Schema::getEnumeration,
            Schema::getExclusiveMaximum,
            Schema::getExclusiveMinimum,
            Schema::getFormat,
            Schema::getItems,
            Schema::getMaximum,
            Schema::getMaxItems,
            Schema::getMaxLength,
            Schema::getMaxProperties,
            Schema::getMinimum,
            Schema::getMinItems,
            Schema::getMinLength,
            Schema::getMinProperties,
            Schema::getMultipleOf,
            Schema::getNot,
            Schema::getOneOf,
            Schema::getPattern,
            Schema::getProperties,
            Schema::getRef,
            Schema::getRequired,
            Schema::getUniqueItems,
            Schema::getXml);

    /**
     * @see https://json-schema.org/draft/2020-12/json-schema-core.html#annotations
     */
    @SuppressWarnings("deprecation")
    private static final List<Function<Schema, Object>> SCHEMA_ANNOTATION_PROVIDERS = Arrays.asList(
            Schema::getDefaultValue,
            Schema::getDeprecated,
            Schema::getDescription,
            Schema::getExample,
            Schema::getExamples,
            Schema::getExtensions,
            Schema::getExternalDocs,
            Schema::getReadOnly,
            Schema::getTitle,
            Schema::getWriteOnly);

}
