package io.smallrye.openapi.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component(role = MavenDependencyIndexCreator.class, instantiationStrategy = "singleton")
public class MavenDependencyIndexCreator {

    private final Cache<String, IndexView> indexCache = CacheBuilder.newBuilder().build();

    private final Set<String> ignoredArtifacts = new HashSet<>();

    @Requirement
    private Logger logger;

    public MavenDependencyIndexCreator() {
        ignoredArtifacts.add("org.graalvm.sdk:graal-sdk");
        ignoredArtifacts.add("org.yaml:snakeyaml");
        ignoredArtifacts.add("org.wildfly.common:wildfly-common");
        ignoredArtifacts.add("com.fasterxml.jackson.core:jackson-core");
        ignoredArtifacts.add("com.fasterxml.jackson.core:jackson-databind");
        ignoredArtifacts.add("io.smallrye.reactive:smallrye-mutiny-vertx-core");
        ignoredArtifacts.add("commons-io:commons-io");
        ignoredArtifacts.add("io.smallrye.reactive:mutiny");
        ignoredArtifacts.add("org.jboss.narayana.jta:narayana-jta");
        ignoredArtifacts.add("org.glassfish.jaxb:jaxb-runtime");
        ignoredArtifacts.add("com.github.ben-manes.caffeine:caffeine");
        ignoredArtifacts.add("org.hibernate.validator:hibernate-validator");
        ignoredArtifacts.add("io.smallrye.config:smallrye-config-core");
        ignoredArtifacts.add("com.thoughtworks.xstream:xstream");
        ignoredArtifacts.add("com.github.javaparser:javaparser-core");
        ignoredArtifacts.add("org.jboss:jandex");
        ignoredArtifacts.add("org.jboss.resteasy:resteasy-core");

        ignoredArtifacts.add("antlr");
        ignoredArtifacts.add("io.netty");
        ignoredArtifacts.add("org.drools");
        ignoredArtifacts.add("net.bytebuddy");
        ignoredArtifacts.add("org.hibernate");
        ignoredArtifacts.add("org.kie");
        ignoredArtifacts.add("org.postgresql");
        ignoredArtifacts.add("org.apache.httpcomponents");
    }

    public IndexView createIndex(MavenProject mavenProject,
            boolean scanDependenciesDisable,
            List<String> includeDependenciesScopes,
            List<String> includeDependenciesTypes,
            List<String> includeStandardJavaModules) {

        List<Map.Entry<Object, Duration>> indexDurations = new ArrayList<>();

        List<File> artifacts = new ArrayList<>();
        String buildOutput = mavenProject.getBuild().getOutputDirectory();
        if (buildOutput != null) {
            logger.debug("Build output: " + buildOutput);
            artifacts.add(new File(buildOutput));
        } else {
            logger.warn("Build output is null!");
        }

        if (!scanDependenciesDisable) {
            mavenProject.getArtifacts()
                    .stream()
                    .filter(artifact -> !isIgnored(artifact, includeDependenciesScopes, includeDependenciesTypes))
                    .map(Artifact::getFile)
                    .filter(Objects::nonNull)
                    .forEach(artifacts::add);
        }

        List<IndexView> indexes = new ArrayList<>();

        if (includeStandardJavaModules != null) {
            for (String moduleName : includeStandardJavaModules) {
                LocalDateTime start = LocalDateTime.now();
                indexes.add(indexJdkModule(moduleName));
                Duration duration = Duration.between(start, LocalDateTime.now());
                indexDurations.add(new AbstractMap.SimpleEntry<>("module:" + moduleName, duration));
            }
        }

        for (File artifact : artifacts) {
            try {
                if (artifact.isDirectory()) {
                    // Don't cache local workspace artifacts. Incremental compilation in IDEs would otherwise use the cached index instead of new one.
                    // Right now, support for incremental compilation inside eclipse is blocked by: https://github.com/eclipse-m2e/m2e-core/issues/364#issuecomment-939987848
                    // target/classes
                    LocalDateTime start = LocalDateTime.now();
                    indexes.add(indexModuleClasses(artifact));
                    Duration duration = Duration.between(start, LocalDateTime.now());
                    indexDurations.add(new AbstractMap.SimpleEntry<>(artifact, duration));
                } else if (artifact.getName().endsWith(".jar")) {
                    IndexView artifactIndex = timeAndCache(indexDurations, artifact, () -> {
                        Result result = JarIndexer.createJarIndex(artifact, new Indexer(),
                                false, false, false);
                        return result.getIndex();
                    });
                    indexes.add(artifactIndex);
                }
            } catch (Exception e) {
                logger.error("Can't compute index of " + artifact.getAbsolutePath() + ", skipping", e);
            }
        }

        printIndexDurations(indexDurations);

        return CompositeIndex.create(indexes);
    }

    private void printIndexDurations(List<Map.Entry<Object, Duration>> indexDurations) {
        if (logger.isDebugEnabled()) {
            logger.debug("Indexed directories/artifacts for annotation scanning:");
            indexDurations.forEach(e -> logger.debug("  " + e.getKey() + " (index time " + e.getValue() + ")"));
        }
    }

    private boolean isIgnored(Artifact artifact, List<String> includeDependenciesScopes,
            List<String> includeDependenciesTypes) {
        if (artifact.getScope() == null && artifact.getFile() != null) {
            // The artifact is the current mavenproject, which can never be ignored
            // file can be null for projects with packaging type pom
            return false;
        }
        return !includeDependenciesScopes.contains(artifact.getScope())
                || !includeDependenciesTypes.contains(artifact.getType())
                || ignoredArtifacts.contains(artifact.getGroupId())
                || ignoredArtifacts.contains(artifact.getGroupId() + ":" + artifact.getArtifactId());
    }

    private IndexView timeAndCache(List<Map.Entry<Object, Duration>> indexDurations, File artifact,
            Callable<IndexView> callable) throws ExecutionException {
        LocalDateTime start = LocalDateTime.now();
        IndexView result = indexCache.get(artifact.getAbsolutePath(), callable);
        LocalDateTime end = LocalDateTime.now();

        Duration duration = Duration.between(start, end);
        indexDurations.add(new AbstractMap.SimpleEntry<>(artifact, duration));

        return result;
    }

    private Index indexJdkModule(String moduleName) {
        Indexer indexer = new Indexer();
        FileSystem jrt = FileSystems.getFileSystem(URI.create("jrt:/"));

        for (Path root : jrt.getRootDirectories()) {
            try (var walker = Files.walk(root)) {
                walker
                        .filter(path -> path.startsWith("/modules/" + moduleName))
                        .filter(path -> path.getFileName().toString().endsWith(".class"))
                        .map(path -> {
                            try {
                                return Files.newInputStream(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        })
                        .forEach(stream -> {
                            try {
                                indexer.index(stream);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return indexer.complete();
    }

    // index the classes of this Maven module
    private Index indexModuleClasses(File artifact) throws IOException {
        // Check first if the classes directory exists, before attempting to create an index for the classes
        if (artifact.exists()) {
            try (Stream<Path> stream = Files.walk(artifact.toPath())) {
                File[] classFiles = stream
                        .filter(path -> path.toString().endsWith(".class"))
                        .map(Path::toFile)
                        .toArray(File[]::new);
                return Index.of(classFiles);
            }
        } else {
            logger.warn("Module directory does not exist: " + artifact);
            return Index.of(Collections.emptyList());
        }
    }

}
