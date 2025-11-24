package io.smallrye.openapi.testdata;

import static io.restassured.RestAssured.given;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.skyscreamer.jsonassert.JSONAssert;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.openapi.api.SmallRyeOASConfig;

@QuarkusTest
@TestProfile(QuarkusSyntheticTypeIngoreTest.Profile.class)
@DisabledOnOs(value = OS.WINDOWS, disabledReason = """
        dev services uses testcontainers, not supported on Windows
        WARN  [org.tes.doc.DockerClientProviderStrategy] (build-26) windows is currently not supported
        """)
class QuarkusSyntheticTypeIngoreTest {

    // Hack until fixed in Quarkus
    @OpenApiFilter(priority = 0)
    public static class OperationExtCleaner implements OASFilter {
        @Override
        public Operation filterOperation(Operation operation) {
            operation.removeExtension("x-quarkus-openapi-method-ref");
            return operation;
        }
    }

    public static class Profile implements QuarkusTestProfile {
        @Override
        public String getConfigProfile() {
            return "test-synthetic";
        }

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    OASConfig.SCAN_EXCLUDE_PACKAGES, "io.smallrye.openapi.testdata.java.records,"
                            + "io.smallrye.openapi.testdata.kotlin",
                    OASConfig.SCAN_EXCLUDE_CLASSES, "^(.*\\.RecordSchemaTest.*|.*\\.QuarkusAnnotationScanTest.*)$",
                    SmallRyeOASConfig.OPERATION_ID_STRAGEGY, "",
                    "quarkus.smallrye-openapi.info-title", "Generated API",
                    "quarkus.smallrye-openapi.info-version", "1.0",
                    "quarkus.smallrye-openapi.auto-add-operation-summary", "false",
                    "quarkus.smallrye-openapi.auto-add-tags", "false",
                    "quarkus.smallrye-openapi.auto-add-bad-request-response", "false");
        }
    }

    @Test
    void testOpenAPIEndpointResponds() {
        given()
                .when().get("/q/openapi.json")
                .then()
                .log().ifValidationFails()
                .body(new BaseMatcher<String>() {

                    @Override
                    public boolean matches(Object actual) {
                        try {
                            String expected = Files.readString(
                                    Paths.get(getClass().getResource("ignore.synthetic-classes-interfaces.json").toURI()));
                            JSONAssert.assertEquals(expected, String.valueOf(actual), true);
                        } catch (Exception e) {
                            throw new AssertionError(e.toString(), e);
                        }

                        return true;
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                });
    }
}
