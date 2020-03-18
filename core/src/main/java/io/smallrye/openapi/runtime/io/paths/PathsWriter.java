package io.smallrye.openapi.runtime.io.paths;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.operation.OperationWriter;
import io.smallrye.openapi.runtime.io.parameter.ParameterWriter;
import io.smallrye.openapi.runtime.io.server.ServerWriter;

/**
 * Writing the Paths to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#pathsObject">pathsObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class PathsWriter {

    private PathsWriter() {
    }

    /**
     * Writes a {@link Paths} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param paths Paths model
     */
    public static void writePaths(ObjectNode parent, Paths paths) {
        if (paths == null) {
            return;
        }
        ObjectNode pathsNode = parent.putObject(DefinitionConstant.PROP_PATHS);
        if (paths.getPathItems() != null) {
            Set<Map.Entry<String, PathItem>> entrySet = paths.getPathItems().entrySet();
            for (Map.Entry<String, PathItem> entry : entrySet) {
                writePathItem(pathsNode, entry.getValue(), entry.getKey());
            }
        }
        ExtensionWriter.writeExtensions(pathsNode, paths);
    }

    /**
     * Writes a {@link PathItem} to the JSON tree.
     * 
     * @param parent parent json node
     * @param model PathItem model
     * @param pathName the node name (path)
     */
    public static void writePathItem(ObjectNode parent, PathItem model, String pathName) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(pathName);
        JsonUtil.stringProperty(node, PathsConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, PathsConstant.PROP_SUMMARY, model.getSummary());
        JsonUtil.stringProperty(node, PathsConstant.PROP_DESCRIPTION, model.getDescription());
        OperationWriter.writeOperation(node, model.getGET(), PathsConstant.PROP_GET);
        OperationWriter.writeOperation(node, model.getPUT(), PathsConstant.PROP_PUT);
        OperationWriter.writeOperation(node, model.getPOST(), PathsConstant.PROP_POST);
        OperationWriter.writeOperation(node, model.getDELETE(), PathsConstant.PROP_DELETE);
        OperationWriter.writeOperation(node, model.getOPTIONS(), PathsConstant.PROP_OPTIONS);
        OperationWriter.writeOperation(node, model.getHEAD(), PathsConstant.PROP_HEAD);
        OperationWriter.writeOperation(node, model.getPATCH(), PathsConstant.PROP_PATCH);
        OperationWriter.writeOperation(node, model.getTRACE(), PathsConstant.PROP_TRACE);
        ParameterWriter.writeParameterList(node, model.getParameters());
        ServerWriter.writeServers(node, model.getServers());
        ExtensionWriter.writeExtensions(node, model);
    }
}
