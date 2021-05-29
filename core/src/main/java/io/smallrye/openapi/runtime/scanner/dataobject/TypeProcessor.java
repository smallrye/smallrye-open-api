package io.smallrye.openapi.runtime.scanner.dataobject;

import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.ARRAY_TYPE_OBJECT;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.COLLECTION_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.ENUM_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.ITERABLE_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.MAP_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.OBJECT_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.SET_TYPE;
import static io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner.STRING_TYPE;
import static io.smallrye.openapi.runtime.util.TypeUtil.isTerminalType;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.util.MergeUtil;
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
            SchemaRegistry.checkRegistration(type, typeResolver, schema);
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

        if (type.kind() == Type.Kind.ARRAY) {
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

        // Raw Collection
        if (isA(type, COLLECTION_TYPE)) {
            return ARRAY_TYPE_OBJECT;
        }

        // Raw Iterable
        if (isA(type, ITERABLE_TYPE)) {
            return ARRAY_TYPE_OBJECT;
        }

        // Raw Map
        if (isA(type, MAP_TYPE)) {
            return OBJECT_TYPE;
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
        Schema itemSchema = new SchemaImpl();
        arraySchema.type(Schema.SchemaType.ARRAY);

        Type componentType = typeResolver.resolve(arrayType.component());
        boolean isOptional = TypeUtil.isOptional(componentType);

        if (isOptional) {
            componentType = TypeUtil.unwrapType(componentType);
        }

        // Only use component (excludes the special name formatting for arrays).
        TypeUtil.applyTypeAttributes(componentType, itemSchema);

        // If it's not a terminal type, then push for later inspection.
        if (!isTerminalType(componentType) && index.containsClass(componentType)) {
            pushToStack(componentType, itemSchema);
        }

        itemSchema = SchemaRegistry.registerReference(componentType, typeResolver, itemSchema);

        while (arrayType.dimensions() > 1) {
            Schema parentArrSchema = new SchemaImpl();
            parentArrSchema.setType(Schema.SchemaType.ARRAY);
            parentArrSchema.setItems(itemSchema);

            itemSchema = parentArrSchema;
            arrayType = ArrayType.create(arrayType.component(), arrayType.dimensions() - 1);
        }

        if (isOptional) {
            itemSchema = new SchemaImpl()
                    .addAllOf(itemSchema)
                    .addAllOf(new SchemaImpl().nullable(Boolean.TRUE));
        }

        arraySchema.setItems(itemSchema);

        return arrayType;
    }

    private Type readParameterizedType(ParameterizedType pType, Schema schema) {
        DataObjectLogging.logger.processingParametrizedType(pType);
        Type typeRead = pType;

        // If it's a collection, we should treat it as an array.
        if (isA(pType, COLLECTION_TYPE) || isA(pType, ITERABLE_TYPE)) {
            DataObjectLogging.logger.processingTypeAs("Java Collection", "Array");
            schema.type(Schema.SchemaType.ARRAY);
            ParameterizedType ancestorType = TypeResolver.resolveParameterizedAncestor(context, pType, ITERABLE_TYPE);

            if (TypeUtil.isA(context, pType, SET_TYPE)) {
                schema.setUniqueItems(Boolean.TRUE);
            }

            // Should only have one arg for collection.
            Type valueType = ancestorType.arguments().get(0);
            boolean isOptional = TypeUtil.isOptional(valueType);
            if (isOptional) {
                valueType = TypeUtil.unwrapType(valueType);
            }
            Schema valueSchema = readGenericValueType(valueType, schema);

            if (isOptional) {
                valueSchema = new SchemaImpl()
                        .addAllOf(valueSchema)
                        .addAllOf(new SchemaImpl().nullable(Boolean.TRUE));
            }

            schema.setItems(valueSchema);

            typeRead = ARRAY_TYPE_OBJECT; // Representing collection as JSON array
        } else if (isA(pType, MAP_TYPE)) {
            DataObjectLogging.logger.processingTypeAs("Map", "object");
            schema.type(Schema.SchemaType.OBJECT);
            ParameterizedType ancestorType = TypeResolver.resolveParameterizedAncestor(context, pType, MAP_TYPE);

            if (ancestorType.arguments().size() == 2) {
                Type valueType = ancestorType.arguments().get(1);
                // Add properties schema to field schema.
                schema.additionalPropertiesSchema(readGenericValueType(valueType, schema));
            }

            typeRead = OBJECT_TYPE;

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

    private Schema readGenericValueType(Type valueType, Schema schema) {
        Schema valueSchema = new SchemaImpl();

        if (isTerminalType(valueType)) {
            TypeUtil.applyTypeAttributes(valueType, valueSchema);
        } else if (valueType.kind() == Kind.PARAMETERIZED_TYPE) {
            readParameterizedType(valueType.asParameterizedType(), valueSchema);
        } else {
            valueSchema = resolveParameterizedType(valueType, schema, valueSchema);
        }

        return valueSchema;
    }

    private Schema resolveParameterizedType(Type valueType, Schema schema, Schema propsSchema) {
        if (valueType.kind() == Type.Kind.TYPE_VARIABLE ||
                valueType.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE ||
                valueType.kind() == Type.Kind.WILDCARD_TYPE) {
            Type resolved = resolveTypeVariable(propsSchema, valueType, true);
            if (index.containsClass(resolved)) {
                propsSchema.type(Schema.SchemaType.OBJECT);
                propsSchema = SchemaRegistry.registerReference(valueType, typeResolver, propsSchema);
            }
        } else if (index.containsClass(valueType)) {
            if (isA(valueType, ENUM_TYPE)) {
                DataObjectLogging.logger.processingEnum(type);
                propsSchema = SchemaFactory.enumToSchema(context, valueType);
                pushToStack(valueType, schema);
            } else {
                propsSchema.type(Schema.SchemaType.OBJECT);
                pushToStack(valueType, propsSchema);
            }

            propsSchema = SchemaRegistry.registerReference(valueType, typeResolver, propsSchema);
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
        objectStack.push(annotationTarget, parentPathEntry, type, schema);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(context, testSubject, test);
    }
}
