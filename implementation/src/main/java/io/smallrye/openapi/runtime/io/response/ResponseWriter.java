package io.smallrye.openapi.runtime.io.response;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.content.ContentWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.header.HeaderWriter;
import io.smallrye.openapi.runtime.io.link.LinkWriter;

/**
 * Writing the APIResponse to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#responseObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ResponseWriter {

    private ResponseWriter() {
    }

    /**
     * Writes a map of {@link APIResponse} to the JSON tree.
     * 
     * @param parent
     * @param responses
     */
    public static void writeAPIResponses(ObjectNode parent, Map<String, APIResponse> responses) {
        if (responses == null) {
            return;
        }
        ObjectNode responsesNode = parent.putObject(ResponseConstant.PROP_RESPONSES);
        for (String responseName : responses.keySet()) {
            writeAPIResponse(responsesNode, responses.get(responseName), responseName);
        }
    }

    /**
     * Writes a {@link APIResponses} map to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    public static void writeAPIResponses(ObjectNode parent, APIResponses model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(ResponseConstant.PROP_RESPONSES);
        writeAPIResponse(node, model.getDefaultValue(), ResponseConstant.PROP_DEFAULT);
        Set<Map.Entry<String, APIResponse>> entrySet = model.getAPIResponses().entrySet();
        for (Map.Entry<String, APIResponse> entry : entrySet) {
            writeAPIResponse(node, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Writes a {@link APIResponse} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeAPIResponse(ObjectNode parent, APIResponse model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, ResponseConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, ResponseConstant.PROP_DESCRIPTION, model.getDescription());
        HeaderWriter.writeHeaders(node, model.getHeaders());
        ContentWriter.writeContent(node, model.getContent());
        LinkWriter.writeLinks(node, model.getLinks());
        ExtensionWriter.writeExtensions(node, model);
    }

}
