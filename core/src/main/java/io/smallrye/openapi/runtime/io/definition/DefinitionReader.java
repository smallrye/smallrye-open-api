package io.smallrye.openapi.runtime.io.definition;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.runtime.io.IoLogging;
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
import io.smallrye.openapi.runtime.util.Annotations;

/**
 * Reading the OpenAPIDefinition from an annotation or json
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#openapi-object">openapi-object</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class DefinitionReader {

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
        IoLogging.logger.annotation("@OpenAPIDefinition");

        openApi.setInfo(InfoReader.readInfo(context, annotationInstance.value(DefinitionConstant.PROP_INFO)));
        openApi.setTags(TagReader.readTags(context, annotationInstance.value(DefinitionConstant.PROP_TAGS)).orElse(null));
        openApi.setServers(
                ServerReader.readServers(context, annotationInstance.value(DefinitionConstant.PROP_SERVERS)).orElse(null));
        openApi.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(annotationInstance.value(DefinitionConstant.PROP_SECURITY),
                        annotationInstance.value(DefinitionConstant.PROP_SECURITY_SETS))
                .orElse(null));
        openApi.setExternalDocs(
                ExternalDocsReader
                        .readExternalDocs(context, annotationInstance.value(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(ComponentsReader.readComponents(context,
                annotationInstance.value(DefinitionConstant.PROP_COMPONENTS)));
        openApi.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
    }

    /**
     * Reads a OpenAPIDefinition Json node.
     *
     * @param openApi the OpenAPI model
     * @param node the Json node
     */
    public static void processDefinition(final OpenAPI openApi,
            final JsonNode node) {
        IoLogging.logger.jsonNode("OpenAPIDefinition");

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
        return Annotations.getAnnotation(targetClass, DefinitionConstant.DOTNAME_OPEN_API_DEFINITION);
    }
}
