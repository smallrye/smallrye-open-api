package io.smallrye.openapi.runtime.io.link;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.links.Link;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.server.ServerWriter;

/**
 * Writing the Link to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#linkObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LinkWriter {

    private LinkWriter() {
    }

    /**
     * Writes a map of {@link Link} to the JSON tree.
     * 
     * @param parent
     * @param links
     */
    public static void writeLinks(ObjectNode parent, Map<String, Link> links) {
        if (links == null) {
            return;
        }
        ObjectNode linksNode = parent.putObject(ComponentsConstant.PROP_LINKS);
        for (String linkName : links.keySet()) {
            writeLink(linksNode, links.get(linkName), linkName);
        }
    }

    /**
     * Writes a {@link Link} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeLink(ObjectNode parent, Link model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, LinkConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_REF, model.getOperationRef());
        JsonUtil.stringProperty(node, OpenApiConstants.PROP_OPERATION_ID, model.getOperationId());
        writeLinkParameters(node, model.getParameters());
        ObjectWriter.writeObject(node, LinkConstant.PROP_REQUEST_BODY, model.getRequestBody());
        JsonUtil.stringProperty(node, LinkConstant.PROP_DESCRIPTION, model.getDescription());
        ServerWriter.writeServer(node, model.getServer());
        ExtensionWriter.writeExtensions(node, model);
    }

    /**
     * Writes the link parameters to the given node.
     * 
     * @param parent
     * @param parameters
     */
    private static void writeLinkParameters(ObjectNode parent, Map<String, Object> parameters) {
        if (parameters == null) {
            return;
        }
        ObjectNode node = parent.putObject(LinkConstant.PROP_PARAMETERS);
        for (String name : parameters.keySet()) {
            ObjectWriter.writeObject(node, name, parameters.get(name));
        }
    }

}
