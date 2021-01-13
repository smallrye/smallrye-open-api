package io.smallrye.openapi.runtime.scanner.dataobject;

import static io.smallrye.openapi.api.constants.JaxbConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.*;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.media.XMLImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner.RequirementHandler;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
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
            AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(target);

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
        final AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(annotationTarget);
        final String propertyKey = typeResolver.getPropertyName();

        final Schema typeSchema;
        final Schema registeredTypeSchema;
        final Type fieldType;

        if (schemaAnnotation != null && JandexUtil.hasImplementation(schemaAnnotation)) {
            typeSchema = null;
            registeredTypeSchema = null;
            fieldType = JandexUtil.value(schemaAnnotation, SchemaConstant.PROP_IMPLEMENTATION);
        } else {
            // Process the type of the field to derive the typeSchema
            TypeProcessor typeProcessor = new TypeProcessor(context, objectStack, parentPathEntry, typeResolver, entityType,
                    new SchemaImpl(), annotationTarget);

            // Type could be replaced (e.g. generics)
            fieldType = typeProcessor.processType();

            typeSchema = typeProcessor.getSchema();

            // Set any default values that apply to the type schema as a result of the TypeProcessor
            TypeUtil.applyTypeAttributes(fieldType, typeSchema);

            // The registeredTypeSchema will be a reference to typeSchema if registration occurs
            Type registrationType = TypeUtil.isOptional(entityType) ? fieldType : entityType;

            if (typeSchema.getType() != SchemaType.ARRAY) {
                // Only register a reference to the type schema. The full schema will be added by subsequent
                // items on the stack (if not already present in the registry).
                registeredTypeSchema = SchemaRegistry.registerReference(registrationType, typeResolver, typeSchema);
            } else {
                // Allow registration of arrays since we may not encounter a List<CurrentType> again.
                registeredTypeSchema = SchemaRegistry.checkRegistration(registrationType, typeResolver, typeSchema);
            }
        }

        Schema fieldSchema;

        if (schemaAnnotation != null) {
            // Handle field annotated with @Schema.
            fieldSchema = readSchemaAnnotatedField(propertyKey, schemaAnnotation, fieldType);
        } else if (registrationSuccessful(typeSchema, registeredTypeSchema)) {
            // The type schema was registered, start with empty schema for the field using the type from the field type's schema
            fieldSchema = new SchemaImpl().type(typeSchema.getType());
        } else {
            // Use the type's schema for the field as a starting point (poor man's clone)
            fieldSchema = MergeUtil.mergeObjects(new SchemaImpl(), typeSchema);
        }

        BeanValidationScanner.applyConstraints(annotationTarget, fieldSchema, propertyKey, this);

        if (fieldSchema.getNullable() == null && TypeUtil.isOptional(entityType)) {
            fieldSchema.setNullable(Boolean.TRUE);
        }

        processFieldAnnotations(fieldSchema, typeResolver);

        // Only when registration was successful (ref is present and the registered type is a different instance)
        if (registrationSuccessful(typeSchema, registeredTypeSchema)) {
            // Check if the field specifies something additional or different from the type's schema
            if (fieldOverridesType(fieldSchema, typeSchema)) {
                TypeUtil.clearMatchingDefaultAttributes(fieldSchema, typeSchema); // Remove duplicates
                Schema composition = new SchemaImpl();
                composition.addAllOf(registeredTypeSchema); // Reference to the type schema
                composition.addAllOf(fieldSchema);
                fieldSchema = composition;
            } else {
                fieldSchema = registeredTypeSchema; // Reference to the type schema
            }
        } else {
            // Registration did not occur, overlay anything defined by the field on the type's schema
            fieldSchema = MergeUtil.mergeObjects(typeSchema, fieldSchema);
        }

        parentPathEntry.getSchema().addProperty(propertyKey, fieldSchema);
        return fieldSchema;
    }

    private void processFieldAnnotations(Schema fieldSchema, TypeResolver typeResolver) {
        String name = typeResolver.getBeanPropertyName();
        FieldInfo field = typeResolver.getField();
        if (field != null) {
            if (processXmlAttr(name,
                    fieldSchema,
                    field.annotation(XML_ATTRIBUTE),
                    field.annotation(XML_ELEMENT),
                    field.annotation(XML_WRAPPERELEMENT))) {
                return;
            }
        }
        MethodInfo readMethod = typeResolver.getReadMethod();
        if (readMethod != null) {
            if (processXmlAttr(name,
                    fieldSchema,
                    readMethod.annotation(XML_ATTRIBUTE),
                    readMethod.annotation(XML_ELEMENT),
                    readMethod.annotation(XML_WRAPPERELEMENT))) {
                return;
            }
        }
        MethodInfo writeMethod = typeResolver.getWriteMethod();
        if (writeMethod != null) {
            if (processXmlAttr(name,
                    fieldSchema,
                    writeMethod.annotation(XML_ATTRIBUTE),
                    writeMethod.annotation(XML_ELEMENT),
                    writeMethod.annotation(XML_WRAPPERELEMENT))) {
                return;
            }
        }
    }

    private boolean processXmlAttr(
            String name,
            Schema fieldSchema,
            AnnotationInstance xmlAttr,
            AnnotationInstance xmlElement,
            AnnotationInstance xmlWrapper) {
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
        schema.setXml(new XMLImpl());
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

    /**
     * A successful registration results in the registered type schema being a distinct
     * Schema instance containing only a <code>ref</code> to the original type schema.
     *
     * @param typeSchema schema for a type
     * @param registeredTypeSchema a (potential) reference schema to typeSchema
     * @return true if the schemas are not the same (i.e. registration occurred), otherwise false
     */
    private boolean registrationSuccessful(Schema typeSchema, Schema registeredTypeSchema) {
        return (typeSchema != registeredTypeSchema);
    }

    private Schema readSchemaAnnotatedField(String propertyKey, AnnotationInstance annotation, Type postProcessedField) {
        DataObjectLogging.logger.processingFieldAnnotation(annotation, propertyKey);

        // If "required" attribute is on field. It should be applied to the *parent* schema.
        // Required is false by default.
        if (JandexUtil.booleanValueWithDefault(annotation, SchemaConstant.PROP_REQUIRED)) {
            parentPathEntry.getSchema().addRequired(propertyKey);
        }

        // TypeFormat pair contains mappings for Java <-> OAS types and formats.
        // Provide inferred type and format if relevant.
        Map<String, Object> defaults;

        if (JandexUtil.isArraySchema(annotation)) {
            defaults = Collections.emptyMap();
        } else {
            defaults = TypeUtil.getTypeAttributes(postProcessedField);
        }

        // readSchema *may* replace the existing schema, so we must assign.
        return SchemaFactory.readSchema(context, new SchemaImpl(), annotation, defaults);
    }

    /**
     * Determine if the fieldSchema defines any attributes that are not present or
     * different from the attributes in the typeSchema.
     *
     * @param fieldSchema
     * @param typeSchema
     * @return true if fieldSchema defines new attributes or different attributes from typeSchema, otherwise false
     */
    boolean fieldOverridesType(Schema fieldSchema, Schema typeSchema) {
        List<Supplier<Object>> typeAttributes = getAttributeSuppliers(typeSchema);
        List<Supplier<Object>> fieldAttributes = getAttributeSuppliers(fieldSchema);

        for (int i = 0, m = typeAttributes.size(); i < m; i++) {
            Object fieldAttr = fieldAttributes.get(i).get();

            if (fieldAttr != null) {
                Object typeAttr = typeAttributes.get(i).get();

                if (typeAttr == null || !fieldAttr.equals(typeAttr)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get accessors/suppliers for all schema attributes that are relevant to comparing two
     * schemas.
     *
     * @param schema the schema
     * @return a list of suppliers (i.e. getters) for the schema's attributes
     */
    List<Supplier<Object>> getAttributeSuppliers(Schema schema) {
        return Arrays.asList(schema::getAdditionalPropertiesBoolean,
                schema::getAdditionalPropertiesSchema,
                schema::getAllOf,
                schema::getAnyOf,
                schema::getDefaultValue,
                schema::getDeprecated,
                schema::getDescription,
                schema::getDiscriminator,
                schema::getEnumeration,
                schema::getExample,
                schema::getExclusiveMaximum,
                schema::getExclusiveMinimum,
                schema::getExtensions,
                schema::getExternalDocs,
                schema::getFormat,
                schema::getItems,
                schema::getMaximum,
                schema::getMaxItems,
                schema::getMaxLength,
                schema::getMaxProperties,
                schema::getMinimum,
                schema::getMinItems,
                schema::getMinLength,
                schema::getMinProperties,
                schema::getMultipleOf,
                schema::getNot,
                schema::getNullable,
                schema::getOneOf,
                schema::getPattern,
                schema::getProperties,
                schema::getReadOnly,
                schema::getRef,
                schema::getRequired,
                schema::getTitle,
                schema::getType,
                schema::getUniqueItems,
                schema::getWriteOnly,
                schema::getXml);
    }
}
