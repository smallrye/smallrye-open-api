package io.smallrye.openapi.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateSchemaMojo extends AbstractMojo {

    /**
     * Directory where to output the schemas.
     * If no path is specified, the schema will be printed to the log.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated/", property = "outputDirectory")
    private File outputDirectory;

    /**
     * Filename of the schema
     * Default to openapi. So the files created will be openapi.yaml and openapi.json.
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

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject mavenProject;

    @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
    private List<String> classpath;

    @Parameter(defaultValue = "false", property = "skip")
    private boolean skip;

    /**
     * Compiled classes of the project.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "classesDir")
    private File classesDir;

    @Parameter(property = "configProperties")
    private File configProperties;

    // Properies as per OpenAPI Config.

    @Parameter(property = "modelReader")
    private String modelReader;

    @Parameter(property = "filter")
    private String filter;

    @Parameter(property = "scanDisabled")
    private Boolean scanDisabled;

    @Parameter(property = "scanPackages")
    private String scanPackages;

    @Parameter(property = "scanClasses")
    private String scanClasses;

    @Parameter(property = "scanExcludePackages")
    private String scanExcludePackages;

    @Parameter(property = "scanExcludeClasses")
    private String scanExcludeClasses;

    @Parameter(property = "servers")
    private List<String> servers;

    @Parameter(property = "pathServers")
    private List<String> pathServers;

    @Parameter(property = "operationServers")
    private List<String> operationServers;

    @Parameter(property = "scanDependenciesDisable")
    private Boolean scanDependenciesDisable;

    @Parameter(property = "scanDependenciesJars")
    private List<String> scanDependenciesJars;

    @Parameter(property = "schemaReferencesEnable")
    private Boolean schemaReferencesEnable;

    @Parameter(property = "customSchemaRegistryClass")
    private String customSchemaRegistryClass;

    @Parameter(property = "applicationPathDisable")
    private Boolean applicationPathDisable;

    @Parameter(property = "openApiVersion")
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

    @Parameter(property = "operationIdStrategy")
    private String operationIdStrategy;

    @Override
    public void execute() throws MojoExecutionException {
        if (!skip) {
            try {
                IndexView index = createIndex();
                OpenApiDocument schema = generateSchema(index);
                write(schema);
            } catch (IOException ex) {
                getLog().error(ex);
                throw new MojoExecutionException("Could not generate OpenAPI Schema", ex); // TODO allow failOnError = false ?
            }
        }
    }

    private IndexView createIndex() throws MojoExecutionException {
        IndexView moduleIndex;
        try {
            moduleIndex = indexModuleClasses();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't compute index", e);
        }
        if (!scanDependenciesDisable()) {
            List<IndexView> indexes = new ArrayList<>();
            indexes.add(moduleIndex);
            for (Object a : mavenProject.getArtifacts()) {
                Artifact artifact = (Artifact) a;
                if (includeDependenciesScopes.contains(artifact.getScope())
                        && includeDependenciesTypes.contains(artifact.getType())) {
                    try {
                        Result result = JarIndexer.createJarIndex(artifact.getFile(), new Indexer(),
                                false, false, false);
                        indexes.add(result.getIndex());
                    } catch (Exception e) {
                        getLog().error("Can't compute index of " + artifact.getFile().getAbsolutePath() + ", skipping", e);
                    }
                }
            }
            return CompositeIndex.create(indexes);
        } else {
            return moduleIndex;
        }
    }

    private boolean scanDependenciesDisable() {
        if (scanDependenciesDisable == null) {
            return false;
        }
        return scanDependenciesDisable;
    }

    // index the classes of this Maven module
    private Index indexModuleClasses() throws IOException {
        Indexer indexer = new Indexer();

        try (Stream<Path> stream = Files.walk(classesDir.toPath())) {

            List<Path> classFiles = stream
                    .filter(path -> path.toString().endsWith(".class"))
                    .collect(Collectors.toList());
            for (Path path : classFiles) {
                indexer.index(Files.newInputStream(path));
            }
        }
        return indexer.complete();
    }

    private OpenApiDocument generateSchema(IndexView index) throws IOException {
        OpenApiConfig openApiConfig = new MavenConfig(getProperties());

        OpenAPI staticModel = generateStaticModel();
        OpenAPI annotationModel = generateAnnotationModel(index, openApiConfig);

        ClassLoader classLoader = getClassLoader();

        OpenAPI readerModel = OpenApiProcessor.modelFromReader(openApiConfig, classLoader);

        OpenApiDocument document = OpenApiDocument.INSTANCE;

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

    private ClassLoader getClassLoader() throws MalformedURLException {
        Set<URL> urls = new HashSet<>();

        for (String element : classpath) {
            urls.add(new File(element).toURI().toURL());
        }

        return URLClassLoader.newInstance(
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());

    }

    private OpenAPI generateAnnotationModel(IndexView indexView, OpenApiConfig openApiConfig) {
        OpenApiAnnotationScanner openApiAnnotationScanner = new OpenApiAnnotationScanner(openApiConfig, indexView);
        return openApiAnnotationScanner.scan();
    }

    private OpenAPI generateStaticModel() throws IOException {
        Path staticFile = getStaticFile();
        if (staticFile != null) {
            try (InputStream is = Files.newInputStream(staticFile);
                    OpenApiStaticFile openApiStaticFile = new OpenApiStaticFile(is, getFormat(staticFile))) {
                return OpenApiProcessor.modelFromStaticFile(openApiStaticFile);
            }
        }
        return null;
    }

    private Path getStaticFile() {
        Path classesPath = classesDir.toPath();

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
        addToPropertyMap(cp, OpenApiConstants.SMALLRYE_SCAN_DEPENDENCIES_JARS, scanDependenciesJars);
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
            map.put(key, values.stream().collect(Collectors.joining(",")));
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

                writeSchemaFile(directory, schemaFilename + ".yaml", yaml.getBytes());
                writeSchemaFile(directory, schemaFilename + ".json", json.getBytes());

                getLog().info("Wrote the schema files to " + outputDirectory.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write the result", e);
        }
    }

    private void writeSchemaFile(Path directory, String filename, byte[] contents) throws IOException {
        Path file = Paths.get(directory.toString(), filename);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        Files.write(file, contents,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static final String META_INF_OPENAPI_YAML = "META-INF/openapi.yaml";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_YAML = "WEB-INF/classes/META-INF/openapi.yaml";
    private static final String META_INF_OPENAPI_YML = "META-INF/openapi.yml";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_YML = "WEB-INF/classes/META-INF/openapi.yml";
    private static final String META_INF_OPENAPI_JSON = "META-INF/openapi.json";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_JSON = "WEB-INF/classes/META-INF/openapi.json";
}
