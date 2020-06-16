package io.smallrye.openapi.runtime.scanner.spi;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test that the path is created correctly
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PathMakerTest {
    /**
     * Test method for {@link PathMaker#makePath(java.lang.String[])}.
     */
    @Test
    public void testMakePath() {

        String path = AbstractAnnotationScanner.createPathFromSegments("", "", "");
        Assert.assertEquals("/", path);

        path = AbstractAnnotationScanner.createPathFromSegments("/", "/");
        Assert.assertEquals("/", path);

        path = AbstractAnnotationScanner.createPathFromSegments("", "/bookings");
        Assert.assertEquals("/bookings", path);

        path = AbstractAnnotationScanner.createPathFromSegments("/api", "/bookings");
        Assert.assertEquals("/api/bookings", path);

        path = AbstractAnnotationScanner.createPathFromSegments("api", "bookings");
        Assert.assertEquals("/api/bookings", path);

        path = AbstractAnnotationScanner.createPathFromSegments("/", "/bookings", "{id}");
        Assert.assertEquals("/bookings/{id}", path);
    }
}
