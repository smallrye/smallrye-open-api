package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>} and Scott Curtis {@literal <Scott.Curtis@ibm.com>}
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
    public void testResponseGenerationAPIResponseSchemaNoResponseDescription() throws IOException, JSONException {
        test("responses.api-response-schema-no-description.json",
                ResponseGenerationAPIResponseSchemaNoResponseDescriptionTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodePostNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-post.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodePostAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeGetNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-get.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeGetAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodePutNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-put.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodePutAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodePatchAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-patch.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodePatchAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeDeleteAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-delete.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeDeleteAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodePostAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-void-post.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodePostAndVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeGetAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-not-async-void-get.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeGetAndVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodePutAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-not-async-void-put.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodePutAndVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodePatchAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-not-async-void-patch.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodePatchAndVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeDeleteAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-not-async-void-delete.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeDeleteAndVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPostAndVoid() throws IOException, JSONException {
        // no async-specific JSON required to test against 
        // should produce equivalent output to non-async response
        test("responses.api-response-schema-invalid-response-code-void-post.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPostAndVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncGetAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-get.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncGetAndVoidTestResource.class, Pet.class,
                JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPutAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-put.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPutAndVoidTestResource.class, Pet.class,
                JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPatchAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-patch.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPatchAndVoidTestResource.class, Pet.class,
                JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncDeleteAndVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-delete.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncDeleteAndVoidTestResource.class, Pet.class,
                JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPostAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-post.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPostAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncGetAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-get.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncGetAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPutAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-put.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPutAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPatchAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-patch.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPatchAndNonVoidTestResource.class,
                Pet.class, JsonString.class);
    }

    @Test
    public void testResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncDeleteAndNonVoid() throws IOException, JSONException {
        test("responses.api-response-schema-invalid-response-code-delete.json",
                ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncDeleteAndNonVoidTestResource.class,
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
    static class ResponseGenerationAPIResponseSchemaNoResponseDescriptionTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "200")
        public Pet createOrUpdatePet(Pet pet) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodePostAndNonVoidTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet createOrUpdatePet(Pet pet) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeGetAndNonVoidTestResource {
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet getPet(Pet pet) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodePutAndNonVoidTestResource {
        @PUT
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet createOrUpdatePet(Pet pet) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodePatchAndNonVoidTestResource {
        @PATCH
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet updatePet(Pet pet) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeDeleteAndNonVoidTestResource {
        @DELETE
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet deletePet(Pet pet) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodePostAndVoidTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void createOrUpdatePet(Pet pet) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeGetAndVoidTestResource {
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void getPet(Pet pet) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodePutAndVoidTestResource {
        @PUT
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void createOrUpdatePet(Pet pet) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodePatchAndVoidTestResource {
        @PATCH
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void updatePet(Pet pet) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeDeleteAndVoidTestResource {
        @DELETE
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void deletePet(Pet pet) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPostAndVoidTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void createOrUpdatePet(Pet pet, @Suspended AsyncResponse response) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncGetAndVoidTestResource {
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void getPet(Pet pet, @Suspended AsyncResponse response) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPutAndVoidTestResource {
        @PUT
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void createOrUpdatePet(Pet pet, @Suspended AsyncResponse response) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPatchAndVoidTestResource {
        @PATCH
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void updatePet(Pet pet, @Suspended AsyncResponse response) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncDeleteAndVoidTestResource {
        @DELETE
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public void deletePet(Pet pet, @Suspended AsyncResponse response) {
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPostAndNonVoidTestResource {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet createOrUpdatePet(Pet pet, @Suspended AsyncResponse response) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncGetAndNonVoidTestResource {
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet getPet(Pet pet, @Suspended AsyncResponse response) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPutAndNonVoidTestResource {
        @PUT
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet createOrUpdatePet(Pet pet, @Suspended AsyncResponse response) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncPatchAndNonVoidTestResource {
        @PATCH
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet updatePet(Pet pet, @Suspended AsyncResponse response) {
            return new Pet();
        }
    }

    @Path("pets")
    static class ResponseGenerationAPIResponseSchemaInvalidResponseCodeAsyncDeleteAndNonVoidTestResource {
        @DELETE
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponseSchema(value = Pet.class, responseCode = "NaN")
        public Pet deletePet(Pet pet, @Suspended AsyncResponse response) {
            return new Pet();
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

    @Path("pets")
    static class VoidAsyncResponseGenerationTestResource {
        @SuppressWarnings("unused")
        @Path("{id}")
        @GET
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @APIResponse(responseCode = "200")
        @APIResponse(responseCode = "400", description = "Description 400")
        @APIResponseSchema(value = ServerError.class, responseDescription = "Server Error: 500", responseCode = "500")
        public void getPet(@PathParam("id") String id, @Suspended AsyncResponse response) {
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
}
