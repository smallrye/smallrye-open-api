package io.smallrye.openapi.api;

import java.util.Map;
import java.util.Set;

/**
 * Accessor to OpenAPI configuration options.
 *
 * Reference:
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#31-list-of-configurable-items
 *
 * @author eric.wittmann@gmail.com
 */
public interface OpenApiConfig {

    public String modelReader();

    public String filter();

    public boolean scanDisable();

    public Set<String> scanPackages();

    public Set<String> scanClasses();

    public Set<String> scanExcludePackages();

    public Set<String> scanExcludeClasses();

    public Set<String> servers();

    public Set<String> pathServers(String path);

    public Set<String> operationServers(String operationId);

    public boolean scanDependenciesDisable();

    public Set<String> scanDependenciesJars();

    public boolean schemaReferencesEnable();

    public String customSchemaRegistryClass();

    public boolean applicationPathDisable();

    public Map<String, String> getSchemas();
}
