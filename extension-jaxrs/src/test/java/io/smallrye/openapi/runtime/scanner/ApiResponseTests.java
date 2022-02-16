package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>} and Scott Curtis {@literal <Scott.Curtis@ibm.com>}
 */
class ApiResponseTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxResponseGenerationSuppressedByApiResourcesAnnotation() throws IOException, JSONException {
        test("responses.generation-suppressed-by-api-responses-annotation.json",
                test.io.smallrye.openapi.runtime.scanner.ResponseGenerationSuppressedByApiResourcesAnnotationTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Pet.class, javax.json.JsonString.class);
    }

    @Test
    void testJakartaResponseGenerationSuppressedByApiResourcesAnnotation() throws IOException, JSONException {
        test("responses.generation-suppressed-by-api-responses-annotation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseGenerationSuppressedByApiResourcesAnnotationTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Pet.class, jakarta.json.JsonString.class);
    }

    @Test
    void testJavaxResponseGenerationSuppressedBySuppliedDefaultApiResource() throws IOException, JSONException {
        test("responses.generation-suppressed-by-supplied-default-api-response.json",
                test.io.smallrye.openapi.runtime.scanner.ResponseGenerationSuppressedBySuppliedDefaultApiResourceTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Pet.class, javax.json.JsonString.class);
    }

    @Test
    void testJakartaResponseGenerationSuppressedBySuppliedDefaultApiResource() throws IOException, JSONException {
        test("responses.generation-suppressed-by-supplied-default-api-response.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseGenerationSuppressedBySuppliedDefaultApiResourceTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Pet.class, jakarta.json.JsonString.class);
    }

    @Test
    void testJavaxResponseGenerationSuppressedByStatusOmission() throws IOException, JSONException {
        test("responses.generation-suppressed-by-status-omission.json",
                test.io.smallrye.openapi.runtime.scanner.ResponseGenerationSuppressedByStatusOmissionTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Pet.class, javax.json.JsonString.class);
    }

    @Test
    void testJakartaResponseGenerationSuppressedByStatusOmission() throws IOException, JSONException {
        test("responses.generation-suppressed-by-status-omission.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseGenerationSuppressedByStatusOmissionTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Pet.class, jakarta.json.JsonString.class);
    }

    @Test
    void testJavaxResponseGenerationEnabledByIncompleteApiResponse() throws IOException, JSONException {
        test("responses.generation-enabled-by-incomplete-api-response.json",
                test.io.smallrye.openapi.runtime.scanner.ResponseGenerationEnabledByIncompleteApiResponseTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Pet.class, javax.json.JsonString.class);
    }

    @Test
    void testJakartaResponseGenerationEnabledByIncompleteApiResponse() throws IOException, JSONException {
        test("responses.generation-enabled-by-incomplete-api-response.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseGenerationEnabledByIncompleteApiResponseTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Pet.class, jakarta.json.JsonString.class);
    }

    @Test
    void testJakartaResponseGenerationJsonExampleApiResourceTestResource() throws IOException, JSONException {
        test("responses.generation-json-example-api-response.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseGenerationJsonExampleApiResourceTestResource.class);
    }

    @Test
    void testJavaxResponseMultipartGeneration() throws IOException, JSONException {
        test("responses.multipart-generation.json",
                test.io.smallrye.openapi.runtime.scanner.ResponseMultipartGenerationTestResource.class);
    }

    @Test
    void testJakartaResponseMultipartGeneration() throws IOException, JSONException {
        test("responses.multipart-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseMultipartGenerationTestResource.class);
    }

    @Test
    void testJavaxVoidPostResponseGeneration() throws IOException, JSONException {
        test("responses.void-post-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.VoidPostResponseGenerationTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Pet.class, javax.json.JsonString.class);
    }

    @Test
    void testJakartaVoidPostResponseGeneration() throws IOException, JSONException {
        test("responses.void-post-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.VoidPostResponseGenerationTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Pet.class, jakarta.json.JsonString.class);
    }

    @Test
    void testJavaxVoidNonPostResponseGeneration() throws IOException, JSONException {
        test("responses.void-nonpost-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.VoidNonPostResponseGenerationTestResource.class);
    }

    @Test
    void testJakartaVoidNonPostResponseGeneration() throws IOException, JSONException {
        test("responses.void-nonpost-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.VoidNonPostResponseGenerationTestResource.class);
    }

    @Test
    void testJavaxVoidAsyncResponseGeneration() throws IOException, JSONException {
        test("responses.void-async-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.VoidAsyncResponseGenerationTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ServerError.class);
    }

    @Test
    void testJakartaVoidAsyncResponseGeneration() throws IOException, JSONException {
        test("responses.void-async-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.VoidAsyncResponseGenerationTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ServerError.class);
    }

    @Test
    void testJavaxReferenceResponse() throws IOException, JSONException {
        test("responses.component-status-reuse.json",
                test.io.smallrye.openapi.runtime.scanner.ReferenceResponseTestApp.class,
                test.io.smallrye.openapi.runtime.scanner.ReferenceResponseTestResource.class, javax.json.JsonObject.class);
    }

    @Test
    void testJakartaReferenceResponse() throws IOException, JSONException {
        test("responses.component-status-reuse.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ReferenceResponseTestApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ReferenceResponseTestResource.class,
                jakarta.json.JsonObject.class);
    }

    @Test
    void testJavaxGenericTypeVariableResponses() throws IOException, JSONException {
        test("responses.generic-type-variables.json",
                test.io.smallrye.openapi.runtime.scanner.Apple.class,
                test.io.smallrye.openapi.runtime.scanner.BaseResource2.class,
                test.io.smallrye.openapi.runtime.scanner.TestResource3.class);
    }

    @Test
    void testJakartaGenericTypeVariableResponses() throws IOException, JSONException {
        test("responses.generic-type-variables.json",
                test.io.smallrye.openapi.runtime.scanner.Apple.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BaseResource2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.TestResource3.class);
    }

    @Test
    void testJavaxMultivaluedCustomMapTypeResponse() throws IOException, JSONException {
        @javax.ws.rs.Path("map")
        @SuppressWarnings("unused")
        class Resource {
            class CustomRequest {
                public String requestName;
            }

            class CustomResponse {
                public String responseName;
            }

            @javax.ws.rs.POST
            @javax.ws.rs.Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
            @javax.ws.rs.Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
            public javax.ws.rs.core.MultivaluedMap<String, Map<String, CustomResponse>> getMapOfListsOfMaps(
                    javax.ws.rs.core.MultivaluedMap<String, Map<String, CustomRequest>> request) {
                return null;
            }
        }

        test("responses.nested-parameterized-collection-types.json",
                Resource.class,
                Resource.CustomRequest.class,
                Resource.CustomResponse.class,
                javax.ws.rs.core.MultivaluedMap.class,
                Collection.class,
                List.class,
                Map.class,
                NavigableMap.class,
                HashMap.class);
    }

    @Test
    void testJakartaMultivaluedCustomMapTypeResponse() throws IOException, JSONException {
        @jakarta.ws.rs.Path("map")
        @SuppressWarnings("unused")
        class Resource {
            class CustomRequest {
                public String requestName;
            }

            class CustomResponse {
                public String responseName;
            }

            @jakarta.ws.rs.POST
            @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            public jakarta.ws.rs.core.MultivaluedMap<String, Map<String, CustomResponse>> getMapOfListsOfMaps(
                    jakarta.ws.rs.core.MultivaluedMap<String, Map<String, CustomRequest>> request) {
                return null;
            }
        }

        test("responses.nested-parameterized-collection-types.json",
                Resource.class,
                Resource.CustomRequest.class,
                Resource.CustomResponse.class,
                jakarta.ws.rs.core.MultivaluedMap.class,
                Collection.class,
                List.class,
                Map.class,
                NavigableMap.class,
                HashMap.class);
    }

    @Test
    void testKotlinContinuationStringResponse() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/hello")
        @jakarta.ws.rs.Consumes({ "text/plain" })
        class Resource {

            /**
             * Java equivalent of Kotlin function
             * <code>
             * suspend fun hello(): String {
             *     return "Hello RESTEasy Reactive"
             * }
             * </code>
             *
             * @param completion maps to Kotlin string return type when using `suspend`
             * @return nothing
             */
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("/async")
            @jakarta.ws.rs.Produces({ "text/plain" })
            public Object hello1(kotlin.coroutines.Continuation<? super String> completion) {
                return null;
            }

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("/sync")
            @jakarta.ws.rs.Produces({ "text/plain" })
            public String hello2() {
                return "Hello";
            }
        }

        test("responses.kotlin-continuation.json",
                Resource.class,
                kotlin.coroutines.Continuation.class,
                kotlin.coroutines.CoroutineContext.class);
    }

    @Test
    void testStandinGenericTypeArgumentResolution() throws IOException, JSONException {
        abstract class Either<L, R> implements Iterable<R> {
            @Override
            public Iterator<R> iterator() {
                return null;
            }
        }

        @jakarta.ws.rs.Path("/")
        class Test {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("/either")
            @jakarta.ws.rs.Produces({ "text/plain" })
            public Either<Integer, String> getEither() {
                return null;
            }
        }
        Index index = indexOf(Test.class, Either.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.standin-generic-type-resolved.json", result);
    }

    @Test
    void testKotlinContinuationOpaqueResponse() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/")
        class Resource {
            @jakarta.ws.rs.POST
            @jakarta.ws.rs.Produces({ "text/plain" })
            @jakarta.ws.rs.Consumes({ "application/json" })
            @jakarta.ws.rs.Path("1")
            public Object wrongRequestBody(kotlin.Pair<String, String> var1,
                    kotlin.coroutines.Continuation<? super jakarta.ws.rs.core.Response> $completion) {
                return null;
            }

            @jakarta.ws.rs.POST
            @jakarta.ws.rs.Produces({ "text/plain" })
            @jakarta.ws.rs.Consumes({ "application/json" })
            @jakarta.ws.rs.Path("2")
            public jakarta.ws.rs.core.Response alsoWrong(kotlin.Pair<String, String> aPair) {
                return null;
            }
        }

        Index index = Index.of(Resource.class, kotlin.Pair.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.kotlin-continuation-opaque.json", result);
    }

    @Test
    void testMutinyUniTypes() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/item")
        class TestResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces({ "text/plain" })
            public io.smallrye.mutiny.Uni<String> getItem() {
                return null;
            }

            @jakarta.ws.rs.DELETE
            public io.smallrye.mutiny.Uni<Void> deleteItem() {
                return null;
            }
        }

        Index index = indexOf(TestResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.mutiny-uni.json", result);
    }

    @Test
    /*
     * Test case for Smallrye OpenAPI issue #1026.
     * 
     * https://github.com/smallrye/smallrye-open-api/issues/1026
     */
    void testAPIResponseSchema() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/item/{id}")
        class TestResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces({ "text/plain", "application/yaml" })
            @APIResponseSchema(responseCode = "200", value = String.class, responseDescription = "Multiple types of string response content")
            public java.util.concurrent.CompletionStage<jakarta.ws.rs.core.Response> getItem() {
                return null;
            }

            @jakarta.ws.rs.PUT
            @jakarta.ws.rs.Produces({ "text/plain", "application/yaml" })
            @APIResponse(responseCode = "202", description = "No response content w/@Produces (content not specified + is not default '200' status)")
            @APIResponseSchema(responseCode = "204", value = void.class, responseDescription = "No response content w/@Produces (schema is void + is not default '200' status)")
            public java.util.concurrent.CompletionStage<jakarta.ws.rs.core.Response> updateItem(String item) {
                return null;
            }

            @jakarta.ws.rs.DELETE
            @APIResponseSchema(responseCode = "202", value = void.class, responseDescription = "No response content w/o @Produces")
            @APIResponse(responseCode = "204", description = "No response content w/o @Produces (content not specified + is not default '200' status)")
            public java.util.concurrent.CompletionStage<jakarta.ws.rs.core.Response> deleteItem() {
                return null;
            }
        }

        Index index = indexOf(TestResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.api-response-schema-variations.json", result);
    }

}
