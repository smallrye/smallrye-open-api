package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class ExceptionMapperScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-generation.json",
                test.io.smallrye.openapi.runtime.scanner.TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.ResteasyReactiveExceptionMapper.class);
    }

    @Test
    void testJakartaExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResteasyReactiveExceptionMapper.class);
    }

    @Test
    void testJavaxMethodAnnotationOverrideExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-overridden-by-method-annotation-generation.json",
                test.io.smallrye.openapi.runtime.scanner.TestResource2.class,
                test.io.smallrye.openapi.runtime.scanner.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.ResteasyReactiveExceptionMapper.class);
    }

    @Test
    void testJakartaMethodAnnotationOverrideExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-overridden-by-method-annotation-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.TestResource2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResteasyReactiveExceptionMapper.class);
    }
}
