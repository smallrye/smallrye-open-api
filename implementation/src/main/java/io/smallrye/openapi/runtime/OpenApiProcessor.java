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

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

/**
 * Provides some core archive processing functionality.
 * 
 * @author eric.wittmann@gmail.com
 */
public class OpenApiProcessor {

    /**
     * Parse the static file content and return the resulting model. Note that this
     * method does NOT close the resources in the static file. The caller is
     * responsible for that.
     * 
     * @param staticFile OpenApiStaticFile to be parsed
     * @return OpenApiImpl
     */
    public static OpenAPIImpl modelFromStaticFile(OpenApiStaticFile staticFile) {
        if (staticFile == null) {
            return null;
        }
        try {
            return OpenApiParser.parse(staticFile.getContent(), staticFile.getFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an {@link OpenAPI} model by scanning the deployment for relevant JAX-RS and
     * OpenAPI annotations. If scanning is disabled, this method returns null. If scanning
     * is enabled but no relevant annotations are found, an empty OpenAPI model is returned.
     * 
     * @param config OpenApiConfig
     * @param index IndexView of Archive
     * @return OpenAPIImpl generated from annotations
     */
    public static OpenAPIImpl modelFromAnnotations(OpenApiConfig config, IndexView index) {
        if (config.scanDisable()) {
            return null;
        }

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);
        return scanner.scan();
    }

    /**
     * Instantiate the configured {@link OASModelReader} and invoke it. If no reader is configured,
     * then return null. If a class is configured but there is an error either instantiating or invoking
     * it, a {@link RuntimeException} is thrown.
     * 
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @return OpenApiImpl created from OASModelReader
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
     * 
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @return OASFilter instance retrieved from loader
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
