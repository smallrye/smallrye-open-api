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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;

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

    @Test
    public void testConfiguredSchemaRegistration() throws IOException, JSONException {
        @org.eclipse.microprofile.openapi.annotations.media.Schema(name = "time-bean")
        class TimeBean {
            @SuppressWarnings("unused")
            Instant now;
        }
        @Path("current-time")
        class TimeResource {
            @GET
            @Produces("application/json")
            public TimeBean getCurrentTime() {
                return null;
            }
        }
        Index index = indexOf(TimeBean.class, TimeResource.class);
        Map<String, Object> config = new HashMap<>();
        config.put("mp.openapi.schema." + Instant.class.getName(), "{"
                + "\"name\":\"timestamp\","
                + "\"type\":\"string\","
                + "\"format\":\"date-time\","
                + "\"description\":\"An instant in time\""
                + "}");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(config), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.schema-config-in-components.json", result);
    }

    @Test
    public void testPackageInfoDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/package-info.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/PackageInfoTestApplication.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/PackageInfoTestApplication$PackageInfoTestResource.class");
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testPackageInfoDefinitionScanning.json", result);
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

    @Test
    public void testTagScanning() throws IOException, JSONException {
        Index index = indexOf(TagTestResource1.class, TagTestResource2.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.tags.multilocation.json", result);
    }

    @Test
    public void testTagScanning_OrderGivenAnnotations() throws IOException, JSONException {
        Index index = indexOf(TagTestApp.class, TagTestResource1.class, TagTestResource2.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.tags.ordergiven.annotation.json", result);
    }

    @Test
    public void testTagScanning_OrderGivenStaticFile() throws IOException, JSONException {
        Index index = indexOf(TagTestResource1.class, TagTestResource2.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI scanResult = scanner.scan();
        OpenAPI staticResult = OpenApiParser.parse(new ByteArrayInputStream(
                "{\"info\" : {\"title\" : \"Tag order in static file\",\"version\" : \"1.0.0-static\"},\"tags\": [{\"name\":\"tag3\"},{\"name\":\"tag1\"}]}"
                        .getBytes()),
                Format.JSON);
        OpenApiDocument doc = OpenApiDocument.INSTANCE;
        doc.config(nestingSupportConfig());
        doc.modelFromStaticFile(staticResult);
        doc.modelFromAnnotations(scanResult);
        doc.initialize();
        OpenAPI result = doc.get();
        printToConsole(result);
        assertJsonEquals("resource.tags.ordergiven.staticfile.json", result);
    }

    @Path("/tags1")
    @Tag(name = "tag3", description = "TAG3 from TagTestResource1")
    @SuppressWarnings("unused")
    static class TagTestResource1 {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getValue1() {
            return null;
        }

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        @Tag(name = "tag1", description = "TAG1 from TagTestResource1#postValue")
        public void postValue(String value) {
        }

        @PATCH
        @Consumes(MediaType.TEXT_PLAIN)
        @Tag
        public void patchValue(String value) {
        }
    }

    @Path("/tags2")
    @Tag(description = "This tag will not appear without a name")
    @Tag(name = "tag1", description = "TAG1 from TagTestResource2")
    @Tag(ref = "http://example/com/tag2")
    @SuppressWarnings("unused")
    static class TagTestResource2 {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Tag(name = "tag3", description = "TAG3 from TagTestResource2#getValue1", externalDocs = @ExternalDocumentation(description = "Ext doc from TagTestResource2#getValue1"))
        public String getValue1() {
            return null;
        }

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        public void postValue(String value) {
        }

        @PATCH
        @Consumes(MediaType.TEXT_PLAIN)
        @Tags({
                @Tag, @Tag
        })
        public void patchValue(String value) {
        }
    }

    @ApplicationPath("/tags")
    @OpenAPIDefinition(info = @Info(title = "Testing user-specified tag order", version = "1.0.0"), tags = {
            @Tag(name = "tag3"),
            @Tag(name = "tag1") })
    static class TagTestApp extends Application {
    }
}
