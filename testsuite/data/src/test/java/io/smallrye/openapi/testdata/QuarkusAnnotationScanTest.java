package io.smallrye.openapi.testdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

@QuarkusTest
class QuarkusAnnotationScanTest {

    private static final Logger LOG = Logger.getLogger(QuarkusAnnotationScanTest.class);

    static void assertJsonEquals(String expectedResource, SmallRyeOpenAPI actual) throws Exception {
        URL resourceUrl = QuarkusAnnotationScanTest.class.getResource(expectedResource);
        assertJsonEquals(resourceUrl, actual);
    }

    static void assertJsonEquals(URL expectedResourceUrl, SmallRyeOpenAPI actual) throws Exception {
        String actualJSON = actual.toJSON();
        LOG.debug(actualJSON);

        JSONAssert.assertEquals(
                Files.readString(Paths.get(expectedResourceUrl.toURI())),
                actualJSON,
                true);
    }

    static SmallRyeOpenAPI scan(IndexView index, Config config) {
        return SmallRyeOpenAPI.builder()
                .defaultRequiredProperties(false)
                .withIndex(index)
                .withConfig(config)
                .build();
    }

    static Config config(Map<String, String> properties) {
        return new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(properties, "unit-test", ConfigSource.DEFAULT_ORDINAL))
                .build();
    }

    static IndexView index(String... classNames) {
        List<Class<?>> classes = new ArrayList<>(classNames.length);

        for (String className : classNames) {
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return Index.of(classes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void testKotlinPropertyName() throws Exception {
        IndexView index = index("io.smallrye.openapi.testdata.kotlin.KotlinBean",
                "io.smallrye.openapi.testdata.kotlin.KotlinLongValue");
        SmallRyeOpenAPI result = scan(index, config(Collections.emptyMap()));
        assertJsonEquals("components.schemas.kotlin-value-class-propname.json", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "io.smallrye.openapi.testdata.kotlin.JavaDeprecatedKotlinBean",
            "io.smallrye.openapi.testdata.kotlin.DeprecatedKotlinBean"
    })
    void testDeprecatedAnnotation(String className) throws Exception {
        IndexView index = index(className);
        SmallRyeOpenAPI result = scan(index, config(Collections.emptyMap()));
        assertJsonEquals("components.schemas.kotlin-deprecated-annotation.json", result);
    }

    @Test
    void testRecordWithPojoPrefixedRecordComponents() throws Exception {
        Index index = Index.of(
                io.smallrye.openapi.testdata.java.records.NonBeanRecord.class,
                io.smallrye.openapi.testdata.java.records.RecordReferencingBean.class);
        SmallRyeOpenAPI result = scan(index, config(Collections.emptyMap()));
        assertJsonEquals("components.schemas.prefixed-record-component-names.json", result);
    }

    @Test
    void testKotlinResourceWithUnwrappedFlowSSE() throws Exception {
        IndexView index = index("io.smallrye.openapi.testdata.kotlin.KotlinResource");
        SmallRyeOpenAPI result = scan(index, config(Collections.emptyMap()));
        assertJsonEquals("components.schemas.kotlin-flow-unwrapped.json", result);
    }

    interface Named<T> {
        @JsonProperty("nombre")
        T name();
    }

    @Test
    void testRecordInheritsInterfacePropertyName() throws Exception {
        @Schema(name = "Widget")
        record Widget(String name, int number) implements Named<String> {
        }

        @Path("widgets")
        class WidgetsResource {

            @GET
            @Operation(summary = "Get a widget")
            public Widget get() {
                return new Widget("foo", 42);
            }
        }

        Index index = Index.of(Named.class, Widget.class, WidgetsResource.class);
        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .withIndex(index)
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableStandardStaticFiles(false)
                .build();

        LOG.debug(result.toJSON());
        var nameModel = result.model().getComponents().getSchemas().get("Widget").getProperties().get("nombre");
        assertNotNull(nameModel);
        assertEquals(List.of(SchemaType.STRING), nameModel.getType());
    }
}
