package io.smallrye.openapi.runtime.scanner;

import static io.smallrye.openapi.runtime.util.TypeUtil.getSchemaAnnotation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeParser;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * A simple registry used to track schemas that have been generated and inserted
 * into the #/components section of the
 *
 * @author eric.wittmann@gmail.com
 */
public class SchemaRegistry {

    /**
     * Check if the entityType is eligible for registration using the
     * typeResolver. The eligible kinds of types are
     *
     * <ul>
     * <li>{@link org.jboss.jandex.Type.Kind#CLASS CLASS}
     * <li>{@link org.jboss.jandex.Type.Kind#PARAMETERIZED_TYPE
     * PARAMETERIZED_TYPE}
     * <li>{@link org.jboss.jandex.Type.Kind#TYPE_VARIABLE TYPE_VARIABLE}
     * <li>{@link org.jboss.jandex.Type.Kind#WILDCARD_TYPE WILDCARD_TYPE}
     * </ul>
     *
     * If eligible, schema references are enabled by MP Config property
     * <code>mp.openapi.extensions.smallrye.schema-references.enable</code>, and the
     * resolved type is available in the registry's {@link IndexView} then the
     * schema can be registered.
     *
     * Only if the type has not already been registered earlier will it be
     * added.
     *
     * @param type
     *        the {@link Type} the {@link Schema} applies to
     * @param resolver
     *        a {@link TypeResolver} that will be used to resolve
     *        parameterized and wildcard types
     * @param schema
     *        {@link Schema} to add to the registry
     * @return the same schema if not eligible for registration, or a reference
     *         to the schema registered for the given Type
     */
    public Schema checkRegistration(Type type, Set<Type> views, TypeResolver resolver, Schema schema) {
        return register(type, views, resolver, schema, (reg, key) -> reg.register(key, schema, null));
    }

    /**
     * Attempt to register ONLY a reference to entityType using the typeResolver.
     * The eligible kinds of types are
     *
     * <ul>
     * <li>{@link org.jboss.jandex.Type.Kind#CLASS CLASS}
     * <li>{@link org.jboss.jandex.Type.Kind#PARAMETERIZED_TYPE
     * PARAMETERIZED_TYPE}
     * <li>{@link org.jboss.jandex.Type.Kind#TYPE_VARIABLE TYPE_VARIABLE}
     * <li>{@link org.jboss.jandex.Type.Kind#WILDCARD_TYPE WILDCARD_TYPE}
     * </ul>
     *
     * If eligible, schema references are enabled by MP Config property
     * <code>mp.openapi.extensions.smallrye.schema-references.enable</code>, and the
     * resolved type is available in the registry's {@link IndexView} then the
     * schema reference can be registered.
     *
     * Only if the type has not already been registered earlier will it be
     * added.
     *
     * @param type
     *        the {@link Type} the {@link Schema} applies to
     * @param resolver
     *        a {@link TypeResolver} that will be used to resolve
     *        parameterized and wildcard types
     * @param schema
     *        {@link Schema} to add to the registry
     * @return the same schema if not eligible for registration, or a reference
     *         to the schema registered for the given Type
     */
    public Schema registerReference(Type type, Set<Type> views, TypeResolver resolver, Schema schema) {
        return register(type, views, resolver, schema, SchemaRegistry::registerReference);
    }

    public Schema register(Type type, Set<Type> views, TypeResolver resolver, Schema schema,
            BiFunction<SchemaRegistry, TypeKey, Schema> registrationAction) {

        final Type resolvedType = TypeResolver.resolve(type, resolver);

        switch (resolvedType.kind()) {
            case CLASS:
            case PARAMETERIZED_TYPE:
            case TYPE_VARIABLE:
            case WILDCARD_TYPE:
                break;
            default:
                return schema;
        }

        if (disabled) {
            return schema;
        }

        TypeKey key = new TypeKey(resolvedType, views);

        if (hasRef(key)) {
            schema = lookupRef(key);
        } else if (!isTypeRegistrationSupported(resolvedType, schema)
                || index.getClassByName(resolvedType.name()) == null) {
            return schema;
        } else {
            schema = registrationAction.apply(this, key);
        }

        return schema;
    }

