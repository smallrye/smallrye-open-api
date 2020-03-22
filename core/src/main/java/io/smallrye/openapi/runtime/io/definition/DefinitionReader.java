package io.smallrye.openapi.runtime.io.definition;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.components.ComponentsReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsReader;
import io.smallrye.openapi.runtime.io.info.InfoReader;
import io.smallrye.openapi.runtime.io.paths.PathsReader;
import io.smallrye.openapi.runtime.io.securityrequirement.SecurityRequirementReader;
import io.smallrye.openapi.runtime.io.server.ServerReader;
import io.smallrye.openapi.runtime.io.tag.TagReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the OpenAPIDefinition from an annotation or json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#openapi-object">openapi-object</a>
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
            final OpenAPI openApi,
            final AnnotationInstance annotationInstance) {
        LOG.debug("Processing an @OpenAPIDefinition annotation.");

        openApi.setInfo(InfoReader.readInfo(annotationInstance.value(DefinitionConstant.PROP_INFO)));
        openApi.setTags(TagReader.readTags(annotationInstance.value(DefinitionConstant.PROP_TAGS)).orElse(null));
        openApi.setServers(
                ServerReader.readServers(annotationInstance.value(DefinitionConstant.PROP_SERVERS)).orElse(null));
        openApi.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(annotationInstance.value(DefinitionConstant.PROP_SECURITY)).orElse(null));
        openApi.setExternalDocs(
                ExternalDocsReader
                        .readExternalDocs(annotationInstance.value(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(ComponentsReader.readComponents(context,
                annotationInstance.value(DefinitionConstant.PROP_COMPONENTS)));
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

        openApi.setOpenapi(JsonUtil.stringProperty(node, DefinitionConstant.PROP_OPENAPI));
        openApi.setInfo(InfoReader.readInfo(node.get(DefinitionConstant.PROP_INFO)));
        openApi.setTags(TagReader.readTags(node.get(DefinitionConstant.PROP_TAGS)).orElse(null));
        openApi.setServers(ServerReader.readServers(node.get(DefinitionConstant.PROP_SERVERS)).orElse(null));
        openApi.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(node.get(DefinitionConstant.PROP_SECURITY)).orElse(null));
        openApi.setExternalDocs(
                ExternalDocsReader.readExternalDocs(node.get(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(
                ComponentsReader.readComponents(node.get(DefinitionConstant.PROP_COMPONENTS)));
        openApi.setPaths(PathsReader.readPaths(node.get(DefinitionConstant.PROP_PATHS)));
        ExtensionReader.readExtensions(node, openApi);
    }

    // helper methods for scanners
    public static AnnotationInstance getDefinitionAnnotation(final ClassInfo targetClass) {
        return JandexUtil.getClassAnnotation(targetClass,
                DefinitionConstant.DOTNAME_OPEN_API_DEFINITION);
    }
}
