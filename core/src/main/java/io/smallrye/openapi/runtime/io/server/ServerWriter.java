package io.smallrye.openapi.runtime.io.server;

import java.util.List;

import org.eclipse.microprofile.openapi.models.servers.Server;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.servervariable.ServerVariableWriter;

/**
 * Writing the Server to json
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#serverObject">serverObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerWriter {

    private ServerWriter() {
    }

    /**
     * Writes the {@link Server} model array to the JSON tree.
     *
     * @param node the json node
     * @param servers list of Server models
     */
    public static void writeServers(ObjectNode node, List<Server> servers) {
        if (servers == null) {
            return;
        }
        ArrayNode array = node.putArray(DefinitionConstant.PROP_SERVERS);
        for (Server server : servers) {
            ObjectNode serverNode = array.addObject();
            writeServerToNode(serverNode, server);
        }
    }

    /**
     * Writes a {@link Server} model to the given JSON node.
     *
     * @param parent parent json node
     * @param model Server model
     */
    public static void writeServer(ObjectNode parent, Server model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(ServerConstant.PROP_SERVER);
        writeServerToNode(node, model);
    }

    private static void writeServerToNode(ObjectNode node, Server model) {
        JsonUtil.stringProperty(node, ServerConstant.PROP_URL, model.getUrl());
        JsonUtil.stringProperty(node, ServerConstant.PROP_DESCRIPTION, model.getDescription());
        ServerVariableWriter.writeServerVariables(node, model.getVariables());
        ExtensionWriter.writeExtensions(node, model);
    }
}
