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

package io.smallrye.openapi.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

/**
 * Provides some core archive processing functionality.
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
public class OpenApiProcessor {

    /**
     * Creates a MP Config instance from the given ShrinkWrap archive.
     * @param archive
     */
    public static OpenApiConfig configFromArchive(Archive<?> archive) {
        try (ShrinkWrapClassLoader cl = new ShrinkWrapClassLoader(archive)) {
            return new OpenApiConfig(ConfigProvider.getConfig(cl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a static file located in the deployment and, if it exists, parse it and
     * return the resulting model.  If no static file is found, returns null.  If an
     * error is encountered while parsing the file then a runtime exception is
     * thrown.
     * @param config
     * @param archive
     */
    public static OpenAPIImpl modelFromStaticFile(OpenApiConfig config, Archive archive) {
        Format format = Format.YAML;

        // Check for the file in both META-INF and WEB-INF/classes/META-INF
        Node node = archive.get("/META-INF/openapi.yaml");
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/META-INF/openapi.json");
            format = Format.JSON;
        }
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.json");
            format = Format.JSON;
        }

        if (node == null) {
            return null;
        }

        try (InputStream stream = node.getAsset().openStream()) {
            return OpenApiParser.parse(stream, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an {@link OpenAPI} model by scanning the deployment for relevant JAX-RS and
     * OpenAPI annotations.  If scanning is disabled, this method returns null.  If scanning
     * is enabled but no relevant annotations are found, an empty OpenAPI model is returned.
     * @param config
     * @param archive
     */
    public static OpenAPIImpl modelFromAnnotations(OpenApiConfig config, Archive archive) {
        if (config.scanDisable()) {
            return null;
        }

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, archive);
        return scanner.scan();
    }


    /**
     * Instantiate the configured {@link OASModelReader} and invoke it. If no reader is configured, 
     * then return null. If a class is configured but there is an error either instantiating or invoking
     * it, a {@link RuntimeException} is thrown.
     * @param config
     * @param loader
     */
    public static OpenAPIImpl modelFromReader(OpenApiConfig config, ClassLoader loader) {
        String readerClassName = config.modelReader();
        if (readerClassName == null) {
            return null;
        }
        try {
            Class<?> c = loader.loadClass(readerClassName);
            OASModelReader reader = (OASModelReader) c.newInstance();
            return (OpenAPIImpl) reader.buildModel();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiate the {@link OASFilter} configured by the app.
     * @param config
     * @param loader
     */
    public static OASFilter getFilter(OpenApiConfig config, ClassLoader loader) {
        String filterClassName = config.filter();
        if (filterClassName == null) {
            return null;
        }
        try {
            Class<?> c = loader.loadClass(filterClassName);
            return (OASFilter) c.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
