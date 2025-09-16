package io.smallrye.openapi.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

/**
 * Schema generation task implementation.
 *
 * <p>
 * See {@link SmallryeOpenApiProperties} for information about the individual options.
 */
@CacheableTask
public class SmallryeOpenApiTask extends DefaultTask implements SmallryeOpenApiProperties {

    private final FileCollection classpath;
    private final FileCollection resourcesSrcDirs;
    private final FileCollection classesDirs;

    /**
     * Directory where to output the schemas. If no path is specified, the schema will be printed to
     * the log.
     */
    private final DirectoryProperty outputDirectory;

    private final Configs properties;

    enum OutputFileFilter {
        ALL,
        YAML,
        JSON
    }

    @Inject
    public SmallryeOpenApiTask(
            SmallryeOpenApiExtension ext,
            ObjectFactory objects,
            ProjectLayout layout,
            FileCollection classpath,
            FileCollection resourcesSrcDirs,
            FileCollection classesDirs) {
        this.classpath = classpath;
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
            IndexView index = new GradleDependencyIndexCreator(getLogger()).createIndex(this);
            SmallRyeOpenAPI openAPI = generateOpenAPI(index, resourcesSrcDirs);
            write(openAPI);
        } catch (Exception ex) {
            // allow failOnError = false ?
            throw new GradleException("Could not generate OpenAPI Schema", ex);
        }
    }

    @Internal
    FileCollection getClasspath() {
        return this.classpath;
    }

    @Internal
    FileCollection getClassesDirs() {
        return this.classesDirs;
    }

    private SmallRyeOpenAPI generateOpenAPI(IndexView index, FileCollection resourcesSrcDirs) {
        return SmallRyeOpenAPI.builder()
                .withConfig(properties.asMicroprofileConfig())
                .withApplicationClassLoader(getClassLoader())
                .withResourceLocator(path -> resourcesSrcDirs.getFiles()
                        .stream()
                        .map(File::toPath)
                        .filter(Files::exists)
                        .map(Path::toString)
                        .map(dirPath -> Paths.get(dirPath, path))
                        .filter(Files::exists)
                        .map(staticFile -> {
                            try {
                                return staticFile.toUri().toURL();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .findFirst()
                        .orElse(null))
                .withIndex(index)
                .build();
    }

    private ClassLoader getClassLoader() {
        URL[] locators = Stream.of(classesDirs, classpath)
                .map(FileCollection::getFiles)
                .flatMap(Collection::stream)
                .map(pathEntry -> {
                    getLogger().debug("Adding {} to annotation scanner class loader", pathEntry);

                    try {
                        return pathEntry.toURI().toURL();
                    } catch (MalformedURLException mue) {
                        throw new UncheckedIOException(mue);
                    }
                })
                .toArray(URL[]::new);

        return URLClassLoader.newInstance(locators, Thread.currentThread().getContextClassLoader());
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

    private void write(SmallRyeOpenAPI openAPI) throws GradleException {
        try {
            String yaml = openAPI.toYAML();
            String json = openAPI.toJSON();
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

            if (Stream.of(OutputFileFilter.ALL, OutputFileFilter.YAML)
                    .anyMatch(f -> f
                            .equals(OutputFileFilter.valueOf(OutputFileFilter.class, properties.outputFileTypeFilter.get())))) {
                writeFile(directory, "yaml", yaml.getBytes(charset));
            }

            if (Stream.of(OutputFileFilter.ALL, OutputFileFilter.JSON)
                    .anyMatch(f -> f
                            .equals(OutputFileFilter.valueOf(OutputFileFilter.class, properties.outputFileTypeFilter.get())))) {
                writeFile(directory, "json", json.getBytes(charset));
            }

            getLogger().info("Wrote the schema files to {}",
                    outputDirectory.get().getAsFile().getAbsolutePath());
        } catch (IOException e) {
            throw new GradleException("Can't write the result", e);
        }
    }

    private void writeFile(Path directory, String type, byte[] contents) throws IOException {
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
    public Property<String> getInfoSummary() {
        return properties.infoSummary;
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
    public Property<String> getOperationIdStrategy() {
        return properties.operationIdStrategy;
    }

    @Input
    @Optional
    @Override
    public Property<OpenApiConfig.DuplicateOperationIdBehavior> getDuplicateOperationIdBehavior() {
        return properties.duplicateOperationIdBehavior;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getScanProfiles() {
        return properties.scanProfiles;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getScanExcludeProfiles() {
        return properties.scanExcludeProfiles;
    }

    @Input
    @Optional
    @Override
    public MapProperty<String, String> getScanResourceClasses() {
        return properties.scanResourceClasses;
    }

    @Input
    @Optional
    @Override
    public Property<String> getOutputFileTypeFilter() {
        return properties.outputFileTypeFilter;
    }

    @Input
    @Optional
    @Override
    public Property<String> getEncoding() {
        return properties.encoding;
    }

    @Input
    @Optional
    @Override
    public ListProperty<String> getIncludeStandardJavaModules() {
        return properties.includeStandardJavaModules;
    }
}
