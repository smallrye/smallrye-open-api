package io.smallrye.openapi.runtime.io.callback;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.paths.PathsWriter;

/**
 * Writing the Callback to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#callbackObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class CallbackWriter {

    private CallbackWriter() {
    }

    /**
     * Writes a map of {@link Callback} to the JSON tree.
     * 
     * @param parent
     * @param callbacks
     */
    public static void writeCallbacks(ObjectNode parent, Map<String, Callback> callbacks) {
        if (callbacks == null) {
            return;
        }
        ObjectNode callbacksNode = parent.putObject(CallbackConstant.PROP_CALLBACKS);
        Set<Map.Entry<String, Callback>> entrySet = callbacks.entrySet();
        for (Map.Entry<String, Callback> entry : entrySet) {
            writeCallback(callbacksNode, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Writes a {@link Callback} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeCallback(ObjectNode parent, Callback model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, CallbackConstant.PROP_$REF, model.getRef());

        Set<Map.Entry<String, PathItem>> entrySet = model.getPathItems().entrySet();
        for (Map.Entry<String, PathItem> entry : entrySet) {
            PathsWriter.writePathItem(node, entry.getValue(), entry.getKey());
        }

        ExtensionWriter.writeExtensions(node, model);
    }

}
