package io.smallrye.openapi.spring;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.ClassType;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Scanner that scan Spring entry points.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringAnnotationScanner implements AnnotationScanner {

    @Override
    public String getName() {
        return "Spring";
    }

    @Override
    public boolean shouldIntrospectClassToSchema(ClassType classType) {
        return true;
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI openApi) {
        return openApi;
    }

}
