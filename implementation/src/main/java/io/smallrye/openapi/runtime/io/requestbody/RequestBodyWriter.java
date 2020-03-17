package io.smallrye.openapi.runtime.io.requestbody;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.parameters.RequestBody;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.content.ContentWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;

/**
 * Writing the RequestBody to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#requestBodyObject">requestBodyObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class RequestBodyWriter {

    private RequestBodyWriter() {
    }

    /**
     * Writes a map of {@link RequestBody} to the JSON tree.
     * 
     * @param parent
     * @param requestBodies
     */
    public static void writeRequestBodies(ObjectNode parent, Map<String, RequestBody> requestBodies) {
        if (requestBodies == null) {
            return;
        }
        ObjectNode requestBodiesNode = parent.putObject(ComponentsConstant.PROP_REQUEST_BODIES);
        for (String requestBodyName : requestBodies.keySet()) {
            writeRequestBody(requestBodiesNode, requestBodies.get(requestBodyName), requestBodyName);
        }
    }

    /**
     * Writes a {@link RequestBody} to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    public static void writeRequestBody(ObjectNode parent, RequestBody model) {
        writeRequestBody(parent, model, RequestBodyConstant.PROP_REQUEST_BODY);
    }

    /**
     * Writes a {@link RequestBody} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeRequestBody(ObjectNode parent, RequestBody model, String name) {
        if (model == null) {
            return;
        }

        ObjectNode node = parent.putObject(name);

        JsonUtil.stringProperty(node, RequestBodyConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, RequestBodyConstant.PROP_DESCRIPTION, model.getDescription());
        ContentWriter.writeContent(node, model.getContent());
        JsonUtil.booleanProperty(node, RequestBodyConstant.PROP_REQUIRED, model.getRequired());
        ExtensionWriter.writeExtensions(node, model);
    }

}
