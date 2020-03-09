package io.smallrye.openapi.api.reader;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * Reading the OpenAPIDefinition annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class DefinitionReader {
    private static final Logger LOG = Logger.getLogger(DefinitionReader.class);

    private DefinitionReader() {
    }

    /**
     * Reads a OpenAPIDefinition annotation.
     * 
     * @param context the scanning context
     * @param openApi OpenAPIImpl
     * @param annotationInstance {@literal @}OpenAPIDefinition annotation
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     */
    public static void processDefinition(final AnnotationScannerContext context,
            OpenAPI openApi, // TODO: make this a return ?
            final AnnotationInstance annotationInstance,
            final String[] currentConsumes,
            final String[] currentProduces) {
        LOG.debug("Processing an @OpenAPIDefinition annotation.");
        openApi.setInfo(InfoReader.readInfo(annotationInstance.value(OpenApiConstants.PROP_INFO)));
        openApi.setTags(TagReader.readTags(annotationInstance.value(OpenApiConstants.PROP_TAGS)));
        openApi.setServers(ServerReader.readServers(annotationInstance.value(OpenApiConstants.PROP_SERVERS)));
        openApi.setSecurity(SecurityReader.readSecurity(annotationInstance.value(OpenApiConstants.PROP_SECURITY)));
        openApi.setExternalDocs(
                ExternalDocsReader.readExternalDocs(annotationInstance.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(ComponentsReader.readComponents(context,
                annotationInstance.value(OpenApiConstants.PROP_COMPONENTS), currentConsumes, currentProduces));
    }
}
