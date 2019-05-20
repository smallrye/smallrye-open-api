/*
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

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Indexer;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ResourceParameterTests extends OpenApiDataObjectScannerTestBase {

    /*
     * Test cast derived from original example in Smallrye OpenAPI issue #25.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/25
     *
     */
    @Test
    public void testParameterResource() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/ParameterResource.class");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.parameters.simpleSchema.json", result);
    }
}
