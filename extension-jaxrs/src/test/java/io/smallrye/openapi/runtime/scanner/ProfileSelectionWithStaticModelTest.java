package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

class ProfileSelectionWithStaticModelTest {

    public static class MyReader implements OASModelReader {
        @Override
        public OpenAPI buildModel() {
            return OASFactory.createOpenAPI()
                    .components(OASFactory.createComponents()
                            .addPathItem("Orders", OASFactory.createPathItem()
                                    .GET(OASFactory.createOperation()
                                            .responses(OASFactory.createAPIResponses()
                                                    .addAPIResponse("default", OASFactory.createAPIResponse()
                                                            .content(OASFactory.createContent()
                                                                    .addMediaType("text/plain", OASFactory.createMediaType()
                                                                            .schema(OASFactory.createSchema()
                                                                                    .type(List.of(SchemaType.STRING))))))))));
        }
    }

    @Test
    void testStaticModelOperationExcluded() throws Exception {
        @Path("/api")
        class MyResource {
            @Path("/users")
            @GET
            @Extension(name = "x-smallrye-profile-public", value = "")
            public List<String> listUsers() {
                return Collections.emptyList();
            }
        }

        SmallRyeOpenAPI result;

        try {
            System.setProperty(SmallRyeOASConfig.SCAN_PROFILES, "public");
            System.setProperty(OASConfig.MODEL_READER, MyReader.class.getName());

            result = SmallRyeOpenAPI.builder()
                    .enableStandardFilter(false)
                    .enableStandardStaticFiles(false)
                    .withCustomStaticFile(() -> {
                        return getClass()
                                .getClassLoader()
                                .getResourceAsStream(
                                        "io/smallrye/openapi/runtime/scanner/static/profile-selection-static-model.yaml");
                    })
                    .enableAnnotationScan(true)
                    .withIndex(Index.of(MyResource.class))
                    .build();
        } finally {
            System.clearProperty(SmallRyeOASConfig.SCAN_PROFILES);
            System.clearProperty(OASConfig.MODEL_READER);
        }

        OpenAPI model = result.model();

        var paths = model.getPaths().getPathItems();
        assertEquals(2, paths.size(), () -> paths.keySet().toString());
        assertTrue(paths.containsKey("/api/users"), () -> paths.keySet().toString());
        assertTrue(paths.containsKey("/api/public/echo"), () -> paths.keySet().toString());

        var componentPaths = model.getComponents().getPathItems();
        assertEquals(1, componentPaths.size());
        var orderOperations = componentPaths.get("Orders").getOperations();
        // removed by filter
        assertTrue(orderOperations.isEmpty());
    }
}
