package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.Assert.assertNull;

import java.io.IOException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * Test the various positions of the {@link java.lang.Deprecated Deprecated} annotation.
 * 
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class DeprecatedAnnotationTest extends IndexScannerTestBase {

    @Path(value = "/deprecated")
    @Produces(value = "application/json")
    @Consumes(value = "application/json")
    @Deprecated
    static class DeprecatedResource {

        @GET
        @Path(value = "/d1")
        public Response getD1() {
            return null;
        }

        @GET
        @Path(value = "/d2")
        public Response getD2() {
            return null;
        }

    }

    @Test
    void testDeprecatedClassSetsOperationsDeprecated() throws IOException, JSONException {
        Index index = Index.of(DeprecatedResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        assertTrue(result.getPaths().getPathItem("/deprecated/d1").getGET().getDeprecated());
        assertTrue(result.getPaths().getPathItem("/deprecated/d2").getGET().getDeprecated());
    }

    /**************************************************************************/

    @Path(value = "/mixed")
    @Produces(value = "application/json")
    @Consumes(value = "application/json")
    static class MixedDeprecationResource {

        @GET
        @Path(value = "/m1")
        public Response getM1() {
            return null;
        }

        @GET
        @Path(value = "/d1")
        @Deprecated
        public Response getD1() {
            return null;
        }

    }

    @Test
    void testDeprecatedMethodSetsOperationsDeprecated() throws IOException, JSONException {
        Index index = Index.of(MixedDeprecationResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        assertNull(result.getPaths().getPathItem("/mixed/m1").getGET().getDeprecated());
        assertTrue(result.getPaths().getPathItem("/mixed/d1").getGET().getDeprecated());
    }

    /**************************************************************************/

    @Path(value = "/params")
    @Produces(value = "application/json")
    @Consumes(value = "application/json")
    static class DeprecatedParamResource {

        @GET
        @Path(value = "/o1")
        public Response getO1(@QueryParam("p1") @Deprecated String p1, @QueryParam("p2") String p2) {
            return null;
        }

        @POST
        @Path(value = "/o2")
        public Response getO2(@FormParam("p1") @Deprecated String p1, @FormParam("p2") String p2) {
            return null;
        }

        @GET
        @Path(value = "/o3")
        public Response getO3(@MatrixParam("p1") @Deprecated String p1, @MatrixParam("p2") String p2) {
            return null;
        }

    }

    @Test
    void testDeprecatedParametersSetDeprecated() throws IOException, JSONException {
        Index index = Index.of(DeprecatedParamResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();

        assertTrue(result.getPaths().getPathItem("/params/o1").getGET().getParameters().get(0).getDeprecated());
        assertNull(result.getPaths().getPathItem("/params/o1").getGET().getParameters().get(1).getDeprecated());

        assertTrue(result.getPaths().getPathItem("/params/o2").getPOST().getRequestBody().getContent()
                .getMediaType("application/json").getSchema().getProperties().get("p1").getDeprecated());
        assertNull(result.getPaths().getPathItem("/params/o2").getPOST().getRequestBody().getContent()
                .getMediaType("application/json").getSchema().getProperties().get("p2").getDeprecated());

        Schema matrixSchema = result.getPaths().getPathItem("/params/o3{o3}").getGET().getParameters().get(0).getSchema();
        assertTrue(matrixSchema.getProperties().get("p1").getDeprecated());
        assertNull(matrixSchema.getProperties().get("p2").getDeprecated());

    }

    /**************************************************************************/
}
