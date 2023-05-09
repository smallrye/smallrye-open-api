package io.smallrye.openapi.runtime.scanner;

import static org.jboss.jandex.DotName.createSimple;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import test.io.smallrye.openapi.runtime.scanner.entities.Bar;
import test.io.smallrye.openapi.runtime.scanner.entities.BuzzLinkedList;
import test.io.smallrye.openapi.runtime.scanner.entities.EnumContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.EnumRequiredContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.GenericTypeTestContainer;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
class ExpectationTests extends JaxRsDataObjectScannerTestBase {

    @BeforeEach
    void setup() {
        context.getSchemaRegistry().setDisabled(true);
    }

    /**
     * Unresolvable type parameter.
     */
    @Test
    void testUnresolvable() throws IOException, JSONException {
        DotName bar = createSimple(Bar.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, ClassType.create(bar, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(bar.local(), result);
        assertJsonEquals(bar.local(), "unresolvable.expected.json", result);
    }

    /**
     * Unresolvable type parameter.
     */
    @Test
    void testCycle() throws IOException, JSONException {
        DotName buzz = createSimple(BuzzLinkedList.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, ClassType.create(buzz, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(buzz.local(), result);
        assertJsonEquals(buzz.local(), "cycle.expected.json", result);
    }

    @Test
    void testBareEnum() throws IOException, JSONException {
        DotName baz = createSimple(EnumContainer.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, ClassType.create(baz, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(baz.local(), result);
        assertJsonEquals(baz.local(), "enum.expected.json", result);
    }

    @Test
    void testRequiredEnum() throws IOException, JSONException {
        DotName baz = createSimple(EnumRequiredContainer.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, ClassType.create(baz, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(baz.local(), result);
        assertJsonEquals(baz.local(), "enumRequired.expected.json", result);
    }

    @ParameterizedTest
    @CsvSource({
            "nesting, generic.nested.expected.json",
            "complexNesting, generic.complexNesting.expected.json",
            "complexInheritance, generic.complexInheritance.expected.json",
            "genericWithBounds, generic.withBounds.expected.json",
            "genericContainer, generic.fields.expected.json",
            "overriddenNames, generic.fields.overriddenNames.expected.json"
    })
    void testGenericTypeFields(String fieldName, String expectedResource) throws IOException, JSONException {
        String name = GenericTypeTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, fieldName).type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, pType);
        Schema result = scanner.process();
        printToConsole(name, result);
        assertJsonEquals(name, expectedResource, result);
    }
}
