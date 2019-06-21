package io.smallrye.openapi.runtime.scanner;

import static io.smallrye.openapi.runtime.util.TypeUtil.getSchemaAnnotation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * A simple registry used to track schemas that have been generated and inserted
 * into the #/components section of the
 *
 * @author eric.wittmann@gmail.com
 */
public class SchemaRegistry {

    // Initial value is null
    private static ThreadLocal<SchemaRegistry> current = new ThreadLocal<>();

    /**
     * Create a new instance of a {@link SchemaRegistry} on this thread. The
     * registry returned by this method may also be obtained by subsequent calls
     * to {@link #currentInstance()}. Additional calls of this method will
     * replace the registry in the current thread context with a new instance.
     *
     * @param config
     *        current runtime configuration
     * @param oai
     *        the OpenAPI being constructed by the scan
     * @param index
     *        indexed class information
     * @return the registry
     */
    public static SchemaRegistry newInstance(OpenApiConfig config, OpenAPI oai, IndexView index) {
        SchemaRegistry registry = new SchemaRegistry(config, oai, index);
        current.set(registry);
        return registry;
    }

    /**
     * Retrieve the {@link SchemaRegistry} previously created by
     * {@link SchemaRegistry#newInstance(OpenApiConfig, OpenAPI, IndexView)
     * newInstance} for the current thread, or <code>null</code> if none has yet
     * been created.
     *
     * @return a {@link SchemaRegistry} instance or null
     */
    public static SchemaRegistry currentInstance() {
        return current.get();
    }

    public static void remove() {
        current.remove();
    }

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
     * <code>mp.openapi.extensions.schema-references.enable</code>, and the
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
     *         to the schema registered for the give Type
     */
    public static Schema checkRegistration(Type type, TypeResolver resolver, Schema schema) {
        Type resolvedType = resolver.getResolvedType(type);

        switch (resolvedType.kind()) {
            case CLASS:
            case PARAMETERIZED_TYPE:
            case TYPE_VARIABLE:
            case WILDCARD_TYPE:
                break;
            default:
                return schema;
        }

        SchemaRegistry registry = currentInstance();

        if (registry == null || !registry.schemaReferenceSupported()) {
            return schema;
        }

        TypeKey key = new TypeKey(resolvedType);

        if (registry.has(key)) {
            schema = registry.lookupRef(key);
        } else if (registry.index.getClassByName(resolvedType.name()) == null) {
            return schema;
        } else {
            schema = registry.register(key, schema);
        }

        return schema;
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

    private final OpenApiConfig config;
    private final OpenAPI oai;
    private final IndexView index;

    private final Map<TypeKey, GeneratedSchemaInfo> registry = new LinkedHashMap<>();
    private final Set<String> names = new LinkedHashSet<>();

    private SchemaRegistry(OpenApiConfig config, OpenAPI oai, IndexView index) {
        this.config = config;
        this.oai = oai;
        this.index = index;

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
    }

    /**
     * Register the provided {@link Schema} for the provided {@link Type}. If an
     * existing schema has already been registered for the type, it will be
     * replaced by the schema given in this method.
     *
     * @param entityType
     *        the type the {@link Schema} applies to
     * @param schema
     *        {@link Schema} to add to the registry
     * @return a reference to the newly registered {@link Schema}
     */
    public Schema register(Type entityType, Schema schema) {
        TypeKey key = new TypeKey(entityType);

        if (has(key)) {
            // This is a replacement registration
            remove(key);
        }

        return register(key, schema);
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
    private Schema register(TypeKey key, Schema schema) {
        /*
         * We cannot use the 'name' on the SchemaImpl because it may be a
         * property name rather then a schema name.
         */
        AnnotationTarget targetSchema = index.getClassByName(key.type.name());
        AnnotationInstance schemaAnnotation = getSchemaAnnotation(targetSchema);
        String schemaName = null;

        if (schemaAnnotation != null) {
            schemaName = JandexUtil.stringValue(schemaAnnotation, OpenApiConstants.PROP_NAME);
        }

        String nameBase = schemaName != null ? schemaName : key.defaultName();
        String name = nameBase;
        int idx = 1;
        while (this.names.contains(name)) {
            name = nameBase + idx++;
        }

        Schema schemaRef = new SchemaImpl();
        schemaRef.setRef(OpenApiConstants.REF_PREFIX_SCHEMA + name);

        registry.put(key, new GeneratedSchemaInfo(name, schema, schemaRef));
        names.add(name);

        ModelUtil.components(oai).addSchema(name, schema);

        return schemaRef;
    }

    public Schema lookupRef(Type instanceType) {
        return lookupRef(new TypeKey(instanceType));
    }

    public boolean has(Type instanceType) {
        return has(new TypeKey(instanceType));
    }

    public boolean schemaReferenceSupported() {
        return config != null && config.schemaReferencesEnable();
    }

    private Schema lookupRef(TypeKey key) {
        GeneratedSchemaInfo info = registry.get(key);

        if (info == null) {
            throw new NoSuchElementException("Class schema not registered: " + key.type.name());
        }

        return info.schemaRef;
    }

    private boolean has(TypeKey key) {
        return registry.containsKey(key);
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
    static class TypeKey {
        private final Type type;
        private int hash = 0;

        TypeKey(Type type) {
            this.type = type;
        }

        public String defaultName() {
            StringBuilder name = new StringBuilder(type.name().local());

            switch (type.kind()) {
                case PARAMETERIZED_TYPE:
                    for (Type param : type.asParameterizedType().arguments()) {
                        if (param.kind() == Type.Kind.WILDCARD_TYPE) {
                            name.append(wildcardName(param.asWildcardType()));
                        } else {
                            name.append(param.name().local());
                        }
                    }
                    break;
                case WILDCARD_TYPE:
                    name.append(wildcardName(type.asWildcardType()));
                    break;
                default:
                    break;
            }

            return name.toString();
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
            int hash = this.hash;

            if (hash != 0) {
                return hash;
            }

            hash = type.name().hashCode();

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

            return this.hash = hash;
        }
    }
}
