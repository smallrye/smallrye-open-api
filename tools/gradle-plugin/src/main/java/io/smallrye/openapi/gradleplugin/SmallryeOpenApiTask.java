package io.smallrye.openapi.gradleplugin;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfig.DuplicateOperationIdBehavior;
import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

/**
 * Schema generation task implementation.
 *
 * <p>
 * See {@link SmallryeOpenApiProperties} for information about the individual options.
 */
@CacheableTask
public class SmallryeOpenApiTask extends DefaultTask implements SmallryeOpenApiProperties {

    private final NamedDomainObjectProvider<Configuration> configProvider;
    private final FileCollection resourcesSrcDirs;
    private final FileCollection classesDirs;

    private static final String META_INF_OPENAPI_YAML = "META-INF/openapi.yaml";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_YAML = "WEB-INF/classes/META-INF/openapi.yaml";
    private static final String META_INF_OPENAPI_YML = "META-INF/openapi.yml";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_YML = "WEB-INF/classes/META-INF/openapi.yml";
    private static final String META_INF_OPENAPI_JSON = "META-INF/openapi.json";
    private static final String WEB_INF_CLASSES_META_INF_OPENAPI_JSON = "WEB-INF/classes/META-INF/openapi.json";

    /**
     * Directory where to output the schemas. If no path is specified, the schema will be printed to
     * the log.
     */
    private final DirectoryProperty outputDirectory;

    private final Configs properties;

    @Inject
    public SmallryeOpenApiTask(
            SmallryeOpenApiExtension ext,
            ObjectFactory objects,
            ProjectLayout layout,
            NamedDomainObjectProvider<Configuration> configProvider,
            FileCollection resourcesSrcDirs,
            FileCollection classesDirs) {
        this.configProvider = configProvider;
        this.resourcesSrcDirs = resourcesSrcDirs;
        this.classesDirs = classesDirs;

        outputDirectory = objects
                .directoryProperty()
                .convention(layout.getBuildDirectory().dir("generated/openapi"));

        properties = new Configs(objects, ext);
    }

    @TaskAction
    public void generate() {
        try {
            clearOutput();

            Configuration config = configProvider.get();

            Set<ResolvedArtifact> dependencies = properties.scanDependenciesDisable.get().booleanValue()
                    ? Collections.emptySet()
                    : config.getResolvedConfiguration().getResolvedArtifacts();

            IndexView index = new GradleDependencyIndexCreator(getLogger()).createIndex(dependencies,
                    classesDirs);
            OpenApiDocument schema = generateSchema(index, resourcesSrcDirs, config);
            write(schema);
        } catch (Exception ex) {
            throw new GradleException(
                    "Could not generate OpenAPI Schema",
                    ex); // TODO allow failOnError = false ?
        }
    }

    private OpenApiDocument generateSchema(
            IndexView index,
            FileCollection resourcesSrcDirs,
            FileCollection config) throws IOException {
        OpenApiConfig openApiConfig = properties.asOpenApiConfig();
        ClassLoader classLoader = getClassLoader(config);

        OpenAPI staticModel = generateStaticModel(openApiConfig, resourcesSrcDirs);
        OpenAPI annotationModel = generateAnnotationModel(index, openApiConfig, SmallryeOpenApiTask.class.getClassLoader());
        OpenAPI readerModel = OpenApiProcessor.modelFromReader(openApiConfig, classLoader);

        OpenApiDocument document = OpenApiDocument.INSTANCE;

        document.reset();
        document.config(openApiConfig);

        if (annotationModel != null) {
            addingModelDebug("annotations", annotationModel);
            document.modelFromAnnotations(annotationModel);
        }
        if (readerModel != null) {
            addingModelDebug("reader", readerModel);
            document.modelFromReader(readerModel);
        }
        if (staticModel != null) {
            addingModelDebug("static", staticModel);
            document.modelFromStaticFile(staticModel);
        }
        document.filter(OpenApiProcessor.getFilter(openApiConfig, classLoader));
        document.initialize();

        return document;
    }

