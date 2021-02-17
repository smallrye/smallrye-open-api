package io.smallrye.openapi.runtime.scanner.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test that the path is created correctly
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class PathMakerTest {
    /**
     * Test method for {@link PathMaker#makePath(java.lang.String[])}.
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
}