    /**
     * Convenience method to check if the current thread's <code>SchemaRegistry</code>
     * contains a schema for the given type (which may require type resolution using resolver).
     *
     * @param type type to check for existence of schema
     * @param views types applied to the currently-active JsonView (Jackson annotation)
     * @param resolver resolver for type parameter
     * @return true when schema references are enabled and the type is present in the registry, otherwise false
     */
    public boolean hasSchema(Type type, Set<Type> views, TypeResolver resolver) {
        if (disabled) {
            return false;
        }

        return hasSchema(TypeResolver.resolve(type, resolver), views);
    }

    /**
     * Information about a single generated schema.
     *
     * @author eric.wittmann@gmail.com
     */
    static class GeneratedSchemaInfo {
        public final String name;
        public final Schema schema;
        public final Schema schemaRef;

        GeneratedSchemaInfo(String name, Schema schema, Schema schemaRef) {
            this.name = name;
            this.schema = schema;
            this.schemaRef = schemaRef;
        }
    }

    private final AnnotationScannerContext context;
    private final OpenApiConfig config;
    private final OpenAPI oai;
    private final IndexView index;
    /**
     * Testing only! Disables use of the registry for backward-compatibility of several tests that
     * directly use {@link OpenApiDataObjectScanner}.
     */
    private boolean disabled;

    private final Map<TypeKey, GeneratedSchemaInfo> registry = new LinkedHashMap<>();
    private final Set<String> names = new LinkedHashSet<>();

    public SchemaRegistry(AnnotationScannerContext context) {
        this.context = context;
        this.config = context.getConfig();
        this.oai = context.getOpenApi();
        this.index = context.getAugmentedIndex();

        /*
         * If anything has been added in the component scan, add the names here
         * to prevent a collision.
         */
        Components components = oai.getComponents();

        if (components != null) {
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                this.names.addAll(schemas.keySet());
            }
        }

