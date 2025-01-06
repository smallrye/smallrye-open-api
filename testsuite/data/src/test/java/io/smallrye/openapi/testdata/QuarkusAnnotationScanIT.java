package io.smallrye.openapi.testdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

class QuarkusAnnotationScanIT {

    private static final Logger LOG = Logger.getLogger(QuarkusAnnotationScanIT.class);

    static void printToConsole(OpenAPI oai) throws IOException {
        // Remember to set debug level logging.
        LOG.debug(OpenApiSerializer.serialize(oai, Format.JSON));
    }

    static void assertJsonEquals(String expectedResource, OpenAPI actual) throws Exception {
        URL resourceUrl = QuarkusAnnotationScanIT.class.getResource(expectedResource);
        assertJsonEquals(resourceUrl, actual);
    }

    static void assertJsonEquals(URL expectedResourceUrl, OpenAPI actual) throws Exception {
        JSONAssert.assertEquals(
                String.join("\n", Files.readAllLines(Paths.get(expectedResourceUrl.toURI()))),
                OpenApiSerializer.serialize(actual, Format.JSON),
                true);
    }

    static OpenApiConfig emptyConfig() {
        return dynamicConfig(Collections.emptyMap());
    }

    static OpenApiConfig dynamicConfig(String key, Object value) {
        Map<String, String> config = new HashMap<>(1);
        config.put(key, value.toString());
        return dynamicConfig(config);
    }

    static OpenApiConfig dynamicConfig(Map<String, String> properties) {
        Config config = new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(properties, "unit-test", ConfigSource.DEFAULT_ORDINAL))
                .build();
        return OpenApiConfig.fromConfig(config);
    }

    @Test
    void testKotlinPropertyName() throws Exception {
        Index index = Index.of(io.smallrye.openapi.testdata.kotlin.KotlinBean.class,
                io.smallrye.openapi.testdata.kotlin.KotlinLongValue.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.kotlin-value-class-propname.json", result);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(classes = {
            io.smallrye.openapi.testdata.kotlin.JavaDeprecatedKotlinBean.class,
            io.smallrye.openapi.testdata.kotlin.DeprecatedKotlinBean.class
    })
    void testDeprecatedAnnotation(Class<?> clazz) throws Exception {
        Index index = Index.of(clazz);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.kotlin-deprecated-annotation.json", result);
    }

    @Test
    void testRecordWithPojoPrefixedRecordComponents() throws Exception {
        Index index = Index.of(
                io.smallrye.openapi.testdata.java.records.NonBeanRecord.class,
                io.smallrye.openapi.testdata.java.records.RecordReferencingBean.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.prefixed-record-component-names.json", result);
    }

    @Test
    void testKotlinResourceWithUnwrappedFlowSSE() throws Exception {
        Index index = Index.of(io.smallrye.openapi.testdata.kotlin.KotlinResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
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
        OpenAPI result = SmallRyeOpenAPI.builder()
                .withIndex(index)
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableStandardStaticFiles(false)
                .build()
                .model();

        printToConsole(result);
        var nameModel = result.getComponents().getSchemas().get("Widget").getProperties().get("nombre");
        assertNotNull(nameModel);
        assertEquals(List.of(SchemaType.STRING), nameModel.getType());
    }

    @Test
    void testSyntheticClassesAndInterfacesIgnoredByDefault() throws Exception {
        try (InputStream source = getClass().getResourceAsStream("/smallrye-open-api-testsuite-data.idx")) {
            IndexReader reader = new IndexReader(source);
            Index index = reader.read();
            OpenAPI result = OpenApiProcessor.bootstrap(
                    dynamicConfig(OASConfig.SCAN_EXCLUDE_PACKAGES,
                            "io.smallrye.openapi.testdata.java.records,io.smallrye.openapi.testdata.kotlin"),
                    index);
            printToConsole(result);
            assertJsonEquals("ignore.synthetic-classes-interfaces.json", result);
        }
    }
}
