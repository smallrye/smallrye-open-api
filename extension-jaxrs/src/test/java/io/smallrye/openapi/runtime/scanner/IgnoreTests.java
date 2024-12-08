package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import test.io.smallrye.openapi.runtime.scanner.entities.IgnoreSchemaOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.IgnoreTestContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonIgnoreOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonIgnoreTypeExample;
import test.io.smallrye.openapi.runtime.scanner.entities.TransientFieldExample;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
class IgnoreTests extends JaxRsDataObjectScannerTestBase {

    // Always ignore nominated properties when given class is used.
    @Test
    void testIgnore_jsonIgnorePropertiesOnClass() throws IOException, JSONException {
        String name = IgnoreTestContainer.class.getName();
        Type type = getFieldFromKlazz(name, "jipOnClassTest").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, type);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "ignore.jsonIgnorePropertiesOnClass.expected.json", result);
    }

    // Ignore nominated properties of the field in this instance only.
    @Test
    void testIgnore_jsonIgnorePropertiesOnField() throws IOException, JSONException {
        String name = IgnoreTestContainer.class.getName();
        FieldInfo fieldInfo = getFieldFromKlazz(name, "jipOnFieldTest");
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, fieldInfo, fieldInfo.type());

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "ignore.jsonIgnorePropertiesOnField.expected.json", result);
    }

    // Entirely ignore a single field once.
    @Test
    void testIgnore_jsonIgnoreField() throws IOException, JSONException {
        DotName name = DotName.createSimple(JsonIgnoreOnFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.jsonIgnoreField.expected.json", result);
    }

    // Entirely ignore a single field once.
    @Test
    void testIgnore_jsonIgnoreType() throws IOException, JSONException {
        DotName name = DotName.createSimple(JsonIgnoreTypeExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.jsonIgnoreType.expected.json", result);
    }

    // Entirely ignore a single field once using JSON-B.
    @Test
    void testJavaxIgnore_jsonbTransientField() throws IOException, JSONException {
        DotName name = DotName
                .createSimple(
                        test.io.smallrye.openapi.runtime.scanner.entities.javax.JsonbTransientOnFieldExample.class.getName());
        testIgnore_jsonbTransientField(name, "ignore.jsonbTransientField.expected.json");
    }

    @Test
    void testJakartaIgnore_jsonbTransientField() throws IOException, JSONException {
        DotName name = DotName.createSimple(
                test.io.smallrye.openapi.runtime.scanner.entities.jakarta.JsonbTransientOnFieldExample.class.getName());
        testIgnore_jsonbTransientField(name, "ignore.jakartaJsonbTransientField.expected.json");
    }

    void testIgnore_jsonbTransientField(DotName name, String expected) throws IOException, JSONException {
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), expected, result);
    }

    // Entirely ignore a single field once using hidden attribute of Schema.
    @Test
    void testIgnore_schemaHiddenField() throws IOException, JSONException {
        DotName name = DotName.createSimple(IgnoreSchemaOnFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.schemaHiddenField.expected.json", result);
    }

    @Test
    void testIgnore_transientField() throws IOException, JSONException {
        DotName name = DotName.createSimple(TransientFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.transientField.expected.json", result);
    }

    static class BidirectionalJsonIgnoreProperties {
        static class Views {
            public static class Max extends Full {
            }

            public static class Full extends Ingest {
            }

            public static class Ingest extends Abridged {
            }

            public static class Abridged {
            }
        }

        @org.eclipse.microprofile.openapi.annotations.media.Schema
        static class Station {
            @JsonView(Views.Full.class)
            private UUID id;

            @JsonView(Views.Abridged.class)
            private String name;

            @JsonView(Views.Ingest.class)
            @JsonIgnoreProperties("station")
            @org.eclipse.microprofile.openapi.annotations.media.Schema(readOnly = true, description = "Read-only entity details (only returned/used on detail queries).")
            private Set<Base> baseCollection;
        }

        @org.eclipse.microprofile.openapi.annotations.media.Schema
        static class Base {
            @JsonView(Views.Full.class)
            private UUID id;

            @JsonView(Views.Abridged.class)
            private String name;

            @JsonView(Views.Abridged.class)
            @JsonIgnoreProperties("baseCollection")
            private Station station;
        }

        @jakarta.ws.rs.Path("/base")
        static class BaseResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @JsonView(Views.Full.class)
            @APIResponse(responseCode = "200", content = @Content(schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(implementation = Base.class)))
            public jakarta.ws.rs.core.Response getBase() {
                return null;
            }
        }

        @jakarta.ws.rs.Path("/station")
        static class StationResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @JsonView(Views.Full.class)
            @APIResponse(responseCode = "200", content = @Content(schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(implementation = Station.class)))
            public jakarta.ws.rs.core.Response getStation() {
                return null;
            }
        }
    }

    @Test
    void testBidirectionalJsonIgnoreProperties() throws IOException, JSONException {
        Class<?>[] classes = {
                BidirectionalJsonIgnoreProperties.Views.Max.class,
                BidirectionalJsonIgnoreProperties.Views.Full.class,
                BidirectionalJsonIgnoreProperties.Views.Ingest.class,
                BidirectionalJsonIgnoreProperties.Views.Abridged.class,
                BidirectionalJsonIgnoreProperties.Views.class,
                BidirectionalJsonIgnoreProperties.Base.class,
                BidirectionalJsonIgnoreProperties.Station.class,
                BidirectionalJsonIgnoreProperties.BaseResource.class,
                BidirectionalJsonIgnoreProperties.StationResource.class
        };

        assertJsonEquals("ignore.bidirectionalIgnoreProperties.json",
                scan(config(SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_SCHEMAS, "true"), null, classes));
    }
}