        config.getSchemas().entrySet().forEach(entry -> {
            String typeSignature = entry.getKey();
            String jsonSchema = entry.getValue();
            Type type;
            Schema schema;

            try {
                type = TypeParser.parse(typeSignature);
            } catch (Exception e) {
                ScannerLogging.logger.configSchemaTypeInvalid(typeSignature, e);
                return;
            }

            try {
                schema = context.getExtensions()
                        .stream()
                        .map(ext -> ext.parseSchema(jsonSchema))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow(NoSuchElementException::new);
            } catch (Exception e) {
                ScannerLogging.logger.errorParsingSchema(typeSignature);
                return;
            }

            this.register(new TypeKey(type, Collections.emptySet()), schema, Extensions.getName(schema));
            ScannerLogging.logger.configSchemaRegistered(typeSignature);
        });
    }

    /**
     * Register the provided {@link Schema} for the provided {@link Type}. If an
     * existing schema has already been registered for the type, it will be
     * replaced by the schema given in this method.
     *
     * @param entityType
     *        the type the {@link Schema} applies to
     * @param views
     *
     * @param schema
     *        {@link Schema} to add to the registry
     * @return a reference to the newly registered {@link Schema}
     */
    public Schema register(Type entityType, Set<Type> views, Schema schema) {
        TypeKey key = new TypeKey(entityType, views);

        if (hasRef(key)) {
            // This is a replacement registration
            remove(key);
        }

        return register(key, schema, null);
    }

    private Schema registerReference(TypeKey key) {
        String name = deriveName(key, null);
        Schema schemaRef = OASFactory.createSchema().ref(name);
        registry.put(key, new GeneratedSchemaInfo(name, null, schemaRef));
        names.add(name);

        return OASFactory.createSchema().ref(schemaRef.getRef());
    }

    /**
     * Derive the schema's display name and add to both the registry and the
     * OpenAPI document's schema map, contained in components. If a type is
     * registered using a name that already exists in the registry, a sequential
     * number will be appended to the schemas display name prior to adding.
     *
     * Note, this method does NOT merge schemas found during the scanning of the
     * {@link org.eclipse.microprofile.openapi.annotations.Components}
     * annotation with those found during the model scan.
     *
     * @param key
     *        a value to be used for referencing the schema in the registry
     * @param schema
     *        {@link Schema} to add to the registry
     * @return a reference to the newly registered {@link Schema}
     */
    private Schema register(TypeKey key, Schema schema, String schemaName) {
        String name = deriveName(key, schemaName);
        Schema schemaRef = OASFactory.createSchema().ref(name);
        registry.put(key, new GeneratedSchemaInfo(name, schema, schemaRef));
        names.add(name);

        ModelUtil.components(oai).addSchema(name, schema);

        return OASFactory.createSchema().ref(schemaRef.getRef());
    }

    String deriveName(TypeKey key, String schemaName) {
        /*
         * We cannot use the 'name' on the SchemaImpl because it may be a
         * property name rather then a schema name.
         */
        if (schemaName == null) {
            AnnotationTarget targetSchema = index.getClassByName(key.type.name());
            AnnotationInstance schemaAnnotation = targetSchema != null ? getSchemaAnnotation(context, targetSchema) : null;

            if (schemaAnnotation != null) {
                schemaName = context.annotations().value(schemaAnnotation, SchemaConstant.PROP_NAME);
            }
        }

        String nameBase = schemaName != null ? schemaName : key.defaultName();
        String name = nameBase + key.viewSuffix();
        int idx = 1;
        while (this.names.contains(name)) {
            name = nameBase + idx++;
        }

        return name;
    }

    public Schema lookupRef(Type instanceType, Set<Type> views) {
        return lookupRef(new TypeKey(instanceType, views));
    }

    public boolean hasRef(Type instanceType, Set<Type> views) {
        return hasRef(new TypeKey(instanceType, views));
    }

    public Schema lookupSchema(Type instanceType, Set<Type> views) {
        return lookupSchema(new TypeKey(instanceType, views));
    }

    public boolean hasSchema(Type instanceType, Set<Type> views) {
        return hasSchema(new TypeKey(instanceType, views));
    }

    public boolean isTypeRegistrationSupported(Type type, Schema schema) {
        if (config == null || !TypeUtil.allowRegistration(context, type)) {
            return false;
        }
        if (!config.arrayReferencesEnable()) {
            List<Schema.SchemaType> types = schema.getType();
            return types == null || !types.contains(SchemaType.ARRAY);
        }
        return true;
    }

    private Schema lookupRef(TypeKey key) {
        GeneratedSchemaInfo info = registry.get(key);

        if (info == null) {
            throw ScannerMessages.msg.notRegistered(key.type.name());
        }

        return OASFactory.createSchema().ref(info.schemaRef.getRef());
    }

    private Schema lookupSchema(TypeKey key) {
        GeneratedSchemaInfo info = registry.get(key);

        if (info == null) {
            throw ScannerMessages.msg.notRegistered(key.type.name());
        }

        return info.schema;
    }

    private boolean hasRef(TypeKey key) {
        return registry.containsKey(key);
    }

    private boolean hasSchema(TypeKey key) {
        return registry.containsKey(key) && registry.get(key).schema != null;
    }

    private void remove(TypeKey key) {
        GeneratedSchemaInfo info = this.registry.remove(key);
        this.names.remove(info.name);
    }

    /************************************************************************/

    /**
     * This class is used as the key when storing {@link Schema}s in the
     * registry. The purpose is to replicate the same behavior as the
     * {@link Type} classes <code>equals</code> and <code>hashCode</code>
     * functions, with the exception that the {@link Type}'s annotations are not
     * considered in these versions of the methods.
     *
     *
     * @author Michael Edgar {@literal <michael@xlate.io>}
     */
    public static final class TypeKey {
        private final Type type;
        private final Set<Type> views;
        private int hashCode = 0;

        TypeKey(Type type, Set<Type> views) {
            this.type = type;
            this.views = new LinkedHashSet<>(views);
        }

        /*
         * TypeKey(Type type) {
         * this(type, Collections.emptySet());
         * }
         */

        public String defaultName() {
            StringBuilder name = new StringBuilder(type.name().local());

            switch (type.kind()) {
                case PARAMETERIZED_TYPE:
                    appendParameterNames(name, type.asParameterizedType());
                    break;
                case WILDCARD_TYPE:
                    name.append(wildcardName(type.asWildcardType()));
                    break;
                default:
                    break;
            }

            return name.toString();
        }

        public String viewSuffix() {
            if (views.isEmpty()) {
                return "";
            }

            StringBuilder suffix = new StringBuilder();

            for (Type view : views) {
                suffix.append('_');
                suffix.append(view.name().local());
            }

            return suffix.toString();
        }

        static void appendParameterNames(StringBuilder name, ParameterizedType type) {
            for (Type param : type.asParameterizedType().arguments()) {
                switch (param.kind()) {
                    case PARAMETERIZED_TYPE:
                        name.append(param.name().local());
                        appendParameterNames(name, param.asParameterizedType());
                        break;
                    case WILDCARD_TYPE:
                        name.append(wildcardName(param.asWildcardType()));
                        break;
                    default:
                        name.append(param.name().local());
                        break;
                }
            }
        }

        static String wildcardName(WildcardType type) {
            Type superBound = type.superBound();

            if (superBound != null) {
                return "Super" + superBound.name().local();
            } else {
                Type extendsBound = type.extendsBound();

                if (!DotName.createSimple("java.lang.Object").equals(extendsBound.name())) {
                    return "Extends" + extendsBound.name().local();
                }

                return extendsBound.name().local();
            }
        }

        /**
         * Determine if the two {@link Type}s are equal.
         *
         * @see Type#equals
         * @see ParameterizedType#equals
         * @see TypeVariable#equals
         * @see WildcardType#equals
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }

            TypeKey other = (TypeKey) o;

            if (type == other.type) {
                return true;
            }

            if (type == null || type.getClass() != other.type.getClass()) {
                return false;
            }

            if (!type.name().equals(other.type.name())) {
                return false;
            }

            if (!views.equals(other.views)) {
                return false;
            }

            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                ParameterizedType otherType = (ParameterizedType) other.type;
                Type typeOwner = paramType.owner();
                Type otherOwner = otherType.owner();

                return (typeOwner == otherOwner || (typeOwner != null && typeOwner.equals(otherOwner)))
                        && Objects.equals(paramType.arguments(), otherType.arguments());
            }

            if (type instanceof TypeVariable) {
                TypeVariable varType = (TypeVariable) type;
                TypeVariable otherType = (TypeVariable) other.type;

                String id = varType.identifier();
                String otherId = otherType.identifier();

                return id.equals(otherId) && Objects.equals(varType.bounds(), otherType.bounds());
            }

            if (type instanceof WildcardType) {
                WildcardType wildType = (WildcardType) type;
                WildcardType otherType = (WildcardType) other.type;

                return Objects.equals(wildType.extendsBound(), otherType.extendsBound()) &&
                        Objects.equals(wildType.superBound(), otherType.superBound());
            }

            return true;
        }

        /**
         * @see Type#equals
         * @see ParameterizedType#equals
         * @see TypeVariable#equals
         * @see WildcardType#equals
         */
        @Override
        public int hashCode() {
            int hash = this.hashCode;

            if (hash != 0) {
                return hash;
            }

            hash = type.name().hashCode();
            hash = 31 * hash + views.hashCode();

            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                Type owner = paramType.owner();
                hash = 31 * hash + Objects.hashCode(paramType.arguments());
                hash = 31 * hash + (owner != null ? owner.hashCode() : 0);
            }

            if (type instanceof TypeVariable) {
                TypeVariable varType = (TypeVariable) type;
                hash = 31 * hash + varType.identifier().hashCode();
                hash = 31 * hash + Objects.hashCode(varType.bounds());
            }

            if (type instanceof WildcardType) {
                WildcardType wildType = (WildcardType) type;
                hash = 31 * hash + Objects.hash(wildType.extendsBound(), wildType.superBound());
            }

            this.hashCode = hash;
            return hash;
        }
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
