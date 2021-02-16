package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.IgnoreSchemaOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.IgnoreTestContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonIgnoreOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonIgnoreTypeExample;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonbTransientOnFieldExample;
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
    void testIgnore_jsonbTransientField() throws IOException, JSONException {
        DotName name = DotName.createSimple(JsonbTransientOnFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.jsonbTransientField.expected.json", result);
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
}
