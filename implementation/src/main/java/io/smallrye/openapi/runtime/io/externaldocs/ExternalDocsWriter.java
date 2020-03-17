package io.smallrye.openapi.runtime.io.externaldocs;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;

/**
 * This writes External Documentation json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#externalDocumentationObject">externalDocumentationObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExternalDocsWriter {

    private ExternalDocsWriter() {
    }

    /**
     * Writes the {@link ExternalDocumentation} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    public static void writeExternalDocumentation(ObjectNode parent, ExternalDocumentation model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(DefinitionConstant.PROP_EXTERNAL_DOCS);

        JsonUtil.stringProperty(node, ExternalDocsConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.stringProperty(node, ExternalDocsConstant.PROP_URL, model.getUrl());
        ExtensionWriter.writeExtensions(node, model);
    }
}
