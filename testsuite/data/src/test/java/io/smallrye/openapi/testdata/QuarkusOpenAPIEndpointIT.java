package io.smallrye.openapi.testdata;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@DisabledOnOs(value = OS.WINDOWS, disabledReason = """
        dev services uses testcontainers, not supported on Windows
        WARN  [org.tes.doc.DockerClientProviderStrategy] (build-26) windows is currently not supported
        """)
class QuarkusOpenAPIEndpointIT {

    @Test
    void testOpenAPIEndpointResponds() {
        given()
                .when().get("/q/openapi.json")
                .then()
                .log().ifValidationFails()
                .body("paths", aMapWithSize(greaterThan(0)));
    }
}
