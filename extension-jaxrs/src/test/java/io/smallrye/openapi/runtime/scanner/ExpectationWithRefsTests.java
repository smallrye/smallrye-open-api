package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.Bar;
import test.io.smallrye.openapi.runtime.scanner.entities.BuzzLinkedList;
import test.io.smallrye.openapi.runtime.scanner.entities.EnumContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.EnumRequiredContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.GenericTypeTestContainer;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class ExpectationWithRefsTests extends JaxRsDataObjectScannerTestBase {

    OpenAPI oai;
    SchemaRegistry registry;

    @BeforeEach
    void setupRegistry() {
        oai = context.getOpenApi();
        registry = context.getSchemaRegistry();
    }

    private void testAssertion(Class<?> target, String expectedResourceName) throws IOException, JSONException {
        DotName name = componentize(target.getName());
        Type type = ClassType.create(name, Type.Kind.CLASS);
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, type);

        Schema result = scanner.process();
        registry.register(type, Collections.emptySet(), result);

        printToConsole(oai);
        assertJsonEquals(expectedResourceName, oai);
    }

    private void testAssertion(Class<?> containerClass,
            String targetField,
            String expectedResourceName) throws IOException, JSONException {

        String containerName = containerClass.getName();
        Type parentType = getFieldFromKlazz(containerName, targetField).type();

        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, parentType);

        Schema result = scanner.process();
        registry.register(parentType, Collections.emptySet(), result);

        printToConsole(oai);
        assertJsonEquals(expectedResourceName, oai);
    }

    /**
     * Unresolvable type parameter.
     */
    @Test
    void testUnresolvableWithRefs() throws IOException, JSONException {
        testAssertion(Bar.class, "refsEnabled.unresolvable.expected.json");
    }

    /**
     * Cyclic reference.
     */
    @Test
    void testCycleWithRef() throws IOException, JSONException {
        testAssertion(BuzzLinkedList.class, "refsEnabled.cycle.expected.json");
    }

    @Test
    void testBareEnumWithRef() throws IOException, JSONException {
        testAssertion(EnumContainer.class, "refsEnabled.enum.expected.json");
    }

    @Test
    void testRequiredEnumWithRef() throws IOException, JSONException {
        testAssertion(EnumRequiredContainer.class, "refsEnabled.enumRequired.expected.json");
    }

    @Test
    void testNestedGenericsWithRefs() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "nesting", "refsEnabled.generic.nested.expected.json");
    }

    @Test
    void testComplexNestedGenericsWithRefs() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "complexNesting", "refsEnabled.generic.complexNesting.expected.json");
    }

    @Test
    void testComplexInheritanceGenericsWithRefs() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "complexInheritance",
                "refsEnabled.generic.complexInheritance.expected.json");
    }

    @Test
    void testGenericsWithBoundsWithRef() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "genericWithBounds", "refsEnabled.generic.withBounds.expected.json");
    }

    @Test
    void genericFieldWithRefTest() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "genericContainer", "refsEnabled.generic.fields.expected.json");
    }

    @Test
    void fieldNameOverrideWithRefTest() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "overriddenNames",
                "refsEnabled.generic.fields.overriddenNames.expected.json");
    }

    @Test
    void durationContainer() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "durationContainer", "refsEnabled.duration.fields.expected.json");
    }

    @Test
    void periodContainer() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "periodContainer", "refsEnabled.period.fields.expected.json");
    }
}
