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

        if (type.kind() == Type.Kind.ARRAY) {
            readArrayType(type.asArrayType(), this.schema);
        }

        if (TypeUtil.isWrappedType(type)) {
            return readWrappedType(type, this.schema);
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

        // Only use component (excludes the special name formatting for arrays).
        TypeUtil.applyTypeAttributes(arrayType.component(), itemSchema);

        // If it's not a terminal type, then push for later inspection.
        if (!isTerminalType(arrayType.component()) && index.containsClass(arrayType)) {
            pushToStack(arrayType, itemSchema);
        }

        itemSchema = SchemaRegistry.registerReference(arrayType.component(), typeResolver, itemSchema);

        while (arrayType.dimensions() > 1) {
            Schema parentArrSchema = new SchemaImpl();
            parentArrSchema.setType(Schema.SchemaType.ARRAY);
            parentArrSchema.setItems(itemSchema);

            itemSchema = parentArrSchema;
            arrayType = ArrayType.create(arrayType.component(), arrayType.dimensions() - 1);
        }

        arraySchema.setItems(itemSchema);

        return arrayType;
    }

    private Type readWrappedType(Type wrapperType, Schema schema) {
        Type wrappedType = TypeUtil.unwrapType(wrapperType);

        if (!isTerminalType(wrappedType) && index.containsClass(wrappedType)) {
            pushToStack(wrappedType, schema);
        }

        return wrappedType;
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
            schema.setItems(readGenericValueType(valueType, schema));

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
                pushToStack(pType, schema);
            }
        } else if (index.containsClass(pType)) {
            // This type will be resolved later, if necessary.
            pushToStack(pType, schema);
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
        Type resolvedType = typeResolver.getResolvedType(fieldType);
        DataObjectLogging.logger.resolvedType(fieldType, resolvedType);

        if (isTerminalType(resolvedType) || !index.containsClass(resolvedType)) {
            DataObjectLogging.logger.terminalType(resolvedType);
            TypeUtil.applyTypeAttributes(resolvedType, schema);
        } else if (pushToStack) {
            // Add resolved type to stack.
            objectStack.push(annotationTarget, parentPathEntry, resolvedType, schema);
        }

        return resolvedType;
    }

    private void pushToStack(Type type, Schema schema) {
        objectStack.push(annotationTarget, parentPathEntry, type, schema);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(context, testSubject, test);
    }
}
