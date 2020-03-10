package io.smallrye.openapi.api.reader;

import org.eclipse.microprofile.openapi.models.Components;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Reading the Components annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#componentsObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ComponentsReader {
    private static final Logger LOG = Logger.getLogger(ComponentsReader.class);

    private ComponentsReader() {
    }

    /**
     * Reads any Components annotations.
     * 
     * @param context the scanning context
     * @param annotationValue the {@literal @}Components annotation
     * @return Components model
     */
    public static Components readComponents(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an @Components annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        Components components = new ComponentsImpl();
        // TODO for EVERY item below, handle the case where the annotation is ref-only.  then strip the ref path and use the final segment as the name
        components.setCallbacks(CallbackReader.readCallbacks(context, nested.value(OpenApiConstants.PROP_CALLBACKS)));
        components.setExamples(ExampleReader.readExamples(nested.value(OpenApiConstants.PROP_EXAMPLES)));
        components.setHeaders(HeaderReader.readHeaders(context, nested.value(OpenApiConstants.PROP_HEADERS)));
        components.setLinks(LinkReader.readLinks(nested.value(OpenApiConstants.PROP_LINKS)));
        components.setParameters(ParameterReader.readParameters(context, nested.value(OpenApiConstants.PROP_PARAMETERS)));
        components.setRequestBodies(RequestBodyReader.readRequestBodies(context,
                nested.value(OpenApiConstants.PROP_REQUEST_BODIES)));
        components.setResponses(ResponseObjectReader.readResponses(context, nested.value(OpenApiConstants.PROP_RESPONSES)));
        components.setSchemas(SchemaReader.readSchemas(context, nested.value(OpenApiConstants.PROP_SCHEMAS)));
        components.setSecuritySchemes(
                SecuritySchemeReader.readSecuritySchemes(nested.value(OpenApiConstants.PROP_SECURITY_SCHEMES)));

        return components;
    }
}
