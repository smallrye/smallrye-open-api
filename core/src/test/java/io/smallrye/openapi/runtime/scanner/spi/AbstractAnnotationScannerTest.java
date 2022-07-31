package io.smallrye.openapi.runtime.scanner.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;

class AbstractAnnotationScannerTest {

    static final class DummyAnnotationScanner extends AbstractAnnotationScanner {
        @Override
        public String getName() {
            return "Dummy";
        }

        @Override
        public OpenAPI scan(AnnotationScannerContext annotationScannerContext, OpenAPI oai) {
            return oai;
        }

        @Override
        public boolean isAsyncResponse(MethodInfo method) {
            return false;
        }

        @Override
        public boolean isPostMethod(MethodInfo method) {
            return false;
        }

        @Override
        public boolean isDeleteMethod(MethodInfo method) {
            return false;
        }

        @Override
        public boolean containsScannerAnnotations(List<AnnotationInstance> instances,
                List<AnnotationScannerExtension> extensions) {
            return false;
        }
    }

    /**
     * Test method for {@link AbstractAnnotationScanner#makePath(String)}.
     */
    @Test
    void testMakePath() {

        String path = AbstractAnnotationScanner.createPathFromSegments("", "", "");
        Assertions.assertEquals("/", path);

        path = AbstractAnnotationScanner.createPathFromSegments("/", "/");
        Assertions.assertEquals("/", path);

        path = AbstractAnnotationScanner.createPathFromSegments("", "/bookings");
        Assertions.assertEquals("/bookings", path);

        path = AbstractAnnotationScanner.createPathFromSegments("/api", "/bookings");
        Assertions.assertEquals("/api/bookings", path);

        path = AbstractAnnotationScanner.createPathFromSegments("api", "bookings");
        Assertions.assertEquals("/api/bookings", path);

        path = AbstractAnnotationScanner.createPathFromSegments("/", "/bookings", "{id}");
        Assertions.assertEquals("/bookings/{id}", path);
    }

    @Test
    void testNoConfiguredProfile() {
        OpenApiConfig config = new OpenApiConfig() {
        };

        OperationImpl operation = new OperationImpl();
        operation.setExtensions(Collections.singletonMap("x-smallrye-profile-external", ""));

        boolean result = AbstractAnnotationScanner.processProfiles(config, operation);

        assertTrue(result);
        assertEquals(0, operation.getExtensions().size());
    }

    @Test
    void testConfiguredIncludeProfile() {
        OpenApiConfig config = new OpenApiConfig() {
            @Override
            public Set<String> getScanProfiles() {
                return Collections.singleton("external");
            }
        };

        OperationImpl operation = new OperationImpl();

        boolean result = AbstractAnnotationScanner.processProfiles(config, operation);
        assertFalse(result);

        operation.setExtensions(Collections.singletonMap("x-smallrye-profile-external", ""));
        result = AbstractAnnotationScanner.processProfiles(config, operation);

        assertTrue(result);
        assertEquals(0, operation.getExtensions().size());
    }

    @Test
    void testConfiguredExcludeProfile() {
        OpenApiConfig config = new OpenApiConfig() {
            @Override
            public Set<String> getScanExcludeProfiles() {
                return Collections.singleton("external");
            }
        };

        OperationImpl operation = new OperationImpl();

        boolean result = AbstractAnnotationScanner.processProfiles(config, operation);
        assertTrue(result);

        operation.setExtensions(Collections.singletonMap("x-smallrye-profile-external", ""));
        result = AbstractAnnotationScanner.processProfiles(config, operation);

        assertFalse(result);
        assertEquals(0, operation.getExtensions().size());
    }

    @ParameterizedTest
    @CsvSource({
            "true,  p1, p1, PATH, true",
            "true,  p1, p1,     , false",
            "true,  p1, p2, PATH, false",
            "false, p1, p2,     , false",
            "true,  p1,   , PATH, false",
            "false, p1,   ,     , false",
            "     , p1, p1, PATH, false"
    })
    void testDefaultIsPathParameter(Boolean allowNaked, String searchParamName, String paramName, Parameter.In paramIn,
            boolean expectedResult) {
        OpenApiConfigImpl config = (OpenApiConfigImpl) IndexScannerTestBase.emptyConfig();
        config.setAllowNakedPathParameter(allowNaked);

        ResourceParameters params = new ResourceParameters();
        params.setOperationParameters(Arrays.asList(new ParameterImpl().name(paramName).in(paramIn)));

        AbstractAnnotationScanner scanner = new DummyAnnotationScanner();
        AnnotationScannerContext context = new AnnotationScannerContext(null, Thread.currentThread().getContextClassLoader(),
                config);
        assertEquals(expectedResult, scanner.isPathParameter(context, searchParamName, params));
    }
}
