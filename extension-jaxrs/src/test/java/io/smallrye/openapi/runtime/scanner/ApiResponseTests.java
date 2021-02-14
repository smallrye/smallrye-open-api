package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.json.JSONException;
import org.junit.Ignore;
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
    @Ignore
    public void testKeycloakIssue() throws IOException, JSONException {
        test("",
                AccountResource.class,
                PasswordRequest.class,
                UsersResource.class,
                org.keycloak.representations.idm.UserRepresentation.class,
                org.keycloak.representations.idm.CredentialRepresentation.class,
                org.keycloak.representations.idm.FederatedIdentityRepresentation.class,
                org.keycloak.representations.idm.UserConsentRepresentation.class,
                org.keycloak.representations.idm.SocialLinkRepresentation.class,
                org.keycloak.representations.idm.RoleRepresentation.class,
                org.keycloak.common.util.MultivaluedHashMap.class,
                Collection.class,
                List.class,
                Map.class,
                Set.class);
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

    @Tag(name = "Account Management", description = "An API to manage the logged in Account Account.")
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    static class AccountResource {

        @GET
        @Operation(summary = "Get account", description = "Get the logged in account.")
        @APIResponses({
                @APIResponse(responseCode = "200", description = "The logged in account profile"),
                @APIResponse(responseCode = "500", description = "The logged in account profile is either empty or invalid or an internal error occurred")
        })
        public org.keycloak.representations.idm.UserRepresentation getAccount() {
            return null;
        }

        @POST
        @Operation(summary = "Update account", description = "Update the logged in account.")
        public org.keycloak.representations.idm.UserRepresentation updateAccount(
                @RequestBody(description = "The updated user account") org.keycloak.representations.idm.UserRepresentation user) {
            return null;
        }

        @POST
        @Path("/credentials/password")
        @Operation(summary = "Update password", description = "Update the password for logged in account.")
        public void updatePassword(@RequestBody(description = "The password change request") PasswordRequest request) {
        }
    }

    static class PasswordRequest {
        String currentPassword;
        String newPassword;
        String confirmation;

        public PasswordRequest() {
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmation() {
            return confirmation;
        }

        public void setConfirmation(String confirmation) {
            this.confirmation = confirmation;
        }
    }

    @Tag(name = "Users Management", description = "An API to manage users.")
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    static class UsersResource {
        @GET
        @Operation(summary = "Get users", description = "Get all users.")
        @APIResponses({
                @APIResponse(responseCode = "200", description = "The list of Users"),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public Collection<org.keycloak.representations.idm.UserRepresentation> getUsers() {
            return null;
        }

        @GET
        @Path("{id}")
        @Operation(summary = "Get user", description = "Get the user.")
        @Consumes(MediaType.TEXT_PLAIN)
        @APIResponses({
                @APIResponse(responseCode = "200", description = "The Account"),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public org.keycloak.representations.idm.UserRepresentation getUser(
                @Parameter(description = "The id of the user") @PathParam("id") String id) {
            return null;
        }

        @POST
        @Operation(summary = "Create user", description = "Create user.")
        @APIResponses({
                @APIResponse(responseCode = "201", description = "Creation succeeded"),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public Response createUser(
                @RequestBody(description = "The user") org.keycloak.representations.idm.UserRepresentation user) {
            return null;
        }

        @PUT
        @Path("{id}")
        @Operation(summary = "Update user", description = "Update user by id.")
        @APIResponses({
                @APIResponse(responseCode = "200", description = "Update succeeded"),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public org.keycloak.representations.idm.UserRepresentation updateUser(
                @Parameter(description = "The id of the user") @PathParam("id") String id,
                @RequestBody(description = "The user") org.keycloak.representations.idm.UserRepresentation user) {
            return null;
        }

        @DELETE
        @Path("{id}")
        @Operation(summary = "Delete users", description = "Delete user by id.")
        @APIResponses({
                @APIResponse(responseCode = "200", description = "Deletion succeeded"),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public Response deleteUser(@Parameter(description = "The id of the user") @PathParam("id") String id) {
            return null;
        }

        @PUT
        @Path("{id}/reset-password")
        @Operation(summary = "Reset password", description = "Reset the old password with new one..")
        public void resetPassword(@Parameter(description = "The id of the user") @PathParam("id") String id,
                @RequestBody(description = "The credentials. Only newPassword is used here.") PasswordRequest credentials) {
        }

        @GET
        @Path("{id}/role-mappings/realm")
        @Operation(summary = "Get realm roles", description = "Get a list of realm roles of the user.")
        @APIResponses({
                @APIResponse(responseCode = "200", description = "The list of Roles."),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public Collection<org.keycloak.representations.idm.RoleRepresentation> getRealmRoleMappings(
                @Parameter(description = "The id of the user") @PathParam("id") String id) {
            return null;
        }

        @POST
        @Path("{id}/role-mappings/realm")
        @Operation(summary = "Add realm roles to user", description = "Add a list of realm roles to the user.")
        @APIResponses({
                @APIResponse(responseCode = "204", description = "Operation successful."),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public void addRealmRoleMappings(@Parameter(description = "The id of the user") @PathParam("id") String id,
                Collection<org.keycloak.representations.idm.RoleRepresentation> roles) {
        }

        @DELETE
        @Path("{id}/role-mappings/realm")
        @Operation(summary = "Delete realm roles", description = "Delete a list of realm roles from the user.")
        @APIResponses({
                @APIResponse(responseCode = "204", description = "Operation successful."),
                @APIResponse(responseCode = "500", description = "An internal error occurred")
        })
        public void deleteRealmRoleMappings(@Parameter(description = "The id of the user") @PathParam("id") String id,
                Collection<org.keycloak.representations.idm.RoleRepresentation> roles) {
        }
    }
}
