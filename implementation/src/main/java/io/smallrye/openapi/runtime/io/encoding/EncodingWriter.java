package io.smallrye.openapi.runtime.io.encoding;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Encoding;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.header.HeaderWriter;

/**
 * Write the Encoding object to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#encodingObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class EncodingWriter {

    private EncodingWriter() {
    }

    /**
     * Writes a map of {@link Encoding} objects to the JSON tree.
     * 
     * @param parent
     * @param models
     */
    public static void writeEncodings(ObjectNode parent, Map<String, Encoding> models) {
        if (models == null) {
            return;
        }
        ObjectNode node = parent.putObject(EncodingConstant.PROP_ENCODING);
        for (String name : models.keySet()) {
            Encoding encoding = models.get(name);
            writeEncoding(node, encoding, name);
        }
    }

    /**
     * Writes a {@link Encoding} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeEncoding(ObjectNode parent, Encoding model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, EncodingConstant.PROP_CONTENT_TYPE, model.getContentType());
        HeaderWriter.writeHeaders(node, model.getHeaders());
        JsonUtil.enumProperty(node, EncodingConstant.PROP_STYLE, model.getStyle());
        JsonUtil.booleanProperty(node, EncodingConstant.PROP_EXPLODE, model.getExplode());
        JsonUtil.booleanProperty(node, EncodingConstant.PROP_ALLOW_RESERVED, model.getAllowReserved());
        ExtensionWriter.writeExtensions(node, model);
    }
}
