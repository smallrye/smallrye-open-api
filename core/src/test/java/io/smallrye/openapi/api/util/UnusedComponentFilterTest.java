package io.smallrye.openapi.api.util;

import static org.eclipse.microprofile.openapi.OASFactory.createComponents;
import static org.eclipse.microprofile.openapi.OASFactory.createOpenAPI;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;
import io.smallrye.openapi.model.ReferenceType;

class UnusedComponentFilterTest {

    private static final Config REMOVE_UNUSED_COMPONENTS_CONFIG = new SmallRyeConfigBuilder()
            .withSources(new PropertiesConfigSource(
                    Map.of(SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_COMPONENTS, "*"),
                    "unit-test",
                    ConfigSource.DEFAULT_ORDINAL))
            .build();

    UnusedComponentFilter target;

    @BeforeEach
    void setUp() {
        target = new UnusedComponentFilter();
    }

    @Test
    void testEmptyNoFailureWithoutComponents() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"));

        Assertions.assertDoesNotThrow(() -> {
            SmallRyeOpenAPI.builder()
                    .withInitialModel(initialModel)
                    .withConfig(REMOVE_UNUSED_COMPONENTS_CONFIG)
                    .build()
                    .model();
        });
    }

    @Test
    void testSimpleUnusedComponentsRemoved() {
        OpenAPI initialModel = createOpenAPI()
                .components(createComponents()
                        .addCallback("unused-callback", OASFactory.createCallback())
                        .addExample("unused-example", OASFactory.createExample())
                        .addHeader("unused-header", OASFactory.createHeader())
                        .addLink("unused-link", OASFactory.createLink())
                        .addParameter("unused-parameter", OASFactory.createParameter())
                        .addPathItem("unused-pathitem", OASFactory.createPathItem())
                        .addRequestBody("unused-requestbody", OASFactory.createRequestBody())
                        .addResponse("unused-response", OASFactory.createAPIResponse())
                        .addSchema("unused-schema", OASFactory.createSchema())
                        .addSecurityScheme("unused-securityscheme", OASFactory.createSecurityScheme()));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_COMPONENTS_CONFIG)
                .build()
                .model();

        for (ReferenceType type : ReferenceType.values()) {
            // Map before has size 1, after filter has size 0
            assertEquals(1, type.get(initialModel.getComponents()).size());
            assertEquals(0, type.get(filteredModel.getComponents()).size());
        }
    }

    @Test
    void testUnusedComponentsRemoved() {
        OpenAPI initialModel = createOpenAPI()
                .components(createComponents()
                        .addExample("unused-example", OASFactory.createExample())
                        .addExample("transitive-unused-example", OASFactory.createExample())
                        //
                        .addExample("used-example", OASFactory.createExample())
                        //
                        .addRequestBody("unused-request", OASFactory.createRequestBody()
                                .content(OASFactory.createContent()
                                        .addMediaType("text/plain", OASFactory.createMediaType()
                                                .addExample("request-content-example",
                                                        OASFactory.createExample().ref("transitive-unused-example")))))
                        //
                        .addRequestBody("used-request", OASFactory.createRequestBody()
                                .content(OASFactory.createContent()
                                        .addMediaType("text/plain", OASFactory.createMediaType()
                                                .addExample("request-content-example",
                                                        OASFactory.createExample().ref("used-example"))))))
                //
                .paths(OASFactory.createPaths()
                        .addPathItem("/resource", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .requestBody(OASFactory.createRequestBody().ref("used-request")))));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_COMPONENTS_CONFIG)
                .build()
                .model();

        assertEquals(1, filteredModel.getComponents().getExamples().size());
        assertEquals(Set.of("used-example"), filteredModel.getComponents().getExamples().keySet());

        assertEquals(1, filteredModel.getComponents().getRequestBodies().size());
        assertEquals(Set.of("used-request"), filteredModel.getComponents().getRequestBodies().keySet());

    }
}
