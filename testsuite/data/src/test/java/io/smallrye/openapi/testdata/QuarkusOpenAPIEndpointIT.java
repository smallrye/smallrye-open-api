package io.smallrye.openapi.testdata;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
class QuarkusOpenAPIEndpointIT {

    @Test
    void testOpenAPIEndpointResponds() {
        given()
                .when().get("/q/openapi.json")
                .then()
                .body("paths", aMapWithSize(greaterThan(0)));
    }
}
