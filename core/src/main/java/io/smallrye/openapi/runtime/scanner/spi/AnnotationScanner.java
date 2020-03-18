package io.smallrye.openapi.runtime.scanner.spi;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.ClassType;

/**
 * This represent a scanner
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface AnnotationScanner {

    public String getName();

    // Scan using this scanner
    public OpenAPI scan(final AnnotationScannerContext annotationScannerContext, OpenAPI oai);

    // Check if we should include this while introspecting classes for the schema
    public boolean shouldIntrospectClassToSchema(ClassType classType);
}
