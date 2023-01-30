package io.smallrye.openapi.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

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
    @Parameter(defaultValue = OpenApiConstants.OPEN_API_VERSION, property = "openApiVersion")
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
                OpenApiDocument schema = generateSchema(index);
                write(schema);
            } catch (Exception ex) {
                getLog().error(ex);
                throw new MojoExecutionException("Could not generate OpenAPI Schema", ex); // TODO allow failOnError = false ?
            }
        }
    }

    private OpenApiDocument generateSchema(IndexView index) throws IOException, DependencyResolutionRequiredException {
        if (systemPropertyVariables != null) {
            systemPropertyVariables.forEach(System::setProperty);
        }

        OpenApiConfig openApiConfig = new MavenConfig(getProperties());
        ClassLoader classLoader = getClassLoader();

        OpenAPI staticModel = generateStaticModel(openApiConfig);
        OpenAPI annotationModel = generateAnnotationModel(index, openApiConfig, classLoader);
        OpenAPI readerModel = OpenApiProcessor.modelFromReader(openApiConfig, classLoader);

        OpenApiDocument document = OpenApiDocument.newInstance();

        document.reset();
        document.config(openApiConfig);

        if (annotationModel != null) {
            document.modelFromAnnotations(annotationModel);
        }
        if (readerModel != null) {
            document.modelFromReader(readerModel);
        }
        if (staticModel != null) {
            document.modelFromStaticFile(staticModel);
        }
        document.filter(OpenApiProcessor.getFilter(openApiConfig, classLoader));
        document.initialize();

        return document;
    }

    private ClassLoader getClassLoader() throws MalformedURLException, DependencyResolutionRequiredException {
        Set<URL> urls = new HashSet<>();

        for (String element : mavenProject.getCompileClasspathElements()) {
            getLog().debug("Adding " + element + " to annotation scanner class loader");
            urls.add(new File(element).toURI().toURL());
        }

        return URLClassLoader.newInstance(
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());

    }

    private OpenAPI generateAnnotationModel(IndexView indexView, OpenApiConfig openApiConfig, ClassLoader classLoader) {
        OpenApiAnnotationScanner openApiAnnotationScanner = new OpenApiAnnotationScanner(openApiConfig, classLoader, indexView);
        return openApiAnnotationScanner.scan();
    }

    private OpenAPI generateStaticModel(OpenApiConfig openApiConfig) throws IOException {
        Path staticFile = getStaticFile();
        if (staticFile != null) {
            try (InputStream is = Files.newInputStream(staticFile);
                    OpenApiStaticFile openApiStaticFile = new OpenApiStaticFile(is, getFormat(staticFile))) {
                return OpenApiProcessor.modelFromStaticFile(openApiConfig, openApiStaticFile);
            }
        }
        return null;
    }

    private Path getStaticFile() {
        Path classesPath = new File(mavenProject.getBuild().getOutputDirectory()).toPath();

        if (Files.exists(classesPath)) {
            Path resourcePath = Paths.get(classesPath.toString(), META_INF_OPENAPI_YAML);
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
            resourcePath = Paths.get(classesPath.toString(), WEB_INF_CLASSES_META_INF_OPENAPI_YAML);
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
            resourcePath = Paths.get(classesPath.toString(), META_INF_OPENAPI_YML);
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
            resourcePath = Paths.get(classesPath.toString(), WEB_INF_CLASSES_META_INF_OPENAPI_YML);
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
            resourcePath = Paths.get(classesPath.toString(), META_INF_OPENAPI_JSON);
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
            resourcePath = Paths.get(classesPath.toString(), WEB_INF_CLASSES_META_INF_OPENAPI_JSON);
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
        }
        return null;
    }

    private Format getFormat(Path path) {
        if (path.endsWith(".json")) {
            return Format.JSON;
        }
        return Format.YAML;
    }

    private Map<String, String> getProperties() throws IOException {
        // First check if the configProperties is set, if so, load that.
        Map<String, String> cp = new HashMap<>();
        if (configProperties != null && configProperties.exists()) {
            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(configProperties.toPath())) {
                p.load(is);
                cp.putAll((Map) p);
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
        addToPropertyMap(cp, OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_DISABLE, scanDependenciesDisable);
        addToPropertyMap(cp, OpenApiConstants.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS, customSchemaRegistryClass);
        addToPropertyMap(cp, OpenApiConstants.SMALLRYE_APP_PATH_DISABLE, applicationPathDisable);
        addToPropertyMap(cp, OpenApiConstants.VERSION, openApiVersion);
        addToPropertyMap(cp, OpenApiConstants.INFO_TITLE, infoTitle);
        addToPropertyMap(cp, OpenApiConstants.INFO_VERSION, infoVersion);
        addToPropertyMap(cp, OpenApiConstants.INFO_DESCRIPTION, infoDescription);
        addToPropertyMap(cp, OpenApiConstants.INFO_TERMS, infoTermsOfService);
        addToPropertyMap(cp, OpenApiConstants.INFO_CONTACT_EMAIL, infoContactEmail);
        addToPropertyMap(cp, OpenApiConstants.INFO_CONTACT_NAME, infoContactName);
        addToPropertyMap(cp, OpenApiConstants.INFO_CONTACT_URL, infoContactUrl);
        addToPropertyMap(cp, OpenApiConstants.INFO_LICENSE_NAME, infoLicenseName);
        addToPropertyMap(cp, OpenApiConstants.INFO_LICENSE_URL, infoLicenseUrl);
        addToPropertyMap(cp, OpenApiConstants.OPERATION_ID_STRAGEGY, operationIdStrategy);
        addToPropertyMap(cp, OpenApiConstants.SCAN_PROFILES, scanProfiles);
        addToPropertyMap(cp, OpenApiConstants.SCAN_EXCLUDE_PROFILES, scanExcludeProfiles);

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

    private void write(OpenApiDocument schema) throws MojoExecutionException {
        try {
            String yaml = OpenApiSerializer.serialize(schema.get(), Format.YAML);
            String json = OpenApiSerializer.serialize(schema.get(), Format.JSON);
            if (outputDirectory == null) {
                // no destination file specified => print to stdout
                getLog().info(yaml);
            } else {
                Path directory = outputDirectory.toPath();
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory);
                }

                Charset charset = Charset.defaultCharset();

                if (!StringUtils.isBlank(encoding)) {
                    try {
                        charset = Charset.forName(encoding.trim());
                    } catch (IllegalCharsetNameException e) {
                        throw new MojoExecutionException("encoding parameter does not define a legal charset name", e);
                    } catch (UnsupportedCharsetException e) {
                        throw new MojoExecutionException("encoding parameter does not define a supported charset", e);
                    }
                }

                writeSchemaFile(directory, "yaml", yaml.getBytes(charset));

                writeSchemaFile(directory, "json", json.getBytes(charset));

                getLog().info("Wrote the schema files to " + outputDirectory.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write the result", e);
        }
    }

    private void writeSchemaFile(Path directory, String type, byte[] contents) throws IOException {
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

    private static final String META_INF_OPENAPI_YAML = "META-INF/openapi.yaml";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_YAML = "WEB-INF/classes/META-INF/openapi.yaml";
    private static final String META_INF_OPENAPI_YML = "META-INF/openapi.yml";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_YML = "WEB-INF/classes/META-INF/openapi.yml";
    private static final String META_INF_OPENAPI_JSON = "META-INF/openapi.json";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_JSON = "WEB-INF/classes/META-INF/openapi.json";
}
