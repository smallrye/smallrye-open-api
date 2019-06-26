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

import static io.smallrye.openapi.runtime.util.TypeUtil.getSchemaAnnotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.media.SchemaImpl;
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
    private final String entityName;
    private final Type entityType;

    // This can be overridden.
    private Schema fieldSchema;
    // May be null if field is unannotated.
    private AnnotationTarget annotationTarget;

    public AnnotationTargetProcessor(AugmentedIndexView index,
            DataObjectDeque objectStack,
            DataObjectDeque.PathEntry parentPathEntry,
            TypeResolver typeResolver,
            AnnotationTarget annotationTarget,
            String entityName,
            Type entityType) {
        this.index = index;
        this.objectStack = objectStack;
        this.parentPathEntry = parentPathEntry;
        this.typeResolver = typeResolver;
        this.entityName = entityName;
        this.entityType = entityType;
        this.annotationTarget = annotationTarget;
        this.fieldSchema = new SchemaImpl();
    }

    public AnnotationTargetProcessor(AugmentedIndexView index,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry,
            FieldInfo fieldInfo) {
        this(index, objectStack, parentPathEntry, typeResolver, fieldInfo, fieldInfo.name(), fieldInfo.type());
    }

    public AnnotationTargetProcessor(AugmentedIndexView index,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry,
            Type type) {
        this(index, objectStack, parentPathEntry, typeResolver, index.getClass(type), type.name().toString(), type);
    }

    public static Schema process(AugmentedIndexView index,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry,
            FieldInfo field) {
        AnnotationTargetProcessor fp = new AnnotationTargetProcessor(index, objectStack, typeResolver, parentPathEntry, field);
        return fp.processField();
    }

    public static Schema process(AugmentedIndexView index,
            DataObjectDeque objectStack,
            TypeResolver typeResolver,
            DataObjectDeque.PathEntry parentPathEntry,
            Type type) {
        AnnotationTargetProcessor fp = new AnnotationTargetProcessor(index, objectStack, typeResolver, parentPathEntry, type);
        return fp.processField();
    }

    @Override
    public void setRequired(AnnotationTarget target, String propertyKey) {
        List<String> requiredProperties = parentPathEntry.getSchema().getRequired();

        if (requiredProperties == null || !requiredProperties.contains(propertyKey)) {
            AnnotationInstance schemaAnnotation = getSchemaAnnotation(target);

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

    Schema processField() {
        AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(annotationTarget);

        final String propertyKey = readPropertyKey(schemaAnnotation);

        if (schemaAnnotation == null) {
            // Handle unannotated field and just do simple inference.
            if (shouldInferUnannotatedFields()) {
                readUnannotatedField();
            }
        } else {
            // Handle field annotated with @Schema.
            readSchemaAnnotatedField(propertyKey, schemaAnnotation);
        }

        BeanValidationScanner.applyConstraints(annotationTarget, fieldSchema, propertyKey, this);
        fieldSchema = SchemaRegistry.checkRegistration(entityType, typeResolver, fieldSchema);
        parentPathEntry.getSchema().addProperty(propertyKey, fieldSchema);
        return fieldSchema;
    }

    private String readPropertyKey(AnnotationInstance schemaAnnotation) {
        String key = null;

        if (schemaAnnotation != null) {
            key = JandexUtil.stringValue(schemaAnnotation, OpenApiConstants.PROP_NAME);
        }

        if (key == null) {
            AnnotationInstance jsonbAnnotation = TypeUtil.getAnnotation(annotationTarget,
                    OpenApiConstants.DOTNAME_JSONB_PROPERTY);
            if (jsonbAnnotation != null) {
                key = JandexUtil.stringValue(jsonbAnnotation, OpenApiConstants.PROP_VALUE);

                if (key == null) {
                    key = entityName;
                }
            } else {
                key = entityName;
            }
        }

        return key;
    }

    private void readSchemaAnnotatedField(String propertyKey, AnnotationInstance annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Annotation must not be null");
        }

        LOG.debugv("Processing @Schema annotation {0} on a field {1}", annotation, propertyKey);

        // If "required" attribute is on field. It should be applied to the *parent* schema.
        // Required is false by default.
        if (JandexUtil.booleanValueWithDefault(annotation, OpenApiConstants.PROP_REQUIRED)) {
            parentPathEntry.getSchema().addRequired(propertyKey);
        }

        // Type could be replaced (e.g. generics).
        TypeProcessor typeProcessor = new TypeProcessor(index, objectStack, parentPathEntry, typeResolver, entityType,
                fieldSchema, annotationTarget);

        Type postProcessedField = typeProcessor.processType();
        fieldSchema = typeProcessor.getSchema();

        // TypeFormat pair contains mappings for Java <-> OAS types and formats.
        TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(postProcessedField);

        // Provide inferred type and format if relevant.
        Map<String, Object> overrides = new HashMap<>();
        overrides.put(OpenApiConstants.PROP_TYPE, typeFormat.getSchemaType());
        overrides.put(OpenApiConstants.PROP_FORMAT, typeFormat.getFormat().format());
        // readSchema *may* replace the existing schema, so we must assign.
        this.fieldSchema = SchemaFactory.readSchema(index, fieldSchema, annotation, overrides);
    }

    private void readUnannotatedField() {
        LOG.debugv("Processing unannotated field {0}", entityType);

        TypeProcessor typeProcessor = new TypeProcessor(index, objectStack, parentPathEntry, typeResolver, entityType,
                fieldSchema, annotationTarget);

        Type postProcessedField = typeProcessor.processType();
        fieldSchema = typeProcessor.getSchema();

        TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(postProcessedField);
        fieldSchema.setType(typeFormat.getSchemaType());

        if (typeFormat.getFormat().hasFormat()) {
            fieldSchema.setFormat(typeFormat.getFormat().format());
        }
    }

    private boolean shouldInferUnannotatedFields() {
        String infer = System.getProperties().getProperty("openapi.infer-unannotated-types", "true");
        return Boolean.parseBoolean(infer);
    }
}
