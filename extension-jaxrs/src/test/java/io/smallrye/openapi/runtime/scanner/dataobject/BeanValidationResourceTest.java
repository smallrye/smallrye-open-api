package io.smallrye.openapi.runtime.scanner.dataobject;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class BeanValidationResourceTest extends IndexScannerTestBase {

    @Test
    void testJavaxBeanValidationDocument() throws IOException, JSONException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.BVTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.BVTestResourceEntity.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.BVTestContainer.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.TestEnum.class);
        testBeanValidationDocument(index);
    }

    @Test
    void testJakartaBeanValidationDocument() throws IOException, JSONException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.BVTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.BVTestResourceEntity.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.BVTestContainer.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.TestEnum.class);
        testBeanValidationDocument(index);
    }

    void testBeanValidationDocument(Index index) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("dataobject/resource.testBeanValidationDocument.json", result);
    }

    @Test
    void testJavaxInheritedBVConstraints() throws IOException, JSONException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.User.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.BaseUser.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.UserImpl.class);

        testInheritedBVConstraints(index);
    }

    @Test
    void testJakartaInheritedBVConstraints() throws IOException, JSONException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.User.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.BaseUser.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.UserImpl.class);

        testInheritedBVConstraints(index);
    }

    void testInheritedBVConstraints(Index index) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("dataobject/schema.inherited-bv-constraints.json", result);
    }
}
