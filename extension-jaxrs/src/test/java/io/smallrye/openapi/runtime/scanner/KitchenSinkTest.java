package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.KitchenSink;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
class KitchenSinkTest extends JaxRsDataObjectScannerTestBase {

    private static final Logger LOG = Logger.getLogger(KitchenSinkTest.class);

    /**
     * Test to ensure scanner doesn't choke on various declaration types and patterns.
     *
     * This doesn't have any explicit assertions: it is designed to discover
     * any permutations or configurations that cause exceptions.
     *
     * It is to validate the scanner doesn't break rather than strictly assessing correctness.
     */
    @Test
    void testKitchenSink() throws IOException {
        DotName kitchenSink = DotName.createSimple(KitchenSink.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context,
                ClassType.create(kitchenSink, Type.Kind.CLASS));

        LOG.debugv("Scanning top-level entity: {0}", KitchenSink.class.getName());
        Schema resultSchema = assertDoesNotThrow(() -> scanner.process());
        printToConsole(kitchenSink.local(), resultSchema);
    }

    /**
     * Test parameterized type as a top-level entity (i.e. not just a bare class).
     *
     * @see org.jboss.jandex.ParameterizedType
     */
    @Test
    void testTopLevelParameterisedType() throws IOException {
        // Look up the kitchen sink and get the field named "simpleParameterizedType"
        Type pType = getFieldFromKlazz(KitchenSink.class.getName(), "simpleParameterizedType").type();

        LOG.debugv("Scanning top-level entity: {0}", pType);
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, pType);
        Schema resultSchema = assertDoesNotThrow(() -> scanner.process());
        printToConsole("KustomPair", resultSchema);
    }

    @Test
    public void testKitchenSinkWithRefs() throws IOException, JSONException {
        DotName name = componentize(KitchenSink.class.getName());
        Type type = ClassType.create(name, Type.Kind.CLASS);
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, type);
        OpenAPI oai = context.getOpenApi();
        SchemaRegistry registry = SchemaRegistry.newInstance(context);

        Schema result = scanner.process();
        registry.register(type, result);

        printToConsole(oai);
        assertJsonEquals("refsEnabled.kitchenSink.expected.json", oai);
    }
}
