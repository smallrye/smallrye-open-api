package io.smallrye.openapi.runtime.scanner.dataobject;

import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.ARRAY_TYPE_OBJECT;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.ENUM_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.ITERABLE_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.MAP_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.SET_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.STREAM_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.STRING_TYPE;
import static io.smallrye.openapi.runtime.util.TypeUtil.isTerminalType;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Process {@link Type} instances.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeProcessor {

    private final Schema schema;
    private final AnnotationScannerContext context;
    private final AugmentedIndexView index;
    private final AnnotationTarget annotationTarget;
    private final DataObjectDeque objectStack;
    private final TypeResolver typeResolver;
    private final DataObjectDeque.PathEntry parentPathEntry;
    private final List<StackEntry> objectStackInput = new ArrayList<>();

    static class StackEntry {
        final Type type;
        final Schema schema;

        public StackEntry(Type type, Schema schema) {
            this.type = type;
            this.schema = schema;
        }
    }

    // Type may be changed.
    private Type type;

    public TypeProcessor(final AnnotationScannerContext context,
            DataObjectDeque objectStack,
            DataObjectDeque.PathEntry parentPathEntry, TypeResolver typeResolver,
            Type type,
            Schema schema,
            AnnotationTarget annotationTarget) {
        this.objectStack = objectStack;
        this.typeResolver = typeResolver;
        this.parentPathEntry = parentPathEntry;
        this.type = type;
        this.schema = schema;
        this.context = context;
        this.index = context.getAugmentedIndex();
        this.annotationTarget = annotationTarget;
    }

    public Schema getSchema() {
        return schema;
    }

    public Type processType() {
        // If it's a terminal type.
        if (isTerminalType(type)) {
            context.getSchemaRegistry().checkRegistration(type, context.getJsonViews(), typeResolver, schema);
            return type;
        }

        if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            type = TypeUtil.resolveWildcard(type.asWildcardType());
        }

        if (type.kind() == Type.Kind.TYPE_VARIABLE ||
                type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
            // Resolve type variable to real variable.
            type = resolveTypeVariable(schema, type, false);
        }

        if (TypeUtil.isWrappedType(type)) {
            // Unwrap and proceed using the wrapped type
            type = TypeUtil.unwrapType(type);
        }

        if (isArrayType(type, annotationTarget)) {
            return readArrayType(type.asArrayType(), this.schema);
        }

        if (isA(type, ENUM_TYPE) && index.containsClass(type)) {
            MergeUtil.mergeObjects(schema, SchemaFactory.enumToSchema(context, type));
            pushToStack(type, this.schema);
            return STRING_TYPE;
        }

        if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            // Parameterized type (e.g. Foo<A, B>)
            return readParameterizedType(type.asParameterizedType(), this.schema);
        }

        // Raw Iterable
        if (isA(type, ITERABLE_TYPE)) {
            return TypeResolver.resolveParameterizedAncestor(context, type, ITERABLE_TYPE)
                    .map(p -> readParameterizedType(p.asParameterizedType(), this.schema))
                    .orElse(ARRAY_TYPE_OBJECT);
        }

        // Raw Stream
        if (isA(type, STREAM_TYPE)) {
            return TypeResolver.resolveParameterizedAncestor(context, type, STREAM_TYPE)
                    .map(p -> readParameterizedType(p.asParameterizedType(), this.schema))
                    .orElse(ARRAY_TYPE_OBJECT);
        }

        // Raw Map
        if (isA(type, MAP_TYPE)) {
            return TypeResolver.resolveParameterizedAncestor(context, type, MAP_TYPE)
                    .map(p -> readParameterizedType(p.asParameterizedType(), this.schema))
                    .orElse(MAP_TYPE);
        }

        // Simple case: bare class or primitive type.
        if (index.containsClass(type)) {
            pushToStack(type, this.schema);
        } else {
            // If the type is not in Jandex then we don't have easy access to it.
            // Future work could consider separate code to traverse classes reachable from this classloader.
            DataObjectLogging.logger.typeNotInJandexIndex(type);
        }

        return type;
    }

    private Type readArrayType(ArrayType arrayType, Schema arraySchema) {
        DataObjectLogging.logger.processingArray(arrayType);

        // Array-type schema
        Schema itemSchema = OASFactory.createSchema();
        arraySchema.addType(Schema.SchemaType.ARRAY);

        Type componentType = typeResolver.resolve(arrayType.constituent());
        boolean isOptional = TypeUtil.isOptional(componentType);

        if (isOptional) {
            componentType = TypeUtil.unwrapType(componentType);
        }

        // Only use component (excludes the special name formatting for arrays).
        TypeUtil.applyTypeAttributes(componentType, itemSchema);

        if (!isTerminalType(componentType) && index.containsClass(componentType)) {
            // If it's not a terminal type, then push for later inspection.
            pushToStack(componentType, itemSchema);
            itemSchema = context.getSchemaRegistry().registerReference(componentType, context.getJsonViews(), typeResolver,
                    itemSchema);
        } else {
            // Otherwise, allow registration since we may not encounter the array's element type again.
            itemSchema = context.getSchemaRegistry().checkRegistration(componentType, context.getJsonViews(), typeResolver,
                    itemSchema);
        }

        while (arrayType.dimensions() > 1) {
            Schema parentArrSchema = OASFactory.createSchema();
            parentArrSchema.addType(Schema.SchemaType.ARRAY);
            parentArrSchema.setItems(itemSchema);

            itemSchema = parentArrSchema;
            arrayType = ArrayType.create(arrayType.constituent(), arrayType.dimensions() - 1);
        }

        if (isOptional) {
            itemSchema = wrapOptionalItemSchema(itemSchema);
        }

        arraySchema.setItems(itemSchema);

        return arrayType;
    }

    private Type readParameterizedType(ParameterizedType pType, Schema schema) {
        DataObjectLogging.logger.processingParametrizedType(pType);
        Type typeRead = pType;
        Type seekType = resolveSeekType(pType);

        // If it's a collection, iterable, or a stream, we should treat it as an array.
        if (seekType != null && seekType != MAP_TYPE) {
            DataObjectLogging.logger.processingTypeAs("Java Iterable or Stream", "Array");
            SchemaSupport.setType(schema, Schema.SchemaType.ARRAY);
            ParameterizedType ancestorType = TypeResolver.resolveParameterizedAncestor(context, pType, seekType)
                    .orElse(pType);

            if (TypeUtil.isA(context, pType, SET_TYPE)) {
                schema.setUniqueItems(Boolean.TRUE);
            }

            // Should only have one argument for Iterable and Stream uses first argument of BaseStream.
            Type valueType = ancestorType.arguments().get(0);
            boolean isOptional = TypeUtil.isOptional(valueType);
            if (isOptional) {
                valueType = TypeUtil.unwrapType(valueType);
            }
            Schema valueSchema = readGenericValueType(valueType);

            if (isOptional) {
                valueSchema = wrapOptionalItemSchema(valueSchema);
            }

            schema.setItems(valueSchema);

            typeRead = ARRAY_TYPE_OBJECT; // Representing collection as JSON array
        } else if (seekType == MAP_TYPE) {
            DataObjectLogging.logger.processingTypeAs("Map", "object");
            SchemaSupport.setType(schema, Schema.SchemaType.OBJECT);
            ParameterizedType ancestorType = TypeResolver.resolveParameterizedAncestor(context, pType, seekType)
                    .orElse(pType);

            if (ancestorType.arguments().size() == 2) {
                Type valueType = ancestorType.arguments().get(1);
                // Add properties schema to field schema.
                schema.additionalPropertiesSchema(readGenericValueType(valueType));
            }

            typeRead = MAP_TYPE;

            if (TypeUtil.allowRegistration(context, pType)) {
                // This type will be inspected later, if necessary.
                pushResolvedToStack(pType, schema);
            }
        } else if (index.containsClass(pType)) {
            // This type will be inspected later, if necessary.
            pushResolvedToStack(pType, schema);
        }

        return typeRead;
    }

    private Type resolveSeekType(ParameterizedType pType) {
        if (isA(pType, ITERABLE_TYPE)) {
            return ITERABLE_TYPE;
        }
        if (isA(pType, MAP_TYPE)) {
            return MAP_TYPE;
        }
        if (isA(pType, STREAM_TYPE)) {
            return STREAM_TYPE;
        }
        return null;
    }

    private static Schema wrapOptionalItemSchema(Schema itemSchema) {
        return OASFactory.createSchema()
                .addAnyOf(SchemaSupport.nullSchema())
                .addAnyOf(itemSchema);
    }

    private Schema readGenericValueType(Type valueType) {
        Schema valueSchema = OASFactory.createSchema();

        if (isTerminalType(valueType)) {
            TypeUtil.applyTypeAttributes(valueType, valueSchema);
        } else if (valueType.kind() == Kind.PARAMETERIZED_TYPE) {
            readParameterizedType(valueType.asParameterizedType(), valueSchema);
        } else {
            valueSchema = resolveParameterizedType(valueType, valueSchema);
        }

        return valueSchema;
    }

    private Schema resolveParameterizedType(Type valueType, Schema propsSchema) {
        if (valueType.kind() == Type.Kind.TYPE_VARIABLE ||
                valueType.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE ||
                valueType.kind() == Type.Kind.WILDCARD_TYPE) {
            Type resolved = resolveTypeVariable(propsSchema, valueType, true);
            if (index.containsClass(resolved)) {
                SchemaSupport.setType(propsSchema, Schema.SchemaType.OBJECT);
                propsSchema = context.getSchemaRegistry().registerReference(valueType, context.getJsonViews(),
                        typeResolver,
                        propsSchema);
            }
        } else if (index.containsClass(valueType)) {
            if (isA(valueType, ENUM_TYPE)) {
                DataObjectLogging.logger.processingEnum(type);
                propsSchema = SchemaFactory.enumToSchema(context, valueType);
            } else {
                SchemaSupport.setType(propsSchema, Schema.SchemaType.OBJECT);
            }

            SchemaRegistry registry = context.getSchemaRegistry();

            if (registry.hasSchema(valueType, context.getJsonViews(), typeResolver)) {
                propsSchema = registry.lookupRef(valueType, context.getJsonViews());
            } else {
                pushToStack(valueType, propsSchema);
                propsSchema = registry.registerReference(valueType, context.getJsonViews(), typeResolver,
                        propsSchema);
            }
        }

        return propsSchema;
    }

    private Type resolveTypeVariable(Schema schema, Type fieldType, boolean pushToStack) {
        // Type variable (e.g. A in Foo<A>)
        Type resolvedType = typeResolver.resolve(fieldType);
        DataObjectLogging.logger.resolvedType(fieldType, resolvedType);

        if (isTerminalType(resolvedType) || !index.containsClass(resolvedType)) {
            DataObjectLogging.logger.terminalType(resolvedType);
            TypeUtil.applyTypeAttributes(resolvedType, schema);
        } else if (pushToStack) {
            // Add resolved type to stack.
            pushToStack(resolvedType, schema);
        }

        return resolvedType;
    }

    private void pushResolvedToStack(Type type, Schema schema) {
        Type resolvedType = this.typeResolver.resolve(type);
        pushToStack(resolvedType, schema);
    }

    private void pushToStack(Type type, Schema schema) {
        objectStackInput.add(new StackEntry(type, schema));
    }

    public void pushObjectStackInput() {
        objectStackInput.forEach(e -> objectStack.push(annotationTarget, parentPathEntry, e.type, e.schema));
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(context, testSubject, test);
    }

    private boolean isArrayType(Type type, AnnotationTarget annotationTarget) {
        if (type.kind() != Kind.ARRAY) {
            return false;
        }

        final AnnotationInstance annotation = TypeUtil.getSchemaAnnotation(context, annotationTarget);

        if (annotation != null) {
            Schema.SchemaType schemaType = context.annotations().enumValue(annotation, SchemaConstant.PROP_TYPE,
                    Schema.SchemaType.class);

            if (schemaType != null) {
                return schemaType == Schema.SchemaType.ARRAY;
            }
        }

        return true;
    }
}
