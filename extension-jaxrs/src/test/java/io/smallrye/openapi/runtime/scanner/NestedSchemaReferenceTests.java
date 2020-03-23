package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import test.io.smallrye.openapi.runtime.scanner.entities.NestedSchemaParent;
import test.io.smallrye.openapi.runtime.scanner.resources.NestedSchemaOnParameterResource;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class NestedSchemaReferenceTests extends OpenApiDataObjectScannerTestBase {

    @Test
    public void testNestedSchemasAddedToRegistry() throws IOException, JSONException {
        DotName parentName = componentize(NestedSchemaParent.class.getName());
        Type parentType = ClassType.create(parentName, Type.Kind.CLASS);
        OpenAPIImpl oai = new OpenAPIImpl();
        SchemaRegistry registry = SchemaRegistry.newInstance(nestingSupportConfig(), oai, index);

        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, parentType);

        Schema result = scanner.process();
        registry.register(parentType, result);

        printToConsole(oai);
        assertJsonEquals("refsEnabled.nested.schema.family.expected.json", oai);
    }

    @Test
    public void testNestedSchemaOnParameter() throws IOException, JSONException {
        IndexView i = indexOf(NestedSchemaOnParameterResource.class,
                NestedSchemaOnParameterResource.NestedParameterTestParent.class,
                NestedSchemaOnParameterResource.NestedParameterTestChild.class,
                NestedSchemaOnParameterResource.AnotherNestedChildWithSchemaName.class);

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), i);

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
    public void testSimpleNestedSchemaOnParameter() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/FooResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/FooResource$Foo.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/FooResource$Bar.class");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), indexer.complete());

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("refsEnabled.resource.simple.expected.json", result);
    }
}
