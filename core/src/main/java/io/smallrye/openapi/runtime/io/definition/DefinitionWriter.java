package io.smallrye.openapi.runtime.io.definition;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.components.ComponentsWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsWriter;
import io.smallrye.openapi.runtime.io.info.InfoWriter;
import io.smallrye.openapi.runtime.io.paths.PathsWriter;
import io.smallrye.openapi.runtime.io.securityrequirement.SecurityRequirementWriter;
import io.smallrye.openapi.runtime.io.server.ServerWriter;
import io.smallrye.openapi.runtime.io.tag.TagWriter;

/**
 * Writing the OpenAPIDefinition to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#openapi-object">openapi-object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class DefinitionWriter {

    private DefinitionWriter() {
    }

    /**
     * Writes the given model.
     * 
     * @param node the json node
     * @param model the OpenAPI model
     */
    public static void writeOpenAPI(ObjectNode node, OpenAPI model) {
        JsonUtil.stringProperty(node, DefinitionConstant.PROP_OPENAPI, model.getOpenapi());
        InfoWriter.writeInfo(node, model.getInfo());
        ExternalDocsWriter.writeExternalDocumentation(node, model.getExternalDocs());
        ServerWriter.writeServers(node, model.getServers());
        SecurityRequirementWriter.writeSecurityRequirements(node, model.getSecurity());
        TagWriter.writeTags(node, model.getTags());
        PathsWriter.writePaths(node, model.getPaths());
        ComponentsWriter.writeComponents(node, model.getComponents());
        ExtensionWriter.writeExtensions(node, model);
    }

}
