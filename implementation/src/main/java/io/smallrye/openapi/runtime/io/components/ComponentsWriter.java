package io.smallrye.openapi.runtime.io.components;

import org.eclipse.microprofile.openapi.models.Components;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.callback.CallbackWriter;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.example.ExampleWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.header.HeaderWriter;
import io.smallrye.openapi.runtime.io.link.LinkWriter;
import io.smallrye.openapi.runtime.io.parameter.ParameterWriter;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyWriter;
import io.smallrye.openapi.runtime.io.response.ResponseWriter;
import io.smallrye.openapi.runtime.io.schema.SchemaWriter;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeWriter;

/**
 * Writing the Components to json node
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#componentsObject">componentsObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ComponentsWriter {

    private ComponentsWriter() {
    }

    /**
     * Writes a {@link Components} to the JSON tree.
     * 
     * @param parent
     * @param components
     */
    public static void writeComponents(ObjectNode parent, Components components) {
        if (components == null) {
            return;
        }
        ObjectNode node = parent.putObject(DefinitionConstant.PROP_COMPONENTS);
        SchemaWriter.writeSchemas(node, components.getSchemas());
        ResponseWriter.writeAPIResponses(node, components.getResponses());
        ParameterWriter.writeParameters(node, components.getParameters());
        ExampleWriter.writeExamples(node, components.getExamples());
        RequestBodyWriter.writeRequestBodies(node, components.getRequestBodies());
        HeaderWriter.writeHeaders(node, components.getHeaders());
        SecuritySchemeWriter.writeSecuritySchemes(node, components.getSecuritySchemes());
        LinkWriter.writeLinks(node, components.getLinks());
        CallbackWriter.writeCallbacks(node, components.getCallbacks());
        ExtensionWriter.writeExtensions(node, components);
    }
}
