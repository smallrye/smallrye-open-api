package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ApiResponseTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    public void testResponseGenerationSuppressedByApiResourcesAnnotation() throws IOException, JSONException {
        test("responses.generation-suppressed-by-api-responses-annotation.json",
                ResponseGenerationSuppressedByApiResourcesAnnotationTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationSuppressedBySuppliedDefaultApiResource() throws IOException, JSONException {
        test("responses.generation-suppressed-by-supplied-default-api-response.json",
                ResponseGenerationSuppressedBySuppliedDefaultApiResourceTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationSuppressedByStatusOmission() throws IOException, JSONException {
        test("responses.generation-suppressed-by-status-omission.json",
                ResponseGenerationSuppressedByStatusOmissionTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationEnabledByIncompleteApiResponse() throws IOException, JSONException {
        test("responses.generation-enabled-by-incomplete-api-response.json",
                ResponseGenerationEnabledByIncompleteApiResponseTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseMultipartGeneration() throws IOException, JSONException {
        test("responses.multipart-generation.json",
                ResponseMultipartGenerationTestResource.class);
    }

    @Test
    public void testVoidPostResponseGeneration() throws IOException, JSONException {
        test("responses.void-post-response-generation.json",
                VoidPostResponseGenerationTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testVoidNonPostResponseGeneration() throws IOException, JSONException {
        test("responses.void-nonpost-response-generation.json",
                VoidNonPostResponseGenerationTestResource.class);
    }

    @Test
    public void testVoidAsyncResponseGeneration() throws IOException, JSONException {
        test("responses.void-async-response-generation.json",
                VoidAsyncResponseGenerationTestResource.class, ServerError.class);
    }

    @Test
    public void testReferenceResponse() throws IOException, JSONException {
        test("responses.component-status-reuse.json",
                ReferenceResponseTestApp.class, ReferenceResponseTestResource.class, JsonObject.class);
    }

    @Test
    public void testGenericTypeVariableResponses() throws IOException, JSONException {
        test("responses.generic-type-variables.json",
                Apple.class, BaseResource.class, TestResource.class);
    }

    @Test
    public void testMultivaluedCustomMapTypeResponse() throws IOException, JSONException {
        @Path("map")
        @SuppressWarnings("unused")
        class Resource {
            class CustomRequest {
                public String requestName;
            }

            class CustomResponse {
                public String responseName;
            }

            @POST
            @Consumes(MediaType.APPLICATION_JSON)
            @Produces(MediaType.APPLICATION_JSON)
            public MultivaluedMap<String, Map<String, CustomResponse>> getMapOfListsOfMaps(
                    MultivaluedMap<String, Map<String, CustomRequest>> request) {
                return null;
            }
        }

        test("responses.nested-parameterized-collection-types.json",
                Resource.class,
                Resource.CustomRequest.class,
                Resource.CustomResponse.class,
                MultivaluedMap.class,
                Collection.class,
                List.class,
                Map.class,
                NavigableMap.class,
                HashMap.class);
    }

    /***************** Test models and resources below. ***********************/

    public static class Pet {
        String id;
        JsonString name;
    }

    static class ServerError {
        String description;
    }

    @Path("pets")
    static class ResponseGenerationSuppressedByApiResourcesAnnotationTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponses(/* Intentionally left blank */)
        public Pet createOrUpdatePet(Pet pet) {
            return pet;
        }
    }

    @Path("pets")
    static class ResponseGenerationSuppressedBySuppliedDefaultApiResourceTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "200", content = {}, description = "Description 200")
        @APIResponse(responseCode = "204", description = "Description 204")
        @APIResponse(responseCode = "400", description = "Description 400")
        public Pet createOrUpdatePet(Pet pet) {
            return pet;
        }
    }

    @Path("pets")
    static class ResponseGenerationSuppressedByStatusOmissionTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "204", description = "Description 204")
        @APIResponse(responseCode = "400", description = "Description 400")
        public Pet createOrUpdatePet(Pet pet) {
            return pet;
        }
    }

    @Path("pets")
    static class ResponseGenerationEnabledByIncompleteApiResponseTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "200")
        @APIResponse(responseCode = "204", description = "Description 204")
        @APIResponse(responseCode = "400", description = "Description 400")
        public Pet createOrUpdatePet(Pet pet) {
            return pet;
        }
    }

    @Path("pets")
    static class ResponseMultipartGenerationTestResource {
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces("multipart/mixed")
        @APIResponse(responseCode = "200")
        @APIResponse(responseCode = "400", description = "Description 400")
        public MultipartOutput getPetWithPicture() {
            return null;
        }
    }

    @Path("pets")
    static class VoidPostResponseGenerationTestResource {
        @SuppressWarnings("unused")
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "201")
        @APIResponse(responseCode = "400", description = "Description 400")
        public void createOrUpdatePet(Pet pet) {
        }
    }

    @Path("pets")
    static class VoidNonPostResponseGenerationTestResource {
        @SuppressWarnings("unused")
        @Path("{id}")
        @DELETE
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "204")
        @APIResponse(responseCode = "400", description = "Description 400")
        public void deletePet(@PathParam("id") String id) {
        }
    }

    @Path("pets/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    static class VoidAsyncResponseGenerationTestResource {
        @SuppressWarnings("unused")
        @GET
        @APIResponse(responseCode = "200")
        @APIResponse(responseCode = "400", description = "Description 400")
        // This test was changed to use the @APIResponseSchema annotation for 2.1.*
        @APIResponse(description = "Server Error: 500", responseCode = "500", content = @Content(schema = @Schema(implementation = ServerError.class)))
        public void getPet(@PathParam("id") String id, @Suspended AsyncResponse response) {
        }

        @SuppressWarnings("unused")
        @DELETE
        public void deletePet(@PathParam("id") String id) {
        }

        @SuppressWarnings("unused")
        @DELETE
        @Path("async")
        public void deletePetAsync(@PathParam("id") String id, @Suspended AsyncResponse response) {
        }
    }

    @OpenAPIDefinition(info = @Info(title = "Test title", version = "0.1"), components = @Components(responses = {
            @APIResponse(responseCode = "404", description = "Not Found!", name = "NotFound"),
            @APIResponse(responseCode = "500", description = "Server Error!", name = "ServerError") }))
    static class ReferenceResponseTestApp extends Application {

    }

    @Path("pets")
    static class ReferenceResponseTestResource {
        @SuppressWarnings("unused")
        @Path("{id}")
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "200")
        @APIResponse(ref = "NotFound")
        @APIResponse(ref = "ServerError")
        public JsonObject getPet(@PathParam("id") String id) {
            return null;
        }
    }

    static class Apple {
        public String name;
    }

    static class BaseResource<T, S> {
        @GET
        @Path("typevar")
        public T test(@QueryParam("q1") S q1) {
            return null;
        }

        @GET
        @Path("map")
        public Map<String, T> getMap() {
            return null;
        }
    }

    @Path("/generic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    static class TestResource extends BaseResource<Apple, String> {
        @POST
        @Path("save")
        public Apple update(Apple filter) {
            return null;
        }
    }
}
