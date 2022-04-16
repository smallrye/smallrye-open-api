package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class ResourceInheritanceTests extends JaxRsDataObjectScannerTestBase {

    /*
     * Test case derived from original example linked from Smallrye OpenAPI
     * issue #184.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/184
     * https://github.com/quarkusio/quarkus/issues/4298
     *
     */
    @Test
    void testJavaxInheritedResourceMethod() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.GenericResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ExampleResource1.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ExampleResource2.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Conversation.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Greetable.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Greetable.GreetingBean.class);

        testInheritedResourceMethod(i);
    }

    @Test
    void testJakartaInheritedResourceMethod() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.GenericResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExampleResource1.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExampleResource2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Conversation.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Greetable.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Greetable.GreetingBean.class);

        testInheritedResourceMethod(i);
    }

    void testInheritedResourceMethod(Index i) throws IOException, JSONException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.inheritance.params.json", result);
    }

}
