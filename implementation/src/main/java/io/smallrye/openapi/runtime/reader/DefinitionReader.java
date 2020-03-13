package io.smallrye.openapi.runtime.reader;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the OpenAPIDefinition from an annotation or json
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
     */
    public static void processDefinition(final AnnotationScannerContext context,
            final OpenAPI openApi, // TODO: make this a return ?
            final AnnotationInstance annotationInstance) {
        LOG.debug("Processing an @OpenAPIDefinition annotation.");

        openApi.setInfo(InfoReader.readInfo(annotationInstance.value(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_INFO)));
        openApi.setTags(TagReader.readTags(annotationInstance.value(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_TAGS)));
        openApi.setServers(
                ServerReader.readServers(annotationInstance.value(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_SERVERS)));
        openApi.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(annotationInstance.value(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_SECURITY)));
        openApi.setExternalDocs(
                ExternalDocsReader
                        .readExternalDocs(annotationInstance.value(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(ComponentsReader.readComponents(context,
                annotationInstance.value(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_COMPONENTS)));
        // Where is Paths and Extensions ?
    }

    /**
     * Reads a OpenAPIDefinition Json node.
     * 
     * @param openApi the OpenAPI model
     * @param node the Json node
     */
    public static void processDefinition(final OpenAPI openApi,
            final JsonNode node) {
        LOG.debug("Processing an OpenAPIDefinition json node.");

        openApi.setOpenapi(JsonUtil.stringProperty(node, MPOpenApiConstants.OPEN_API_DEFINITION.PROP_OPENAPI));
        openApi.setInfo(InfoReader.readInfo(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_INFO)));
        openApi.setTags(TagReader.readTags(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_TAGS)));
        openApi.setServers(ServerReader.readServers(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_SERVERS)));
        openApi.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_SECURITY)));
        openApi.setExternalDocs(
                ExternalDocsReader.readExternalDocs(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(
                ComponentsReader.readComponents(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_COMPONENTS)));
        openApi.setPaths(PathsReader.readPaths(node.get(MPOpenApiConstants.OPEN_API_DEFINITION.PROP_PATHS)));
        ExtensionReader.readExtensions(node, openApi);
    }

    // helper methods for scanners
    public static AnnotationInstance getDefinitionAnnotation(final ClassInfo targetClass) {
        return JandexUtil.getClassAnnotation(targetClass,
                MPOpenApiConstants.OPEN_API_DEFINITION.TYPE_OPEN_API_DEFINITION);
    }
}
