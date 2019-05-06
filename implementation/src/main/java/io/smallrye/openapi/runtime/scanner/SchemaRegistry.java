package io.smallrye.openapi.runtime.scanner;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * A simple registry used to track schemas that have been generated and inserted
 * into the #/components section of the
 * @author eric.wittmann@gmail.com
 */
public class SchemaRegistry {

    // Initial value is null
    private static ThreadLocal<SchemaRegistry> current = new ThreadLocal<>();

    public static SchemaRegistry newInstance(OpenApiConfig config, OpenAPI oai) {
        SchemaRegistry registry = new SchemaRegistry(config, oai);
        current.set(registry);
        return registry;
    }

    public static SchemaRegistry currentInstance() {
        return current.get();
    }

    public static Schema checkRegistration(IndexView index, Type entityType, TypeResolver typeResolver, Schema schema) {
        Type resolvedType = typeResolver.getResolvedType(entityType);

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

        if (registry.has(resolvedType)) {
            schema = registry.lookupRef(resolvedType);
        } else if (index.getClassByName(resolvedType.name()) != null) {
            schema = registry.register(resolvedType, schema);
        }

        return schema;
    }

    /**
     * Information about a single generated schema.
     * @author eric.wittmann@gmail.com
     */
    static class GeneratedSchemaInfo {
        @SuppressWarnings("unused")
        public String name;
        public Schema schema;
        public String $ref;
    }

    private final OpenApiConfig config;
    private final OpenAPI oai;
    //private Map<DotName, GeneratedSchemaInfo> registry = new HashMap<>();
    private Map<Type, GeneratedSchemaInfo> registry = new LinkedHashMap<>();
    private Set<String> names = new LinkedHashSet<>();

    private SchemaRegistry(OpenApiConfig config, OpenAPI oai) {
        this.config = config;
        this.oai = oai;
    }

    public Schema register(Type entityType, Schema schema) {
        if (has(entityType)) {
            // This is a replacement registration
            remove(entityType);
        }

        String localName = entityType.name().local();
        String name = localName;
        int idx = 1;
        while (this.names.contains(name)) {
            name = localName + idx++;
        }
        GeneratedSchemaInfo info = new GeneratedSchemaInfo();
        info.schema = schema;
        info.name = name;
        info.$ref = OpenApiConstants.REF_PREFIX_SCHEMA + name;

        //registry.put(entityType.name(), info);
        registry.put(entityType, info);
        names.add(name);

        ModelUtil.components(oai).addSchema(name, schema);

        Schema rval = new SchemaImpl();
        rval.setRef(info.$ref);
        return rval;
    }

    public Schema lookup(Type instanceType) {
        //GeneratedSchemaInfo info = registry.get(instanceType.name());
        GeneratedSchemaInfo info = registry.get(instanceType);

        if (info == null) {
            throw new NoSuchElementException("Class schema not registered: " + instanceType.name());
        }

        return info.schema;
    }

    public Schema lookupRef(Type instanceType) {
        //GeneratedSchemaInfo info = registry.get(instanceType.name());
        GeneratedSchemaInfo info = registry.get(instanceType);

        if (info == null) {
            throw new NoSuchElementException("Class schema not registered: " + instanceType.name());
        }

        Schema rval = new SchemaImpl();
        rval.setRef(info.$ref);
        return rval;
    }

    public boolean has(Type instanceType) {
        //return registry.containsKey(instanceType.name());
        return registry.containsKey(instanceType);
    }

    /*
     * public Map<DotName, GeneratedSchemaInfo> getSchemas() { return
     * this.registry; }
     */

    public Map<Type, GeneratedSchemaInfo> getSchemas() {
        return this.registry;
    }

    public boolean schemaReferenceSupported() {
        return config != null && config.schemaReferencesEnable();
    }

    private void remove(Type entityType) {
        this.registry.remove(entityType);
        this.names.remove(entityType.name().local());
    }
}