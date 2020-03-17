package io.smallrye.openapi.runtime.io.servervariable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.server.ServerConstant;

/**
 * Writing the ServerVariable to json
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverVariableObject">serverVariableObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerVariableWriter {

    private ServerVariableWriter() {
    }

    /**
     * Writes the {@link ServerVariables} model to the JSON tree.
     * 
     * @param serverNode the json node
     * @param variables map of ServerVariable models
     */
    public static void writeServerVariables(ObjectNode serverNode, Map<String, ServerVariable> variables) {
        if (variables == null) {
            return;
        }
        ObjectNode variablesNode = serverNode.putObject(ServerConstant.PROP_VARIABLES);

        Set<Map.Entry<String, ServerVariable>> entrySet = variables.entrySet();
        for (Map.Entry<String, ServerVariable> entry : entrySet) {
            writeServerVariable(variablesNode, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Writes a {@link ServerVariable} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param model the ServerVariable model
     * @param variableName the node name
     */
    public static void writeServerVariable(ObjectNode parent, ServerVariable model, String variableName) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(variableName);
        JsonUtil.stringProperty(node, ServerVariableConstant.PROP_DEFAULT, model.getDefaultValue());
        JsonUtil.stringProperty(node, ServerVariableConstant.PROP_DESCRIPTION, model.getDescription());
        List<String> enumeration = model.getEnumeration();
        if (enumeration != null) {
            ArrayNode enumArray = node.putArray(ServerVariableConstant.PROP_ENUM);
            for (String enumValue : enumeration) {
                enumArray.add(enumValue);
            }
        }
        ExtensionWriter.writeExtensions(node, model);
    }

}
