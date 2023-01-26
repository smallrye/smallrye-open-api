package io.smallrye.openapi.api;

import java.util.Map;
import java.util.Optional;
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

    private static final Optional<String[]> UNSET = Optional.of(new String[0]);
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
    private Boolean arrayReferencesEnable;
    private String customSchemaRegistryClass;
    private Boolean applicationPathDisable;
    private Boolean privatePropertiesEnable;
    private String propertyNamingStrategy;
    private Boolean sortedPropertiesEnable;
    private Map<String, String> schemas;
    private String version;
    private String infoTitle;
    private String infoVersion;
    private String infoDescription;
    private String infoTermsOfService;
    private String infoContactEmail;
    private String infoContactName;
    private String infoContactUrl;
    private String infoLicenseName;
    private String infoLicenseUrl;
    private OperationIdStrategy operationIdStrategy;
    private Set<String> scanProfiles;
    private Set<String> scanExcludeProfiles;
    private Optional<String[]> defaultProduces = UNSET;
    private Optional<String[]> defaultConsumes = UNSET;
    private Optional<Boolean> allowNakedPathParameter = Optional.empty();
    private Integer maximumStaticFileSize;

    public static OpenApiConfig fromConfig(Config config) {
        return new OpenApiConfigImpl(config);
    }

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
            scanPackages = patternOf(getStringConfigValue(OASConfig.SCAN_PACKAGES));
        }
        return scanPackages;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanClasses()
     */
    @Override
    public Pattern scanClasses() {
        if (scanClasses == null) {
            scanClasses = patternOf(getStringConfigValue(OASConfig.SCAN_CLASSES));
        }
        return scanClasses;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanExcludePackages()
     */
    @Override
    public Pattern scanExcludePackages() {
        if (scanExcludePackages == null) {
            scanExcludePackages = patternOf(getStringConfigValue(OASConfig.SCAN_EXCLUDE_PACKAGES),
                    OpenApiConstants.NEVER_SCAN_PACKAGES);
        }
        return scanExcludePackages;
    }

    /**
     * @see io.smallrye.openapi.api.OpenApiConfig#scanExcludeClasses()
     */
    @Override
    public Pattern scanExcludeClasses() {
        if (scanExcludeClasses == null) {
            scanExcludeClasses = patternOf(getStringConfigValue(OASConfig.SCAN_EXCLUDE_CLASSES),
                    OpenApiConstants.NEVER_SCAN_CLASSES);
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
    public boolean arrayReferencesEnable() {
        if (arrayReferencesEnable == null) {
            arrayReferencesEnable = getConfig()
                    .getOptionalValue(OpenApiConstants.SMALLRYE_ARRAY_REFERENCES_ENABLE, Boolean.class)
                    .orElse(true);
        }
        return arrayReferencesEnable;
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
    public boolean privatePropertiesEnable() {
        if (privatePropertiesEnable == null) {
            privatePropertiesEnable = getConfig()
                    .getOptionalValue(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, Boolean.class)
                    .orElse(OpenApiConfig.super.privatePropertiesEnable());
        }

        return privatePropertiesEnable;
    }

    @Override
    public String propertyNamingStrategy() {
        if (propertyNamingStrategy == null) {
            propertyNamingStrategy = getConfig()
                    .getOptionalValue(OpenApiConstants.SMALLRYE_PROPERTY_NAMING_STRATEGY, String.class)
                    .orElse(OpenApiConfig.super.propertyNamingStrategy());
        }

        return propertyNamingStrategy;
    }

    @Override
    public boolean sortedPropertiesEnable() {
        if (sortedPropertiesEnable == null) {
            sortedPropertiesEnable = getConfig()
                    .getOptionalValue(OpenApiConstants.SMALLRYE_SORTED_PROPERTIES_ENABLE, Boolean.class)
                    .orElse(OpenApiConfig.super.sortedPropertiesEnable());
        }

        return sortedPropertiesEnable;
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

    @Override
    public String getOpenApiVersion() {
        if (version == null) {
            version = getStringConfigValue(OpenApiConstants.VERSION);
        }
        return version;
    }

    @Override
    public String getInfoTitle() {
        if (infoTitle == null) {
            infoTitle = getStringConfigValue(OpenApiConstants.INFO_TITLE);
        }
        return infoTitle;
    }

    @Override
    public String getInfoVersion() {
        if (infoVersion == null) {
            infoVersion = getStringConfigValue(OpenApiConstants.INFO_VERSION);
        }
        return infoVersion;
    }

    @Override
    public String getInfoDescription() {
        if (infoDescription == null) {
            infoDescription = getStringConfigValue(OpenApiConstants.INFO_DESCRIPTION);
        }
        return infoDescription;
    }

    @Override
    public String getInfoTermsOfService() {
        if (infoTermsOfService == null) {
            infoTermsOfService = getStringConfigValue(OpenApiConstants.INFO_TERMS);
        }
        return infoTermsOfService;
    }

    @Override
    public String getInfoContactEmail() {
        if (infoContactEmail == null) {
            infoContactEmail = getStringConfigValue(OpenApiConstants.INFO_CONTACT_EMAIL);
        }
        return infoContactEmail;
    }

    @Override
    public String getInfoContactName() {
        if (infoContactName == null) {
            infoContactName = getStringConfigValue(OpenApiConstants.INFO_CONTACT_NAME);
        }
        return infoContactName;
    }

    @Override
    public String getInfoContactUrl() {
        if (infoContactUrl == null) {
            infoContactUrl = getStringConfigValue(OpenApiConstants.INFO_CONTACT_URL);
        }
        return infoContactUrl;
    }

    @Override
    public String getInfoLicenseName() {
        if (infoLicenseName == null) {
            infoLicenseName = getStringConfigValue(OpenApiConstants.INFO_LICENSE_NAME);
        }
        return infoLicenseName;
    }

    @Override
    public String getInfoLicenseUrl() {
        if (infoLicenseUrl == null) {
            infoLicenseUrl = getStringConfigValue(OpenApiConstants.INFO_LICENSE_URL);
        }
        return infoLicenseUrl;
    }

    @Override
    public OperationIdStrategy getOperationIdStrategy() {
        if (operationIdStrategy == null) {
            String strategy = getStringConfigValue(OpenApiConstants.OPERATION_ID_STRAGEGY);
            if (strategy != null) {
                return OperationIdStrategy.valueOf(strategy);
            }
        }
        return null;
    }

    @Override
    public Optional<String[]> getDefaultProduces() {
        if (defaultProduces == UNSET) {
            defaultProduces = getDefaultContentType(OpenApiConstants.DEFAULT_PRODUCES);
        }
        return defaultProduces;
    }

    @Override
    public Optional<Boolean> allowNakedPathParameter() {
        return allowNakedPathParameter;
    }

    @Override
    public void doAllowNakedPathParameter() {
        this.allowNakedPathParameter = Optional.of(true);
    }

    @Override
    public Optional<String[]> getDefaultConsumes() {
        if (defaultConsumes == UNSET) {
            defaultConsumes = getDefaultContentType(OpenApiConstants.DEFAULT_CONSUMES);
        }
        return defaultConsumes;
    }

    @Override
    public Set<String> getScanProfiles() {
        if (scanProfiles == null) {
            String classes = getStringConfigValue(OpenApiConstants.SCAN_PROFILES);
            if (classes == null) {
                classes = getStringConfigValue(OpenApiConstants.SCAN_PROFILES);
            }
            scanProfiles = asCsvSet(classes);
        }
        return scanProfiles;
    }

    @Override
    public Set<String> getScanExcludeProfiles() {
        if (scanExcludeProfiles == null) {
            String classes = getStringConfigValue(OpenApiConstants.SCAN_EXCLUDE_PROFILES);
            if (classes == null) {
                classes = getStringConfigValue(OpenApiConstants.SCAN_EXCLUDE_PROFILES);
            }
            scanExcludeProfiles = asCsvSet(classes);
        }
        return scanExcludeProfiles;
    }

    @Override
    public Integer getMaximumStaticFileSize() {
        if (maximumStaticFileSize == null) {
            maximumStaticFileSize = getConfig()
                    .getOptionalValue(OpenApiConstants.MAXIMUM_STATIC_FILE_SIZE, Integer.class)
                    .orElse(OpenApiConfig.super.getMaximumStaticFileSize());
        }
        return maximumStaticFileSize;
    }

    /**
     * getConfig().getOptionalValue(key) can return "" if optional {@link Converter}s are used. Enforce a null value if
     * we get an empty string back.
     */
    String getStringConfigValue(String key) {
        return getConfig().getOptionalValue(key, String.class).map(v -> "".equals(v.trim()) ? null : v).orElse(null);
    }

    Optional<String[]> getDefaultContentType(String key) {
        return getConfig().getOptionalValue(key, String[].class);
    }
}
