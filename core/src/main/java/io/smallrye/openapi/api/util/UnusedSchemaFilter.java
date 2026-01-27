package io.smallrye.openapi.api.util;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of OASFilter that scans the OpenAPI model and removes
 * any entries in `components.schemas` that are not referenced by other schemas
 * in the same model.
 *
 * @deprecated use {@link UnusedComponentFilter} instead.
 */
@Deprecated(since = "4.3.0")
public class UnusedSchemaFilter implements OASFilter {

    /**
     * Map of schemas present in {@code /components/schemas} with a list of the
     * schemas that refer to them.
     */
    Map<String, List<Schema>> references = new HashMap<>();

    @Override
    public Schema filterSchema(Schema schema) {
        String name = referencedName(schema);

        if (name != null) {
            references.computeIfAbsent(name, k -> new ArrayList<>()).add(schema);
        }

        return schema;
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        final Components components = openAPI.getComponents();

        Optional.ofNullable(components)
                .map(Components::getSchemas)
                .map(Map::keySet)
                .ifPresent(schemaNames -> {
                    Set<String> unusedNames = unusedSchemaNames(schemaNames);

                    while (!unusedNames.isEmpty()) {
                        unusedNames.forEach(name -> remove(name, components));
                        unusedNames = unusedSchemaNames(schemaNames);
                    }
                });
    }

    String referencedName(Schema schema) {
        final String ref = schema.getRef();

        if (ref != null && ref.startsWith("#/components/schemas/")) {
            return ModelUtil.nameFromRef(ref);
        }

        return null;
    }

    boolean notUsed(String schemaName) {
        return !references.containsKey(schemaName);
    }

    Set<String> unusedSchemaNames(Set<String> allSchemaNames) {
        return allSchemaNames.stream().filter(this::notUsed).collect(Collectors.toSet());
    }

    void remove(String schemaName, Components components) {
        Schema unusedSchema = components.getSchemas().get(schemaName);
        removeReference(unusedSchema.getAdditionalPropertiesSchema());
        removeReferences(unusedSchema.getAllOf());
        removeReferences(unusedSchema.getAnyOf());
        removeReferences(unusedSchema.getOneOf());
        removeReference(unusedSchema.getItems());
        removeReference(unusedSchema.getNot());
        removeReferences(unusedSchema.getProperties());
        components.removeSchema(schemaName);
        UtilLogging.logger.unusedSchemaRemoved(schemaName);
    }

    void removeReference(Schema schema) {
        if (schema != null) {
            String name = referencedName(schema);

            if (name != null) {
                references.computeIfPresent(name, (k, v) -> {
                    v.remove(schema);
                    return v.isEmpty() ? null : v;
                });
            }
        }
    }

    void removeReferences(Map<String, Schema> schemas) {
        if (schemas != null) {
            removeReferences(schemas.values());
        }
    }

    void removeReferences(Collection<Schema> schemas) {
        if (schemas != null) {
            schemas.forEach(schema -> {
                removeReference(schema);
                removeReference(schema.getAdditionalPropertiesSchema());
                removeReferences(schema.getAllOf());
                removeReferences(schema.getAnyOf());
                removeReferences(schema.getOneOf());
                removeReference(schema.getItems());
                removeReference(schema.getNot());
                removeReferences(schema.getProperties());
            });
        }
    }
}
