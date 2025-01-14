package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test the various positions of the {@link java.lang.Deprecated Deprecated} annotation.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class DeprecatedAnnotationTest extends IndexScannerTestBase {

    @RestController
    @RequestMapping(value = "/deprecated", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    static class DeprecatedResource {

        @RequestMapping(value = "/d1", method = RequestMethod.GET)
        public ResponseEntity<?> getD1() {
            return null;
        }

        @RequestMapping(value = "/d2", method = RequestMethod.GET)
        public ResponseEntity<?> getD2() {
            return null;
        }

    }

    @Test
    void testDeprecatedClassSetsOperationsDeprecated() {
        OpenAPI result = scan(DeprecatedResource.class); // NOSONAR
        assertTrue(result.getPaths().getPathItem("/deprecated/d1").getGET().getDeprecated());
        assertTrue(result.getPaths().getPathItem("/deprecated/d2").getGET().getDeprecated());
    }

    /**************************************************************************/

    @RestController
    @RequestMapping(value = "/mixed", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    static class MixedDeprecationResource {

        @RequestMapping(value = "/m1", method = RequestMethod.GET)
        public ResponseEntity<?> getM1() {
            return null;
        }

        @RequestMapping(value = "/d1", method = RequestMethod.GET)
        @Deprecated
        public ResponseEntity<?> getD1() {
            return null;
        }

    }

    @Test
    void testDeprecatedMethodSetsOperationsDeprecated() {
        OpenAPI result = scan(MixedDeprecationResource.class);
        assertNull(result.getPaths().getPathItem("/mixed/m1").getGET().getDeprecated());
        assertTrue(result.getPaths().getPathItem("/mixed/d1").getGET().getDeprecated());
    }

    /**************************************************************************/

    @RestController
    @RequestMapping(value = "/params", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    static class DeprecatedParamResource {

        @RequestMapping(value = "/o1", method = RequestMethod.GET)
        public ResponseEntity<?> getO1(@RequestParam("p1") @Deprecated String p1, @RequestParam("p2") String p2) {
            return null;
        }

        @RequestMapping(value = "/o3", method = RequestMethod.GET)
        public ResponseEntity<?> getO3(@MatrixVariable("p1") @Deprecated String p1, @MatrixVariable("p2") String p2) {
            return null;
        }

    }

    @Test
    void testDeprecatedParametersSetDeprecated() {
        OpenAPI result = scan(DeprecatedParamResource.class);

        assertTrue(result.getPaths().getPathItem("/params/o1").getGET().getParameters().get(0).getDeprecated());
        assertNull(result.getPaths().getPathItem("/params/o1").getGET().getParameters().get(1).getDeprecated());

        Schema matrixSchema = result.getPaths().getPathItem("/params/o3{o3}").getGET().getParameters().get(0).getSchema();
        assertTrue(matrixSchema.getProperties().get("p1").getDeprecated());
        assertNull(matrixSchema.getProperties().get("p2").getDeprecated());

    }

    /**************************************************************************/
}