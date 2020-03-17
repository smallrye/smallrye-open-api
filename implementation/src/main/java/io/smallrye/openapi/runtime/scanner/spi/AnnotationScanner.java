package io.smallrye.openapi.runtime.scanner.spi;

import java.util.List;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassType;

import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;

/**
 * This represent a scanner
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface AnnotationScanner {

    public String getName();

    // Scan using this scanner
    public OpenAPI scan(final AnnotationScannerContext annotationScannerContext, OpenAPI oai);

    // Check if certain annotations and extension is from a this scanner
    public boolean containsScannerAnnotations(List<AnnotationInstance> instances, List<AnnotationScannerExtension> extensions);

    // Check if we should include this while introspecting classes for the schema
    public boolean shouldIntrospectClassToSchema(ClassType classType);
}
