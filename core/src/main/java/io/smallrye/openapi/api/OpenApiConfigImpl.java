package io.smallrye.openapi.api;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.OASConfig;

import io.smallrye.openapi.api.constants.OpenApiConstants;

/**
 * Implementation of the {@link OpenApiConfig} interface that gets config information from a
 * standard MP Config object.
 * 
 * @author eric.wittmann@gmail.com
 */
public class OpenApiConfigImpl implements OpenApiConfig {

    private Config config;

    private String modelReader;
    private String filter;
    private Boolean scanDisable;
    private Pattern scanPackages;
    private Pattern scanClasses;
    private Pattern scanExcludePackages;
    private Pattern scanExcludeClasses;
    private Set<String> servers;
    private Boolean scanDependenciesDisable;
    private Set<String> scanDependenciesJars;
    private Boolean schemaReferencesEnable;
    private String customSchemaRegistryClass;
    private Boolean applicationPathDisable;
    private Map<String, String> schemas;

    /**
     * Constructor.
     * 
     * @param config MicroProfile Config instance
     */
    public OpenApiConfigImpl(Config config) {
        this.config = config;
    }

    /**
     * @return the MP config instance
     */
    protected Config getConfig() {
        // We cannot use ConfigProvider.getConfig() as the archive is not deployed yet - TCCL cannot be set
        return config;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#modelReader()
     */
    @Override
    public String modelReader() {
        if (modelReader == null) {
            modelReader = getStringConfigValue(OASConfig.MODEL_READER);
        }
        return modelReader;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#filter()
     */
    @Override
    public String filter() {
        if (filter == null) {
            filter = getStringConfigValue(OASConfig.FILTER);
        }
        return filter;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanDisable()
     */
    @Override
    public boolean scanDisable() {
        if (scanDisable == null) {
            scanDisable = getConfig().getOptionalValue(OASConfig.SCAN_DISABLE, Boolean.class).orElse(false);
        }
        return scanDisable;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanPackages()
     */
    @Override
    public Pattern scanPackages() {
        if (scanPackages == null) {
            scanPackages = patternOf(OASConfig.SCAN_PACKAGES);
        }
        return scanPackages;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanClasses()
     */
    @Override
    public Pattern scanClasses() {
        if (scanClasses == null) {
            scanClasses = patternOf(OASConfig.SCAN_CLASSES);
        }
        return scanClasses;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanExcludePackages()
     */
    @Override
    public Pattern scanExcludePackages() {
        if (scanExcludePackages == null) {
            scanExcludePackages = patternOf(OASConfig.SCAN_EXCLUDE_PACKAGES, OpenApiConstants.NEVER_SCAN_PACKAGES);
        }
        return scanExcludePackages;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanExcludeClasses()
     */
    @Override
    public Pattern scanExcludeClasses() {
        if (scanExcludeClasses == null) {
            scanExcludeClasses = patternOf(OASConfig.SCAN_EXCLUDE_CLASSES, OpenApiConstants.NEVER_SCAN_CLASSES);
        }
        return scanExcludeClasses;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#servers()
     */
    @Override
    public Set<String> servers() {
        if (servers == null) {
            String theServers = getStringConfigValue(OASConfig.SERVERS);
            servers = asCsvSet(theServers);
        }
        return servers;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#pathServers(java.lang.String)
     */
    @Override
    public Set<String> pathServers(String path) {
        String pathServers = getStringConfigValue(OASConfig.SERVERS_PATH_PREFIX + path);
        return asCsvSet(pathServers);
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#operationServers(java.lang.String)
     */
    @Override
    public Set<String> operationServers(String operationId) {
        String opServers = getStringConfigValue(OASConfig.SERVERS_OPERATION_PREFIX + operationId);
        return asCsvSet(opServers);
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanDependenciesDisable()
     */
    @Override
    public boolean scanDependenciesDisable() {
        if (scanDependenciesDisable == null) {
            scanDependenciesDisable = getConfig()
                    .getOptionalValue(OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, Boolean.class)
                    .orElse(getConfig().getOptionalValue(OpenApiConstants.SCAN_DEPENDENCIES_DISABLE, Boolean.class)
                            .orElse(false));
        }
        return scanDependenciesDisable;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanDependenciesJars()
     */
    @Override
    public Set<String> scanDependenciesJars() {
        if (scanDependenciesJars == null) {
            String classes = getStringConfigValue(OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_JARS);
            if (classes == null) {
                classes = getStringConfigValue(OpenApiConstants.SCAN_DEPENDENCIES_JARS);
            }
            scanDependenciesJars = asCsvSet(classes);
        }
        return scanDependenciesJars;
    }

    @Override
    public boolean schemaReferencesEnable() {
        if (schemaReferencesEnable == null) {
            schemaReferencesEnable = getConfig()
                    .getOptionalValue(OpenApiConstants.SMALLRYE_SCHEMA_REFERENCES_ENABLE, Boolean.class)
                    .orElse(getConfig().getOptionalValue(OpenApiConstants.SCHEMA_REFERENCES_ENABLE, Boolean.class)
                            .orElse(true));
        }
        return schemaReferencesEnable;
    }

    @Override
    public String customSchemaRegistryClass() {
        if (customSchemaRegistryClass == null) {
            customSchemaRegistryClass = getStringConfigValue(OpenApiConstants.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS);
            if (customSchemaRegistryClass == null) {
                customSchemaRegistryClass = getStringConfigValue(OpenApiConstants.CUSTOM_SCHEMA_REGISTRY_CLASS);
            }
        }
        return customSchemaRegistryClass;
    }

    @Override
    public boolean applicationPathDisable() {
        if (applicationPathDisable == null) {
            applicationPathDisable = getConfig().getOptionalValue(OpenApiConstants.SMALLRYE_APP_PATH_DISABLE, Boolean.class)
                    .orElse(getConfig().getOptionalValue(OpenApiConstants.APP_PATH_DISABLE, Boolean.class)
                            .orElse(false));
        }
        return applicationPathDisable;
    }

    @Override
    public Map<String, String> getSchemas() {
        if (schemas == null) {
            schemas = StreamSupport
                    .stream(config.getPropertyNames().spliterator(), false)
                    .filter(name -> name.startsWith("mp.openapi.schema.") ||
                            name.startsWith("MP_OPENAPI_SCHEMA_"))
                    .collect(Collectors.toMap(name -> name.substring("mp.openapi.schema.".length()),
                            name -> config.getValue(name, String.class)));
        }
        return schemas;
    }

    /**
     * getConfig().getOptionalValue(key) can return "" if optional {@link Converter}s are used. Enforce a null value if
     * we get an empty string back.
     */
    String getStringConfigValue(String key) {
        return getConfig().getOptionalValue(key, String.class).map(v -> "".equals(v.trim()) ? null : v).orElse(null);
    }

    Pattern patternOf(String key) {
        return patternOf(key, null);
    }

    Pattern patternOf(String key, Set<String> buildIn) {
        String configValue = getStringConfigValue(key);
        Pattern pattern;

        if (configValue != null && (configValue.startsWith("^") || configValue.endsWith("$"))) {
            pattern = Pattern.compile(configValue);
        } else {
            Set<String> literals = asCsvSet(configValue);
            if (buildIn != null && !buildIn.isEmpty()) {
                literals.addAll(buildIn);
            }
            if (literals.isEmpty()) {
                return Pattern.compile("", Pattern.LITERAL);
            } else {
                pattern = Pattern.compile("(" + literals.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")");
            }
        }

        return pattern;
    }

    private static Set<String> asCsvSet(String items) {
        Set<String> rval = new HashSet<>();
        if (items != null) {
            String[] split = items.split(",");
            for (String item : split) {
                rval.add(item.trim());
            }
        }
        return rval;
    }

}
