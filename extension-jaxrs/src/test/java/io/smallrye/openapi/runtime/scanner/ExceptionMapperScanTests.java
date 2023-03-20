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
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-generation.json",
                test.io.smallrye.openapi.runtime.scanner.javax.TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ResteasyReactiveExceptionMapper.class);
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
                test.io.smallrye.openapi.runtime.scanner.javax.TestResource2.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ResteasyReactiveExceptionMapper.class);
    }

    @Test
    void testJakartaMethodAnnotationOverrideExceptionMapper() throws IOException, JSONException {
        test("responses.exception-mapper-overridden-by-method-annotation-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.TestResource2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler1.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler2.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResteasyReactiveExceptionMapper.class);
    }

    @Test
    void testJakartaExceptionMapperMultipleResponse() throws IOException, JSONException {
        test("responses.exception-mapper-multiple-response-generation.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.TestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExceptionHandler3.class);
    }
}
