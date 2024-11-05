package io.smallrye.openapi.runtime.scanner.dataobject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.javax.BVTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.BVTestResourceEntity.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.BVTestContainer.class,
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
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.javax.User.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.BaseUser.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.UserImpl.class);

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

    @Test
    void testJaxRsResponseNestedTypeBVConstraints() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/")
        @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
        @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
        class ValidationResource {
            @jakarta.ws.rs.PUT
            @jakarta.ws.rs.Path("single")
            public void putSingle(@jakarta.validation.constraints.Size(min = 10) String body) {
            }

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("single")
            @jakarta.validation.constraints.Size(min = 10)
            public String getSingle() {
                return "a";
            }

            @jakarta.ws.rs.PUT
            @jakarta.ws.rs.Path("many")
            public void putMany(List<@jakarta.validation.constraints.Size(min = 10) String> body) {
            }

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("many")
            public List<@jakarta.validation.constraints.Size(min = 10) String> getMany() {
                return Arrays.asList("a", "b", "c");
            }

            @jakarta.ws.rs.PUT
            @jakarta.ws.rs.Path("nested")
            public void putNested(
                    Map<String, @jakarta.validation.constraints.NotEmpty Map<String, List<@jakarta.validation.constraints.Size(min = 10) String>>> body) {
            }

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("nested")
            public Map<String, @jakarta.validation.constraints.NotEmpty Map<String, List<@jakarta.validation.constraints.Size(min = 10) String>>> getNested() {
                return null;
            }
        }

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), Index.of(ValidationResource.class));
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("dataobject/schema.type-target-constraints.json", result);
    }
}
