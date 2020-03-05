/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.openapi.runtime.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerFactory;

/**
 * Scans a deployment (using the archive and jandex annotation index) for JAX-RS and
 * OpenAPI annotations. These annotations, if found, are used to generate a valid
 * OpenAPI model. For reference, see:
 *
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#annotations
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiAnnotationScanner {

    private static final Logger LOG = Logger.getLogger(OpenApiAnnotationScanner.class);

    private final AnnotationScannerContext annotationScannerContext;
    private final AnnotationScannerFactory annotationScannerFactory = new AnnotationScannerFactory();

    /**
     * Constructor.
     * 
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index) {
        this(config, index, Collections.singletonList(new AnnotationScannerExtension() {
        }));
    }

    /**
     * Constructor.
     * 
     * @param config OpenApiConfig instance
     * @param index IndexView of deployment
     * @param extensions A set of extensions to scanning
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, IndexView index, List<AnnotationScannerExtension> extensions) {
        FilteredIndexView filteredIndexView;

        if (index instanceof FilteredIndexView) {
            filteredIndexView = FilteredIndexView.class.cast(index);
        } else {
            filteredIndexView = new FilteredIndexView(index, config);
        }

        this.annotationScannerContext = new AnnotationScannerContext(filteredIndexView, extensions, config);
    }

    /**
     * Scan the deployment for relevant annotations. Returns an OpenAPI data model that was
     * built from those found annotations.
     * 
     * @return OpenAPI generated from scanning annotations
     */
    public OpenAPI scan() {

        List<OpenAPI> scans = new ArrayList<>();
        List<AnnotationScanner> annotationScanners = annotationScannerFactory.getAnnotationScanners();
        for (AnnotationScanner annotationScanner : annotationScanners) {
            LOG.debug("Scanning deployment for OpenAPI and " + annotationScanner.getName() + " Annotations.");
            scans.add(annotationScanner.scan(annotationScannerContext));
        }
        // TODO: Merge all scans.
        return scans.get(0);
    }
}
