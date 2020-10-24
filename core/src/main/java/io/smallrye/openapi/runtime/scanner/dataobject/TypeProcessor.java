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

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Process {@link Type} instances.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeProcessor {

    private final Schema schema;
    private final AugmentedIndexView index;
    private final ClassLoader cl;
    private final AnnotationTarget annotationTarget;
    private final DataObjectDeque objectStack;
    private final TypeResolver typeResolver;
    private final DataObjectDeque.PathEntry parentPathEntry;

    // Type may be changed.
    private Type type;

    public TypeProcessor(AugmentedIndexView index,
            ClassLoader cl,
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
        this.index = index;
        this.cl = cl;
        this.annotationTarget = annotationTarget;
    }

    public Schema getSchema() {
        return schema;
    }

    public Type processType() {
        // If it's a terminal type.
        if (isTerminalType(type)) {
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
            DataObjectLogging.log.processingArray(type);
            ArrayType arrayType = type.asArrayType();

            // Array-type schema
            Schema arrSchema = new SchemaImpl();
            schema.type(Schema.SchemaType.ARRAY);

            // Only use component (excludes the special name formatting for arrays).
            TypeUtil.applyTypeAttributes(arrayType.component(), arrSchema);

            // If it's not a terminal type, then push for later inspection.
            if (!isTerminalType(arrayType.component()) && index.containsClass(type)) {
                pushToStack(type, arrSchema);
            }

            arrSchema = SchemaRegistry.checkRegistration(arrayType.component(), typeResolver, arrSchema);

            while (arrayType.dimensions() > 1) {
                Schema parentArrSchema = new SchemaImpl();
                parentArrSchema.setType(Schema.SchemaType.ARRAY);
                parentArrSchema.setItems(arrSchema);

                arrSchema = parentArrSchema;
                arrayType = ArrayType.create(arrayType.component(), arrayType.dimensions() - 1);
            }

            schema.setItems(arrSchema);

            return arrayType;
        }

        if (TypeUtil.isOptional(type)) {
            Type optType = TypeUtil.getOptionalType(type);
            if (!isTerminalType(optType) && index.containsClass(optType)) {
                pushToStack(optType);
            }
            return optType;
        }

        if (isA(type, ENUM_TYPE) && index.containsClass(type)) {
            MergeUtil.mergeObjects(schema, SchemaFactory.enumToSchema(index, cl, type));
            return STRING_TYPE;
        }

        if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            // Parameterized type (e.g. Foo<A, B>)
            return readParameterizedType(type.asParameterizedType());
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
            pushToStack(type);
        } else {
            // If the type is not in Jandex then we don't have easy access to it.
            // Future work could consider separate code to traverse classes reachable from this classloader.
            DataObjectLogging.log.typeNotInJandexIndex(type);
        }

        return type;
    }

    private Type readParameterizedType(ParameterizedType pType) {
        DataObjectLogging.log.processingParametrizedType(pType);
        Type typeRead = pType;

        // If it's a collection, we should treat it as an array.
        if (isA(pType, COLLECTION_TYPE) || isA(pType, ITERABLE_TYPE)) {
            DataObjectLogging.log.processingTypeAs("Java Collection", "Array");
            Schema arraySchema = new SchemaImpl();
            schema.type(Schema.SchemaType.ARRAY);

            if (TypeUtil.isA(index, cl, pType, SET_TYPE)) {
                schema.setUniqueItems(Boolean.TRUE);
            }

            // Should only have one arg for collection.
            Type arg = pType.arguments().get(0);

            if (isTerminalType(arg)) {
                TypeUtil.applyTypeAttributes(arg, arraySchema);
            } else {
                arraySchema = resolveParameterizedType(arg, arraySchema);
            }

            schema.setItems(arraySchema);

            typeRead = ARRAY_TYPE_OBJECT; // Representing collection as JSON array
        } else if (isA(pType, MAP_TYPE)) {
            DataObjectLogging.log.processingTypeAs("Map", "object");
            schema.type(Schema.SchemaType.OBJECT);

            if (pType.arguments().size() == 2) {
                Type valueType = pType.arguments().get(1);
                Schema propsSchema = new SchemaImpl();
                if (isTerminalType(valueType)) {
                    TypeUtil.applyTypeAttributes(valueType, propsSchema);
                } else {
                    propsSchema = resolveParameterizedType(valueType, propsSchema);
                }
                // Add properties schema to field schema.
                schema.additionalPropertiesSchema(propsSchema);
            }
            typeRead = OBJECT_TYPE;
        } else if (index.containsClass(type)) {
            // This type will be resolved later, if necessary.
            pushToStack(pType);
        }

        return typeRead;
    }

    private Schema resolveParameterizedType(Type valueType, Schema propsSchema) {
        if (valueType.kind() == Type.Kind.TYPE_VARIABLE ||
                valueType.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE ||
                valueType.kind() == Type.Kind.WILDCARD_TYPE) {
            Type resolved = resolveTypeVariable(propsSchema, valueType, true);
            if (index.containsClass(resolved)) {
                propsSchema.type(Schema.SchemaType.OBJECT);
                propsSchema = SchemaRegistry.checkRegistration(valueType, typeResolver, propsSchema);
            }
        } else if (index.containsClass(valueType)) {
            if (isA(valueType, ENUM_TYPE)) {
                DataObjectLogging.log.processingEnum(type);
                propsSchema = SchemaFactory.enumToSchema(index, cl, valueType);
            } else {
                propsSchema.type(Schema.SchemaType.OBJECT);
                pushToStack(valueType, propsSchema);
            }

            propsSchema = SchemaRegistry.checkRegistration(valueType, typeResolver, propsSchema);
        }

        return propsSchema;
    }

    private Type resolveTypeVariable(Schema schema, Type fieldType, boolean pushToStack) {
        // Type variable (e.g. A in Foo<A>)
        Type resolvedType = typeResolver.getResolvedType(fieldType);
        DataObjectLogging.log.resolvedType(fieldType, resolvedType);

        if (isTerminalType(resolvedType) || !index.containsClass(resolvedType)) {
            DataObjectLogging.log.terminalType(resolvedType);
            TypeUtil.applyTypeAttributes(resolvedType, schema);
        } else if (pushToStack) {
            // Add resolved type to stack.
            objectStack.push(annotationTarget, parentPathEntry, resolvedType, schema);
        }

        return resolvedType;
    }

    private void pushToStack(Type fieldType) {
        objectStack.push(annotationTarget, parentPathEntry, fieldType, schema);
    }

    private void pushToStack(Type resolvedType, Schema schema) {
        objectStack.push(annotationTarget, parentPathEntry, resolvedType, schema);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(index, cl, testSubject, test);
    }
}
