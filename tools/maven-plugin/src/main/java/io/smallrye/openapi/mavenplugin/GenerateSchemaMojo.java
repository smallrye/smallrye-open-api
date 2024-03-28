package io.smallrye.openapi.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASConfig;
import org.jboss.jandex.IndexView;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)
public class GenerateSchemaMojo extends AbstractMojo {

    /**
     * Directory where to output the schemas.
     * If no path is specified, the schema will be printed to the log.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated/", property = "outputDirectory")
    private File outputDirectory;

    /**
     * Filename of the schema
     * Defaults to openapi. So the files created will be openapi.yaml and openapi.json.
     */
    @Parameter(defaultValue = "openapi", property = "schemaFilename")
    private String schemaFilename;

    /**
     * When you include dependencies, we only look at compile and system scopes (by default)
     * You can change that here.
     * Valid options are: compile, provided, runtime, system, test, import
     */
    @Parameter(defaultValue = "compile,system", property = "includeDependenciesScopes")
    private List<String> includeDependenciesScopes;

    /**
     * When you include dependencies, we only look at jars (by default)
     * You can change that here.
     */
    @Parameter(defaultValue = "jar", property = "includeDependenciesTypes")
    private List<String> includeDependenciesTypes;

    /**
     * Skip execution of the plugin.
     */
    @Parameter(defaultValue = "false", property = "skip")
    private boolean skip;

    /**
     * Disable scanning the project's dependencies for OpenAPI model classes too
     */
    @Parameter(defaultValue = "false", property = "scanDependenciesDisable")
    private boolean scanDependenciesDisable;

    /**
     * Attach the built OpenAPI schema as build artifact.
     */
    @Parameter(defaultValue = "false", property = "attachArtifacts")
    private boolean attachArtifacts;

    /**
     * Load any properties from a file. This file is loaded first, and gets overwritten by explicitly set properties in the
     * maven configuration. Example `${basedir}/src/main/resources/application.properties`.
     */
    @Parameter(property = "configProperties")
    private File configProperties;

    // Properies as per OpenAPI Config.

    /**
     * Configuration property to specify the fully qualified name of the OASModelReader implementation.
     */
    @Parameter(property = "modelReader")
    private String modelReader;

    /**
     * Configuration property to specify the fully qualified name of the OASFilter implementation.
     */
    @Parameter(property = "filter")
    private String filter;

    /**
     * Configuration property to disable annotation scanning.
     */
    @Parameter(property = "scanDisabled")
    private Boolean scanDisabled;

    /**
     * Configuration property to specify the list of packages to scan.
     */
    @Parameter(property = "scanPackages")
    private String scanPackages;

    /**
     * Configuration property to specify the list of classes to scan.
     */
    @Parameter(property = "scanClasses")
    private String scanClasses;

    /**
     * Configuration property to specify the list of packages to exclude from scans.
     */
    @Parameter(property = "scanExcludePackages")
    private String scanExcludePackages;

    /**
     * Configuration property to specify the list of classes to exclude from scans.
     */
    @Parameter(property = "scanExcludeClasses")
    private String scanExcludeClasses;

    /**
     * Configuration property to specify the list of global servers that provide connectivity information.
     */
    @Parameter(property = "servers")
    private List<String> servers;

    /**
     * Prefix of the configuration property to specify an alternative list of servers to service all operations in a path
     */
    @Parameter(property = "pathServers")
    private List<String> pathServers;

    /**
     * Prefix of the configuration property to specify an alternative list of servers to service an operation.
     */
    @Parameter(property = "operationServers")
    private List<String> operationServers;

    /**
     * Fully qualified name of a CustomSchemaRegistry, which can be used to specify a custom schema for a type.
     */
    @Parameter(property = "customSchemaRegistryClass")
    private String customSchemaRegistryClass;

    /**
     * Disable scanning of the javax.ws.rs.Path (and jakarta.ws.rs.Path) for the application path.
     */
    @Parameter(defaultValue = "false", property = "applicationPathDisable")
    private Boolean applicationPathDisable;

    /**
     * To specify a custom OpenAPI version.
     */
    @Parameter(defaultValue = SmallRyeOASConfig.Defaults.VERSION, property = "openApiVersion")
    private String openApiVersion;

    @Parameter(property = "infoTitle")
    private String infoTitle;

    @Parameter(property = "infoVersion")
    private String infoVersion;

    @Parameter(property = "infoDescription")
    private String infoDescription;

    @Parameter(property = "infoTermsOfService")
    private String infoTermsOfService;

