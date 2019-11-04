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
package io.smallrye.openapi.runtime.scanner.dataobject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner.RequirementHandler;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Process annotation targets such as {@link FieldInfo}.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class AnnotationTargetProcessor implements RequirementHandler {
    private static final Logger LOG = Logger.getLogger(AnnotationTargetProcessor.class);

    private final AugmentedIndexView index;
    private final DataObjectDeque objectStack;
    private final DataObjectDeque.PathEntry parentPathEntry;
    private final TypeResolver typeResolver;
    private final Type entityType;

    // May be null if field is unannotated.
    private AnnotationTarget annotationTarget;

    public AnnotationTargetProcessor(AugmentedIndexView index,
            DataObjectDeque objectStack,
            DataObjectDeque.PathEntry parentPathEntry,
            TypeResolver typeResolver,
            AnnotationTarget annotationTarget,
            Type entityType) {

        this.index = index;
        this.objectStack = objectStack;
        this.parentPathEntry = parentPathEntry;
        this.typeResolver = typeResolver;
        this.entityType = entityType;
        this.annotationTarget = annotationTarget;
    }

    public static Schema process(AugmentedIndexView index,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry) {

        AnnotationTargetProcessor fp = new AnnotationTargetProcessor(index, objectStack, parentPathEntry, typeResolver,
                typeResolver.getAnnotationTarget(), typeResolver.getUnresolvedType());
        return fp.processField();
    }

    public static Schema process(AugmentedIndexView index,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry,
            Type type) {
        AnnotationTargetProcessor fp = new AnnotationTargetProcessor(index, objectStack, parentPathEntry, typeResolver,
                index.getClass(type), type);
        return fp.processField();
    }

    @Override
    public void setRequired(AnnotationTarget target, String propertyKey) {
        List<String> requiredProperties = parentPathEntry.getSchema().getRequired();

        if (requiredProperties == null || !requiredProperties.contains(propertyKey)) {
            AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(target);

            if (schemaAnnotation == null ||
                    schemaAnnotation.value(OpenApiConstants.PROP_REQUIRED) == null) {
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
     * {@link SchemaRegistry#checkRegistration(Type, TypeResolver, Schema) checkRegistration}.
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
            fieldType = JandexUtil.value(schemaAnnotation, OpenApiConstants.PROP_IMPLEMENTATION);
        } else {
            // Process the type of the field to derive the typeSchema
            TypeProcessor typeProcessor = new TypeProcessor(index, objectStack, parentPathEntry, typeResolver, entityType,
                    new SchemaImpl(), annotationTarget);

            // Type could be replaced (e.g. generics)
            fieldType = typeProcessor.processType();

            typeSchema = typeProcessor.getSchema();

            // Set any default values that apply to the type schema as a result of the TypeProcessor
            TypeUtil.applyTypeAttributes(fieldType, typeSchema);

            // The registeredTypeSchema will be a reference to typeSchema if registration occurs
            registeredTypeSchema = SchemaRegistry.checkRegistration(entityType, typeResolver, typeSchema);
        }

        Schema fieldSchema;

        if (schemaAnnotation != null) {
            // Handle field annotated with @Schema.
            fieldSchema = readSchemaAnnotatedField(propertyKey, schemaAnnotation, fieldType);
        } else {
            // Use the type's schema for the field as a starting point
            fieldSchema = typeSchema;
        }

        BeanValidationScanner.applyConstraints(annotationTarget, fieldSchema, propertyKey, this);

        // Only when registration was successful (ref is present and the registered type is a different instance)
        if (typeSchema != registeredTypeSchema && registeredTypeSchema.getRef() != null) {
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

    private Schema readSchemaAnnotatedField(String propertyKey, AnnotationInstance annotation, Type postProcessedField) {
        LOG.debugv("Processing @Schema annotation {0} on a field {1}", annotation, propertyKey);

        // If "required" attribute is on field. It should be applied to the *parent* schema.
        // Required is false by default.
        if (JandexUtil.booleanValueWithDefault(annotation, OpenApiConstants.PROP_REQUIRED)) {
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
        return SchemaFactory.readSchema(index, new SchemaImpl(), annotation, defaults);
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
