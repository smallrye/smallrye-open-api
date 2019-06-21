/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.openapi.api.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

/**
 * Some useful methods for creating stuff from ShrinkWrap {@link Archive}s.
 * 
 * @author eric.wittmann@gmail.com
 */
public class ArchiveUtil {
    private static final Logger LOG = Logger.getLogger(ArchiveUtil.class);

    /**
     * Constructor.
     */
    private ArchiveUtil() {
    }

    /**
     * Creates an {@link OpenApiConfig} instance from the given ShrinkWrap archive.
     * 
     * @param archive Shrinkwrap Archive instance
     * @return OpenApiConfig
     */
    public static OpenApiConfig archiveToConfig(Archive<?> archive) {
        try (ShrinkWrapClassLoader cl = new ShrinkWrapClassLoader(archive)) {
            return new OpenApiConfigImpl(ConfigProvider.getConfig(cl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds the static OpenAPI file located in the deployment and, if it exists, returns
     * it as an {@link OpenApiStaticFile}. If not found, returns null. The static file
     * (when not null) contains an {@link InputStream} to the contents of the static file.
     * The caller is responsible for closing this stream.
     * 
     * @param archive Shrinkwrap Archive instance
     * @return OpenApiStaticFile
     */
    public static OpenApiStaticFile archiveToStaticFile(Archive<?> archive) {
        OpenApiStaticFile rval = new OpenApiStaticFile();
        rval.setFormat(Format.YAML);

        // Check for the file in both META-INF and WEB-INF/classes/META-INF
        Node node = archive.get("/META-INF/openapi.yaml");
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.yaml");
        }
        if (node == null) {
            node = archive.get("/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/META-INF/openapi.json");
            rval.setFormat(Format.JSON);
        }
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.json");
            rval.setFormat(Format.JSON);
        }

        if (node == null) {
            return null;
        }

        rval.setContent(node.getAsset().openStream());

        return rval;
    }

    /**
     * Index the ShrinkWrap archive to produce a jandex index.
     * 
     * @param config OpenApiConfig
     * @param archive Shrinkwrap Archive
     * @return indexed classes in Archive
     */
    public static IndexView archiveToIndex(OpenApiConfig config, Archive<?> archive) {
        if (archive == null) {
            throw new RuntimeException("Archive was null!");
        }

        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/CollectionStandin.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/MapStandin.class");
        indexArchive(config, indexer, archive);
        return indexer.complete();
    }

    private static void index(Indexer indexer, String resName) {
        ClassLoader cl = OpenApiAnnotationScanner.class.getClassLoader();
        try (InputStream klazzStream = cl.getResourceAsStream(resName)) {
            indexer.index(klazzStream);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    /**
     * Indexes the given archive.
     * 
     * @param config
     * @param indexer
     * @param archive
     */
    private static void indexArchive(OpenApiConfig config, Indexer indexer, Archive<?> archive) {
        Map<ArchivePath, Node> c = archive.getContent();
        try {
            for (Map.Entry<ArchivePath, Node> each : c.entrySet()) {
                ArchivePath archivePath = each.getKey();
                if (archivePath.get().endsWith(OpenApiConstants.CLASS_SUFFIX)
                        && acceptClassForScanning(config, archivePath.get())) {
                    try (InputStream contentStream = each.getValue().getAsset().openStream()) {
                        LOG.debugv("Indexing asset: {0} from archive: {1}", archivePath.get(), archive.getName());
                        indexer.index(contentStream);
                    }
                    continue;
                }
                if (archivePath.get().endsWith(OpenApiConstants.JAR_SUFFIX)
                        && acceptJarForScanning(config, archivePath.get())) {
                    try (InputStream contentStream = each.getValue().getAsset().openStream()) {
                        JavaArchive jarArchive = ShrinkWrap.create(JavaArchive.class, archivePath.get())
                                .as(ZipImporter.class).importFrom(contentStream).as(JavaArchive.class);
                        indexArchive(config, indexer, jarArchive);
                    }
                    continue;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the given JAR archive (dependency) should be cracked open and indexed
     * along with the rest of the deployment's classes.
     * 
     * @param config
     * @param jarName
     */
    private static boolean acceptJarForScanning(OpenApiConfig config, String jarName) {
        if (config.scanDependenciesDisable()) {
            return false;
        }
        Set<String> scanDependenciesJars = config.scanDependenciesJars();
        String nameOnly = new File(jarName).getName();
        if (scanDependenciesJars.isEmpty() || scanDependenciesJars.contains(nameOnly)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the class represented by the given archive path should be included in
     * the annotation index.
     * 
     * @param config
     * @param archivePath
     */
    private static boolean acceptClassForScanning(OpenApiConfig config, String archivePath) {
        if (archivePath == null) {
            return false;
        }

        Set<String> scanClasses = config.scanClasses();
        Set<String> scanPackages = config.scanPackages();
        Set<String> scanExcludeClasses = config.scanExcludeClasses();
        Set<String> scanExcludePackages = config.scanExcludePackages();
        if (scanClasses.isEmpty() && scanPackages.isEmpty() && scanExcludeClasses.isEmpty() && scanExcludePackages.isEmpty()) {
            return true;
        }

        if (archivePath.startsWith(OpenApiConstants.WEB_ARCHIVE_CLASS_PREFIX)) {
            archivePath = archivePath.substring(OpenApiConstants.WEB_ARCHIVE_CLASS_PREFIX.length());
        }
        String fqcn = archivePath.replaceAll("/", ".").substring(0, archivePath.lastIndexOf(OpenApiConstants.CLASS_SUFFIX));
        String packageName = "";
        if (fqcn.contains(".")) {
            int idx = fqcn.lastIndexOf(".");
            packageName = fqcn.substring(0, idx);
        }

        boolean accept;
        // Includes
        if (scanClasses.isEmpty() && scanPackages.isEmpty()) {
            accept = true;
        } else if (!scanClasses.isEmpty() && scanPackages.isEmpty()) {
            accept = scanClasses.contains(fqcn);
        } else if (scanClasses.isEmpty() && !scanPackages.isEmpty()) {
            accept = scanPackages.contains(packageName);
        } else {
            accept = scanClasses.contains(fqcn) || scanPackages.contains(packageName);
        }
        // Excludes override includes
        if (!scanExcludeClasses.isEmpty() && scanExcludeClasses.contains(fqcn)) {
            accept = false;
        }
        if (!scanExcludePackages.isEmpty() && scanExcludePackages.contains(packageName)) {
            accept = false;
        }
        return accept;
    }

}