    @Parameter(property = "infoContactEmail")
    private String infoContactEmail;

    @Parameter(property = "infoContactName")
    private String infoContactName;

    @Parameter(property = "infoContactUrl")
    private String infoContactUrl;

    @Parameter(property = "infoLicenseName")
    private String infoLicenseName;

    @Parameter(property = "infoLicenseUrl")
    private String infoLicenseUrl;

    /**
     * Configuration property to specify how the operationid is generated. Can be used to minimize risk of collisions between
     * operations.
     */
    @Parameter(property = "operationIdStrategy")
    private String operationIdStrategy;

    /**
     * Profiles which explicitly include operations. Any operation without a matching profile is excluded.
     */
    @Parameter(property = "scanProfiles")
    private List<String> scanProfiles;

    /**
     * Profiles which explicitly exclude operations. Any operation without a matching profile is included.
     */
    @Parameter(property = "scanExcludeProfiles")
    private List<String> scanExcludeProfiles;

    /**
     * Output encoding for openapi document.
     */
    @Parameter(property = "encoding")
    private String encoding;

    /**
     * List of System properties available to the OAS model reader and/or filter (if configured).
     */
    @Parameter
    private Map<String, String> systemPropertyVariables;

    /**
     * Configuration property to specify path for resource classes not annotated with javax.ws.rs.Path (or jakarta.ws.rs.Path)
     */
    @Parameter(property = "scanResourceClasses")
    private Map<String, String> scanResourceClasses;

    enum OutputFileFilter {
        ALL,
        YAML,
        JSON
    }

    /**
     * Filter the type of files that will be generated, allowed values are {@code ALL}, {@code YAML} and {@code JSON}.
     */
    @Parameter(property = "outputFileTypeFilter", defaultValue = "ALL")
    private String outputFileTypeFilter;

    @Component
    private MavenDependencyIndexCreator mavenDependencyIndexCreator;

    @Component
    MavenProjectHelper mavenProjectHelper;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException {
        if (!skip) {
            try {
                IndexView index = mavenDependencyIndexCreator.createIndex(mavenProject, scanDependenciesDisable,
                        includeDependenciesScopes, includeDependenciesTypes);
                SmallRyeOpenAPI openAPI = generateOpenAPI(index);
                write(openAPI);
            } catch (Exception ex) {
                getLog().error(ex);
                // allow failOnError = false ?
                throw new MojoExecutionException("Could not generate OpenAPI Schema", ex);
            }
        }
    }

    private SmallRyeOpenAPI generateOpenAPI(IndexView index) throws IOException, DependencyResolutionRequiredException {
        if (systemPropertyVariables != null) {
            systemPropertyVariables.forEach(System::setProperty);
        }

        Config config = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(getProperties(), "maven-plugin", ConfigSource.DEFAULT_ORDINAL))
                .build();

