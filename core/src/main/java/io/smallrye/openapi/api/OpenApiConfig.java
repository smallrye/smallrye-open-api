package io.smallrye.openapi.api;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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

    public Pattern scanPackages();

    public Pattern scanClasses();

    public Pattern scanExcludePackages();

    public Pattern scanExcludeClasses();

    public Set<String> servers();

    public Set<String> pathServers(String path);

    public Set<String> operationServers(String operationId);

    public boolean scanDependenciesDisable();

    public Set<String> scanDependenciesJars();

    public boolean schemaReferencesEnable();

    public String customSchemaRegistryClass();

    public boolean applicationPathDisable();

    public Map<String, String> getSchemas();

    // Here we extend this in SmallRye with some more configure options (mp.openapi.extensions)
    public String getOpenApiVersion();

    public String getInfoTitle();

    public String getInfoVersion();

    public String getInfoDescription();

    public String getInfoTermsOfService();

    public String getInfoContactEmail();

    public String getInfoContactName();

    public String getInfoContactUrl();

    public String getInfoLicenseName();

    public String getInfoLicenseUrl();
}
