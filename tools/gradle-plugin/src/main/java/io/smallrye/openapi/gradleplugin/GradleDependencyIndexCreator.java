package io.smallrye.openapi.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;

public class GradleDependencyIndexCreator {

    private final Logger logger;

    public GradleDependencyIndexCreator(Logger logger) {
        this.logger = logger;
    }

    IndexView createIndex(Set<ResolvedArtifact> dependencies, FileCollection classesDirs)
            throws Exception {

        List<Entry<ResolvedArtifact, Duration>> indexDurations = new ArrayList<>();
        List<IndexView> indexes = new ArrayList<>();

        for (File f : classesDirs.getFiles()) {
            indexes.add(indexModuleClasses(f));
        }

        for (ResolvedArtifact artifact : dependencies) {
            try {
                if (artifact.getFile().isDirectory()) {
                    // Don't cache local workspace artifacts. Incremental compilation in IDE's would
                    // otherwise use the cached index instead of new one.
                    // Right now, support for incremental compilation inside eclipse is blocked by:
                    // https://github.com/eclipse-m2e/m2e-core/issues/364#issuecomment-939987848
                    // target/classes
                    indexes.add(indexModuleClasses(artifact));
                } else if (artifact.getFile().getName().endsWith(".jar")) {
                    IndexView artifactIndex = logger.isDebugEnabled() ? timedIndex(indexDurations, artifact) : index(artifact);
                    indexes.add(artifactIndex);
                }
            } catch (IOException | ExecutionException e) {
                logger.error(
                        "Can't compute index of {}, skipping", artifact.getFile().getAbsolutePath(),
                        e);
            }
        }
        printIndexDurations(indexDurations);
        return CompositeIndex.create(indexes);
    }

    private Index index(ResolvedArtifact artifact) throws IOException {
        Result result = JarIndexer.createJarIndex(artifact.getFile(), new Indexer(), false,
                false, false);
        return result.getIndex();
    }

    private void printIndexDurations(List<Map.Entry<ResolvedArtifact, Duration>> indexDurations) {
        if (logger.isDebugEnabled()) {
            indexDurations.sort(Map.Entry.comparingByValue());

            indexDurations.forEach(e -> {
                if (e.getValue().toMillis() > 25) {
                    ResolvedArtifact artifact = e.getKey();
                    logger.debug("Indexing took {} for {}, {}, {}, {}, {}", e.getValue(), artifact.getName(),
                            artifact.getExtension(), artifact.getClassifier(), artifact.getType(), artifact.getFile());
                }
            });
        }
    }

    private IndexView timedIndex(
            List<Map.Entry<ResolvedArtifact, Duration>> indexDurations,
            ResolvedArtifact artifact) throws Exception {
        LocalDateTime start = LocalDateTime.now();
        IndexView result = index(artifact);
        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start, end);
        indexDurations.add(new AbstractMap.SimpleEntry<>(artifact, duration));
        return result;
    }

    private Index indexModuleClasses(ResolvedArtifact artifact) throws IOException {
        return indexModuleClasses(artifact.getFile());
    }

    private Index indexModuleClasses(File file) throws IOException {
        Indexer indexer = new Indexer();

        // Check first if the classes directory exists, before attempting to create an index for the
        // classes
        if (file.exists()) {
            try (Stream<Path> stream = Files.walk(file.toPath())) {
                List<Path> classFiles = stream.filter(path -> path.toString().endsWith(".class")).collect(
                        Collectors.toList());
                for (Path path : classFiles) {
                    indexer.index(Files.newInputStream(path));
                }
            }
        }
        return indexer.complete();
    }
}
