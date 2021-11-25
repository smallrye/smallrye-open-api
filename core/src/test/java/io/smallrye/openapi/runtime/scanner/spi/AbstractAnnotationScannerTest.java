package io.smallrye.openapi.runtime.scanner.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OperationImpl;

class AbstractAnnotationScannerTest {
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
}
