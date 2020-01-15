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
import java.net.URI;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.json.JSONException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.smallrye.openapi.api.OpenApiConfig;
import test.io.smallrye.openapi.runtime.scanner.resources.ParameterResource;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ResourceParameterTests extends OpenApiDataObjectScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #25.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/25
     *
     */
    @Test
    public void testParameterResource() throws IOException, JSONException {
        Index index = indexOf(ParameterResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.simpleSchema.json", result);
    }

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #165.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/165
     *
     */
    @Test
    public void testPrimitiveArraySchema() throws IOException, JSONException {
        Index index = indexOf(PrimitiveArraySchemaTestResource.class,
                PrimitiveArraySchemaTestResource.PrimitiveArrayTestObject.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-schema.json", result);
    }

    @Path("/v1")
    static class PrimitiveArraySchemaTestResource {
        @Schema(name = "PrimitiveArrayTestObject", description = "the REST response class")
        static class PrimitiveArrayTestObject {
            @Schema(required = true, description = "a packed data array")
            private double[] data;

            @Schema(implementation = double.class, type = SchemaType.ARRAY)
            // Type is intentionally different than annotation implementation type
            private float[] data2;
        }

        @GET
        @Operation(summary = "Get an object containing a primitive array")
        @APIResponses({
                @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrimitiveArrayTestObject.class))) })
        public PrimitiveArrayTestObject getResponse() {
            return new PrimitiveArrayTestObject();
        }
    }

    /*************************************************************************/

    @Test
    public void testPrimitiveArrayParameter() throws IOException, JSONException {
        Index index = indexOf(PrimitiveArrayParameterTestResource.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-param.json", result);
    }

    @Path("/v1")
    static class PrimitiveArrayParameterTestResource {
        @POST
        @Consumes("application/json")
        @Produces("application/json")
        @Operation(summary = "Convert an array of doubles to an array of floats")
        @APIResponses({
                @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = float[].class))) })
        public float[] doubleToFloat(@SuppressWarnings("unused") double[] input) {
            return new float[0];
        }
    }

    /*************************************************************************/

    @Test
    public void testPrimitiveArrayPolymorphism() throws IOException, JSONException {
        Index index = indexOf(PrimitiveArrayPolymorphismTestResource.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-polymorphism.json", result);
    }

    @Path("/v1")
    static class PrimitiveArrayPolymorphismTestResource {
        @POST
        @Consumes("application/json")
        @Produces("application/json")
        @Operation(summary = "Convert an array of integer types to an array of floating point types")
        @RequestBody(content = @Content(schema = @Schema(anyOf = { int[].class, long[].class })))
        @APIResponses({
                @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                        float[].class, double[].class }))) })
        public Object intToFloat(@SuppressWarnings("unused") Object input) {
            return null;
        }
    }

    /*************************************************************************/

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #201.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/201
     *
     */
    @Test
    public void testSchemaImplementationType() throws IOException, JSONException {
        Index index = indexOf(SchemaImplementationTypeResource.class,
                SchemaImplementationTypeResource.GreetingMessage.class,
                SchemaImplementationTypeResource.SimpleString.class);

        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.string-implementation-wrapped.json", result);
    }

    @Path("/hello")
    static class SchemaImplementationTypeResource {
        static class GreetingMessage {
            @Schema(description = "Used to send a message")
            private final SimpleString message;

            @Schema(implementation = String.class, description = "Simply a string", required = false)
            private SimpleString optionalMessage;

            public GreetingMessage(@JsonProperty SimpleString message) {
                this.message = message;
            }

            public SimpleString getMessage() {
                return message;
            }

            public SimpleString getOptionalMessage() {
                return optionalMessage;
            }
        }

        @Schema(implementation = String.class, title = "A Simple String")
        static class SimpleString {
            @Schema(hidden = true)
            private final String value;

            public SimpleString(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }

        @SuppressWarnings("unused")
        @POST
        @Consumes("application/json")
        @Produces("application/json")
        public Response doPost(GreetingMessage message) {
            return Response.created(URI.create("http://example.com")).build();
        }
    }

    /*************************************************************************/

    /*
     * Test case derived for Smallrye OpenAPI issue #233.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/233
     *
     */
    @Test
    public void testTimeResource() throws IOException, JSONException {
        Index index = indexOf(TimeTestResource.class, TimeTestResource.UTC.class, LocalTime.class, OffsetTime.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.time.json", result);
    }

    @Path("/times")
    @Produces(MediaType.TEXT_PLAIN)
    static class TimeTestResource {

        static class UTC {
            @Schema(description = "Current time at offset '00:00'")
            OffsetTime utc = OffsetTime.now(ZoneId.of("UTC"));
        }

        @Path("local")
        @GET
        public LocalTime getLocalTime() {
            return LocalTime.now();
        }

        @Path("zoned")
        @GET
        public OffsetTime getZonedTime(@QueryParam("zoneId") String zoneId) {
            return OffsetTime.now(ZoneId.of(zoneId));
        }

        @Path("utc")
        @GET
        public UTC getUTC() {
            return new UTC();
        }

        @Path("utc")
        @POST
        public OffsetTime toUTC(@QueryParam("local") LocalTime local, @QueryParam("offsetId") String offsetId) {
            return OffsetTime.of(local, ZoneOffset.of(offsetId));
        }
    }

    /*************************************************************************/

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #237.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/237
     *
     */
    @Test
    public void testTypeVariableResponse() throws IOException, JSONException {
        Index index = indexOf(TypeVariableResponseTestResource.class,
                TypeVariableResponseTestResource.Dto.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.type-variable.json", result);
    }

    @Path("/variable-types")
    @SuppressWarnings("unused")
    static class TypeVariableResponseTestResource<TEST extends TypeVariableResponseTestResource.Dto> {
        static class Dto {
            String id;
        }

        @GET
        public List<TEST> getAll() {
            return null;
        }

        @GET
        @Path("{id}")
        public TEST getOne(@PathParam("id") String id) {
            return null;
        }
    }
}