        return SmallRyeOpenAPI.builder()
                .withConfig(config)
                .withApplicationClassLoader(getClassLoader())
                .withIndex(index)
                .build();
    }

    private ClassLoader getClassLoader() throws DependencyResolutionRequiredException {
        Set<URI> elements = new HashSet<>();

        if (getLog().isDebugEnabled()) {
            getLog().debug("Adding directories/artifacts to annotation scanner class loader:");
        }

        for (String element : mavenProject.getCompileClasspathElements()) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("  " + element);
            }

            elements.add(new File(element).toURI());
        }

        URL[] locators = elements.stream()
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException mue) {
                        throw new UncheckedIOException(mue);
                    }
                })
                .toArray(URL[]::new);

        return URLClassLoader.newInstance(locators, Thread.currentThread().getContextClassLoader());
    }

    private Map<String, String> getProperties() throws IOException {
        // First check if the configProperties is set, if so, load that.
        Map<String, String> cp = new HashMap<>();
        if (configProperties != null && configProperties.exists()) {
            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(configProperties.toPath())) {
                p.load(is);
                p.stringPropertyNames().forEach(k -> cp.put(k, p.getProperty(k)));
            }
        }

        // Now add properties set in the maven plugin.

        addToPropertyMap(cp, OASConfig.MODEL_READER, modelReader);
        addToPropertyMap(cp, OASConfig.FILTER, filter);
        addToPropertyMap(cp, OASConfig.SCAN_DISABLE, scanDisabled);
        addToPropertyMap(cp, OASConfig.SCAN_PACKAGES, scanPackages);
        addToPropertyMap(cp, OASConfig.SCAN_CLASSES, scanClasses);
        addToPropertyMap(cp, OASConfig.SCAN_EXCLUDE_PACKAGES, scanExcludePackages);
        addToPropertyMap(cp, OASConfig.SCAN_EXCLUDE_CLASSES, scanExcludeClasses);
        addToPropertyMap(cp, OASConfig.SERVERS, servers);
        addToPropertyMap(cp, OASConfig.SERVERS_PATH_PREFIX, pathServers);
        addToPropertyMap(cp, OASConfig.SERVERS_OPERATION_PREFIX, operationServers);
        addToPropertyMap(cp, SmallRyeOASConfig.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, scanDependenciesDisable);
        addToPropertyMap(cp, SmallRyeOASConfig.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS, customSchemaRegistryClass);
        addToPropertyMap(cp, SmallRyeOASConfig.SMALLRYE_APP_PATH_DISABLE, applicationPathDisable);
        addToPropertyMap(cp, SmallRyeOASConfig.VERSION, openApiVersion);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_TITLE, infoTitle);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_VERSION, infoVersion);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_DESCRIPTION, infoDescription);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_TERMS, infoTermsOfService);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_CONTACT_EMAIL, infoContactEmail);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_CONTACT_NAME, infoContactName);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_CONTACT_URL, infoContactUrl);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_LICENSE_NAME, infoLicenseName);
        addToPropertyMap(cp, SmallRyeOASConfig.INFO_LICENSE_URL, infoLicenseUrl);
        addToPropertyMap(cp, SmallRyeOASConfig.OPERATION_ID_STRAGEGY, operationIdStrategy);
        addToPropertyMap(cp, SmallRyeOASConfig.SCAN_PROFILES, scanProfiles);
        addToPropertyMap(cp, SmallRyeOASConfig.SCAN_EXCLUDE_PROFILES, scanExcludeProfiles);
        addToPropertyMap(cp, SmallRyeOASConfig.SCAN_RESOURCE_CLASS_PREFIX, scanResourceClasses);

        return cp;
    }

    private void addToPropertyMap(Map<String, String> map, String key, Boolean value) {
        if (value != null) {
            map.put(key, value.toString());
        }
    }

    private void addToPropertyMap(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void addToPropertyMap(Map<String, String> map, String key, List<String> values) {
        if (values != null && !values.isEmpty()) {
            map.put(key, String.join(",", values));
        }
    }

    private void addToPropertyMap(Map<String, String> map, String keyPrefix, Map<String, String> values) {
        if (values != null) {
            values.forEach((key, value) -> map.put(keyPrefix + key, value));
        }
    }

    private void write(SmallRyeOpenAPI openAPI) throws MojoExecutionException {
        try {
            String yaml = openAPI.toYAML();
            String json = openAPI.toJSON();

            if (outputDirectory == null) {
                // no destination file specified => print to stdout
                getLog().info(yaml);
            } else {
                Path directory = outputDirectory.toPath();
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory);
                }

                Charset charset = getCharset(encoding);

                if (Stream.of(OutputFileFilter.ALL, OutputFileFilter.YAML)
                        .anyMatch(f -> f.equals(OutputFileFilter.valueOf(this.outputFileTypeFilter)))) {
                    writeFile(directory, "yaml", yaml.getBytes(charset));
                }

                if (Stream.of(OutputFileFilter.ALL, OutputFileFilter.JSON)
                        .anyMatch(f -> f.equals(OutputFileFilter.valueOf(this.outputFileTypeFilter)))) {
                    writeFile(directory, "json", json.getBytes(charset));
                }

                getLog().info("Wrote the schema files to " + outputDirectory.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write the result", e);
        }
    }

    static Charset getCharset(String encoding) throws MojoExecutionException {
        if (StringUtils.isBlank(encoding)) {
            return Charset.defaultCharset();
        }

        Charset charset;

        try {
            charset = Charset.forName(encoding.trim());
        } catch (IllegalCharsetNameException e) {
            throw new MojoExecutionException("encoding parameter does not define a legal charset name", e);
        } catch (UnsupportedCharsetException e) {
            throw new MojoExecutionException("encoding parameter does not define a supported charset", e);
        }

        return charset;
    }

    private void writeFile(Path directory, String type, byte[] contents) throws IOException {
        Path file = Paths.get(directory.toString(), schemaFilename + "." + type);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        Files.write(file, contents,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        if (attachArtifacts) {
            mavenProjectHelper.attachArtifact(mavenProject, type, schemaFilename, file.toFile());
        }
    }

}
