package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class NestedSchemaReferenceTests extends JaxRsDataObjectScannerTestBase {

    @Test
    void testNestedSchemasAddedToRegistry() throws IOException, JSONException {
        DotName parentName = componentize(test.io.smallrye.openapi.runtime.scanner.entities.NestedSchemaParent.class.getName());
        Type parentType = ClassType.create(parentName, Type.Kind.CLASS);
        OpenAPI oai = context.getOpenApi();
        SchemaRegistry registry = SchemaRegistry.newInstance(context);

        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, parentType);

        Schema result = scanner.process();
        registry.register(parentType, Collections.emptySet(), result);

        printToConsole(oai);
        assertJsonEquals("refsEnabled.nested.schema.family.expected.json", oai);
    }

    @Test
    void testJavaxNestedSchemaOnParameter() throws IOException, JSONException {
        IndexView i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.NestedSchemaOnParameterResource.class,
                test.io.smallrye.openapi.runtime.scanner.resources.javax.NestedSchemaOnParameterResource.NestedParameterTestParent.class,
                test.io.smallrye.openapi.runtime.scanner.resources.javax.NestedSchemaOnParameterResource.NestedParameterTestChild.class,
                test.io.smallrye.openapi.runtime.scanner.resources.javax.NestedSchemaOnParameterResource.AnotherNestedChildWithSchemaName.class);

        testNestedSchemaOnParameter(i);
    }

    @Test
    void testJakartaNestedSchemaOnParameter() throws IOException, JSONException {
        IndexView i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.NestedSchemaOnParameterResource.class,
                test.io.smallrye.openapi.runtime.scanner.resources.jakarta.NestedSchemaOnParameterResource.NestedParameterTestParent.class,
                test.io.smallrye.openapi.runtime.scanner.resources.jakarta.NestedSchemaOnParameterResource.NestedParameterTestChild.class,
                test.io.smallrye.openapi.runtime.scanner.resources.jakarta.NestedSchemaOnParameterResource.AnotherNestedChildWithSchemaName.class);

        testNestedSchemaOnParameter(i);
    }

    void testNestedSchemaOnParameter(IndexView i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("refsEnabled.resource.testNestedSchemaOnParameter.json", result);
    }

    /*
     * Test cast derived from original example in Smallrye OpenAPI issue #73.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/73
     *
     */
    @Test
    void testJavaxSimpleNestedSchemaOnParameter() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/FooResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/FooResource$Foo.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/FooResource$Bar.class");

        testSimpleNestedSchemaOnParameter(indexer.complete());
    }

    @Test
    void testJakartaSimpleNestedSchemaOnParameter() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/FooResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/FooResource$Foo.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/FooResource$Bar.class");

        testSimpleNestedSchemaOnParameter(indexer.complete());
    }

    void testSimpleNestedSchemaOnParameter(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()),
                i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("refsEnabled.resource.simple.expected.json", result);
    }
}
