package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.junit.jupiter.api.Test;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;

/**
 * Test the various positions of the {@link java.lang.Deprecated Deprecated} annotation.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class DeprecatedAnnotationTest extends IndexScannerTestBase {

    @RouteBase(path = "deprecated", consumes = "application/json", produces = "application/json")
    @Deprecated
    static class DeprecatedResource {

        @Route(path = "/d1", methods = HttpMethod.GET)
        public String getD1() {
            return null;
        }

        @Route(path = "/d2", methods = HttpMethod.GET)
        public String getD2() {
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

    @RouteBase(path = "mixed", consumes = "application/json", produces = "application/json")
    static class MixedDeprecationResource {

        @Route(path = "/m1", methods = HttpMethod.GET)
        public String getM1() {
            return null;
        }

        @Route(path = "/d1", methods = HttpMethod.GET)
        @Deprecated
        public String getD1() {
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

    @RouteBase(path = "params", consumes = "application/json", produces = "application/json")
    static class DeprecatedParamResource {

        @Route(path = "/o1", methods = HttpMethod.GET)
        public String getO1(@Param("p1") @Deprecated String p1, @Param("p2") String p2) {
            return null;
        }

    }

    @Test
    void testDeprecatedParametersSetDeprecated() {
        OpenAPI result = scan(DeprecatedParamResource.class);
        printToConsole(result);
        assertTrue(result.getPaths().getPathItem("/params/o1").getGET().getParameters().get(0).getDeprecated());
        assertNull(result.getPaths().getPathItem("/params/o1").getGET().getParameters().get(1).getDeprecated());

    }

    /**************************************************************************/
}