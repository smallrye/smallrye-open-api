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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.media.SchemaImpl;

/**
 * @author eric.wittmann@gmail.com
 */
public class OpenApiAnnotationScannerTest extends OpenApiDataObjectScannerTestBase {

    /**
     * Test method for {@link OpenApiAnnotationScanner#makePath(java.lang.String[])}.
     */
    @Test
    public void testMakePath() {
        String path = OpenApiAnnotationScanner.makePath("", "", "");
        Assert.assertEquals("/", path);

        path = OpenApiAnnotationScanner.makePath("/", "/");
        Assert.assertEquals("/", path);

        path = OpenApiAnnotationScanner.makePath("", "/bookings");
        Assert.assertEquals("/bookings", path);

        path = OpenApiAnnotationScanner.makePath("/api", "/bookings");
        Assert.assertEquals("/api/bookings", path);

        path = OpenApiAnnotationScanner.makePath("api", "bookings");
        Assert.assertEquals("/api/bookings", path);

        path = OpenApiAnnotationScanner.makePath("/", "/bookings", "{id}");
        Assert.assertEquals("/bookings/{id}", path);
    }

    @Test
    public void testHiddenOperationNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/HiddenOperationResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/VisibleOperationResource.class");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testHiddenOperationNotPresent.json", result);
    }

    @Test
    public void testHiddenOperationPathNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/HiddenOperationResource.class");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testHiddenOperationPathNotPresent.json", result);
    }

    @Test
    public void testRequestBodyComponentGeneration() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication$SomeObject.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication$DifferentObject.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication$RequestBodyResource.class");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(customSchemaRegistryConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testRequestBodyComponentGeneration.json", result);
    }

    /**
     * Example of a simple custom schema registry that has only UUID type schema.
     */
    public static class MyCustomSchemaRegistry implements CustomSchemaRegistry {

        @Override
        public void registerCustomSchemas(SchemaRegistry schemaRegistry) {
            Type uuidType = Type.create(componentize(UUID.class.getName()), Kind.CLASS);
            Schema schema = new SchemaImpl();
            schema.setType(Schema.SchemaType.STRING);
            schema.setFormat("uuid");
            schema.setPattern("^[a-f0-9]{8}-?[a-f0-9]{4}-?[1-5][a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}$");
            schema.setTitle("UUID");
            schema.setDescription("Universally Unique Identifier");
            schema.setExample("de8681db-b4d6-4c47-a428-4b959c1c8e9a");
            schemaRegistry.register(uuidType, schema);
        }

    }

    /**
     * Creates a configuration that has defined a custom schema registry.
     * 
     * @return New configuration instance with {@link MyCustomSchemaRegistry}.
     */
    @SuppressWarnings("unchecked")
    private static OpenApiConfig customSchemaRegistryConfig() {
        return new OpenApiConfigImpl(new Config() {

            @Override
            public <T> T getValue(String propertyName, Class<T> propertyType) {
                if (OpenApiConstants.CUSTOM_SCHEMA_REGISTRY_CLASS.equals(propertyName)) {
                    return (T) MyCustomSchemaRegistry.class.getName();
                }
                if (OpenApiConstants.SCHEMA_REFERENCES_ENABLE.equals(propertyName)) {
                    return (T) Boolean.TRUE;
                }
                return null;
            }

            @Override
            public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
                if (OpenApiConstants.CUSTOM_SCHEMA_REGISTRY_CLASS.equals(propertyName)) {
                    return (Optional<T>) Optional.of(MyCustomSchemaRegistry.class.getName());
                }
                if (OpenApiConstants.SCHEMA_REFERENCES_ENABLE.equals(propertyName)) {
                    return (Optional<T>) Optional.of(Boolean.TRUE);
                }
                return Optional.empty();
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return Arrays.asList(OpenApiConstants.CUSTOM_SCHEMA_REGISTRY_CLASS,
                        OpenApiConstants.SCHEMA_REFERENCES_ENABLE);
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                // Not needed for this test case
                return Collections.emptyList();
            }

        });
    }
}