    private void addingModelDebug(String from, OpenAPI model) {
        getLogger().debug("Adding model from {}...", from);
        nullSafeMap("callbacks", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getCallbacks));
        nullSafeMap("examples", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getExamples));
        nullSafeMap("headers", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getHeaders));
        nullSafeMap("links", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getLinks));
        nullSafeMap("parameters", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getParameters));
        nullSafeMap("request bodies", from,
                java.util.Optional.ofNullable(model.getComponents()).map(Components::getRequestBodies));
        nullSafeMap("responses", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getResponses));
        nullSafeMap("schemas", from, java.util.Optional.ofNullable(model.getComponents()).map(Components::getSchemas));
        nullSafeMap("security schemes", from,
                java.util.Optional.ofNullable(model.getComponents()).map(Components::getSecuritySchemes));
        nullSafeColl("servers", from, java.util.Optional.ofNullable(model.getServers()));
        nullSafeMap("path items", from, java.util.Optional.ofNullable(model.getPaths()).map(
                org.eclipse.microprofile.openapi.models.Paths::getPathItems));
        nullSafeColl("security", from, java.util.Optional.ofNullable(model.getSecurity()));
        nullSafeColl("tags", from, java.util.Optional.ofNullable(model.getTags()));
        nullSafeMap("extensions", from, java.util.Optional.ofNullable(model.getExtensions()));
    }

    private void nullSafeMap(String what, String from, java.util.Optional<Map<?, ?>> collection) {
        nullSafe(what, from, collection.map(Map::size));
    }

    private void nullSafeColl(String what, String from, java.util.Optional<Collection<?>> collection) {
        nullSafe(what, from, collection.map(Collection::size));
    }

    private void nullSafe(String what, String from, java.util.Optional<Integer> collection) {
        getLogger().debug("Adding {} {} from {}", collection.map(Object::toString).orElse("<no>"), what, from);
    }

    private ClassLoader getClassLoader(FileCollection config) throws MalformedURLException {
        Set<URL> urls = new HashSet<>();

        for (File dependency : config.getFiles()) {
            getLogger().debug("Adding {} to annotation scanner class loader", dependency);
            urls.add(dependency.toURI().toURL());
        }

        return URLClassLoader.newInstance(
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());
    }

    private OpenAPI generateAnnotationModel(IndexView indexView, OpenApiConfig openApiConfig,
            ClassLoader classLoader) {
        OpenApiAnnotationScanner openApiAnnotationScanner = new OpenApiAnnotationScanner(openApiConfig,
                classLoader, indexView);
        return openApiAnnotationScanner.scan();
    }

    private OpenAPI generateStaticModel(OpenApiConfig openApiConfig, FileCollection resourcesSrcDirs) throws IOException {
        Path staticFile = getStaticFile(resourcesSrcDirs);
        if (staticFile != null) {
            try (InputStream is = Files.newInputStream(staticFile)) {
                try (OpenApiStaticFile openApiStaticFile = new OpenApiStaticFile(is,
                        getFormat(staticFile))) {
                    return OpenApiProcessor.modelFromStaticFile(openApiConfig, openApiStaticFile);
                }
            }
        }
        return null;
    }

    private Path getStaticFile(FileCollection resourcesSrcDirs) {
        Path staticFile = resourcesSrcDirs.getFiles()
                .stream()
                .map(this::getStaticFile)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return staticFile;
    }

    private Path getStaticFile(File dir) {
        getLogger().debug("Checking for static file in {}", dir);
        Path classesPath = dir.toPath();
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

    private void clearOutput() {
        File outputDir = outputDirectory.get().getAsFile();
        deleteRecursively(outputDir);
    }

    private void deleteRecursively(File file) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteRecursively(f);
                }
            }
        }
        file.delete();
    }

    private void write(OpenApiDocument schema) throws GradleException {
        try {
            String yaml = OpenApiSerializer.serialize(schema.get(), Format.YAML);
            String json = OpenApiSerializer.serialize(schema.get(), Format.JSON);
            Path directory = outputDirectory.get().getAsFile().toPath();

            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Charset charset;
            try {
                charset = Charset.forName(properties.encoding.get().trim());
            } catch (IllegalCharsetNameException e) {
                throw new GradleException("encoding parameter does not define a legal charset name", e);
            } catch (UnsupportedCharsetException e) {
                throw new GradleException("encoding parameter does not define a supported charset", e);
            }

            writeSchemaFile(directory, "yaml", yaml.getBytes(charset));

            writeSchemaFile(directory, "json", json.getBytes(charset));

            getLogger().info("Wrote the schema files to {}",
                    outputDirectory.get().getAsFile().getAbsolutePath());
        } catch (IOException e) {
            throw new GradleException("Can't write the result", e);
        }
    }

    private void writeSchemaFile(Path directory, String type, byte[] contents) throws IOException {
        Path file = Paths.get(directory.toString(), properties.schemaFilename.get() + "." + type);
        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        Files.write(
                file,
                contents,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Directory where to output the schemas. If no path is specified, the schema will be printed to
     * the log.
     */
    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @Override
    public RegularFileProperty getConfigProperties() {
        return properties.configProperties;
    }

    @Input
    @Override
    public Property<String> getSchemaFilename() {
        return properties.schemaFilename;
    }

    @Input
    @Override
    public Property<Boolean> getScanDependenciesDisable() {
        return properties.scanDependenciesDisable;
    }

    @Input
    @Optional
    @Override
    public Property<String> getModelReader() {
        return properties.modelReader;
    }

    @Input
    @Optional
    @Override
    public Property<String> getFilter() {
        return properties.filter;
    }

    @Input
    @Optional
    @Override
    public Property<Boolean> getScanDisabled() {
        return properties.scanDisabled;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getScanPackages() {
        return properties.scanPackages;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getScanClasses() {
        return properties.scanClasses;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getScanExcludePackages() {
        return properties.scanExcludePackages;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getScanExcludeClasses() {
        return properties.scanExcludeClasses;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getServers() {
        return properties.servers;
    }

    @Input
    @Optional
    @Override
    public MapProperty<String, String> getPathServers() {
        return properties.pathServers;
    }

    @Input
    @Optional
    @Override
    public MapProperty<String, String> getOperationServers() {
        return properties.operationServers;
    }

    @Input
    @Optional
    @Override
    public Property<String> getCustomSchemaRegistryClass() {
        return properties.customSchemaRegistryClass;
    }

    @Input
    @Override
    public Property<Boolean> getApplicationPathDisable() {
        return properties.applicationPathDisable;
    }

    @Input
    @Override
    public Property<String> getOpenApiVersion() {
        return properties.openApiVersion;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoTitle() {
        return properties.infoTitle;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoVersion() {
        return properties.infoVersion;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoDescription() {
        return properties.infoDescription;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoTermsOfService() {
        return properties.infoTermsOfService;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoContactEmail() {
        return properties.infoContactEmail;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoContactName() {
        return properties.infoContactName;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoContactUrl() {
        return properties.infoContactUrl;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoLicenseName() {
        return properties.infoLicenseName;
    }

    @Input
    @Optional
    @Override
    public Property<String> getInfoLicenseUrl() {
        return properties.infoLicenseUrl;
    }

    @Input
    @Optional
    @Override
    public Property<OperationIdStrategy> getOperationIdStrategy() {
        return properties.operationIdStrategy;
    }

    @Input
    @Optional
    @Override
    public Property<DuplicateOperationIdBehavior> getDuplicateOperationIdBehavior() {
        return properties.duplicateOperationIdBehavior;
    }

    @Input
    @Optional
    @Override
    public SetProperty<String> getScanProfiles() {
        return properties.scanProfiles;
    }

    @Input
    @Optional
    @Override
    public SetProperty<String> getScanExcludeProfiles() {
        return properties.scanExcludeProfiles;
    }

    @Input
    @Optional
    @Override
    public Property<String> getEncoding() {
        return properties.encoding;
    }
}
