/**
 * Copyright 2020 Red Hat, Inc, and individual contributors.
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
package io.smallrye.openapi.runtime.scanner.spi;

import java.util.List;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;

/**
 * Context for scanners.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationScannerContext {
    private final FilteredIndexView index;
    private final List<AnnotationScannerExtension> extensions;
    private final OpenApiConfig config;

    public AnnotationScannerContext(FilteredIndexView index, List<AnnotationScannerExtension> extensions,
            OpenApiConfig config) {
        this.index = index;
        this.extensions = extensions;
        this.config = config;
    }

    public FilteredIndexView getIndex() {
        return index;
    }

    public List<AnnotationScannerExtension> getExtensions() {
        return extensions;
    }

    public OpenApiConfig getConfig() {
        return config;
    }

}
