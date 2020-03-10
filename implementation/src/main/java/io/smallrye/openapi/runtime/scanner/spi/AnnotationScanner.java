package io.smallrye.openapi.runtime.scanner.spi;

import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * This represent a scanner
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface AnnotationScanner {

    public String getName();

    public OpenAPI scan(final AnnotationScannerContext annotationScannerContext, OpenAPI oai);
}
