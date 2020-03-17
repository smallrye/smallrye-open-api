package io.smallrye.openapi.runtime.io.header;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.content.ContentWriter;
import io.smallrye.openapi.runtime.io.example.ExampleWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.schema.SchemaWriter;

/**
 * Writing the Header to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#headerObject">headerObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class HeaderWriter {

    private HeaderWriter() {
    }

    /**
     * Writes a map of {@link Header} to the JSON tree.
     * 
     * @param parent
     * @param headers
     */
    public static void writeHeaders(ObjectNode parent, Map<String, Header> headers) {
        if (headers == null) {
            return;
        }
        ObjectNode headersNode = parent.putObject(ComponentsConstant.PROP_HEADERS);
        for (String headerName : headers.keySet()) {
            writeHeader(headersNode, headers.get(headerName), headerName);
        }
    }

    /**
     * Writes a {@link RequestBody} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeHeader(ObjectNode parent, Header model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, HeaderConstant.PROP_$REF, model.getRef());
        JsonUtil.stringProperty(node, HeaderConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_REQUIRED, model.getRequired());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_DEPRECATED, model.getDeprecated());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_ALLOW_EMPTY_VALUE, model.getAllowEmptyValue());
        JsonUtil.enumProperty(node, HeaderConstant.PROP_STYLE, model.getStyle());
        JsonUtil.booleanProperty(node, HeaderConstant.PROP_EXPLODE, model.getExplode());
        SchemaWriter.writeSchema(node, model.getSchema(), HeaderConstant.PROP_SCHEMA);
        ObjectWriter.writeObject(node, HeaderConstant.PROP_EXAMPLE, model.getExample());
        ExampleWriter.writeExamples(node, model.getExamples());
        ContentWriter.writeContent(node, model.getContent());
        ExtensionWriter.writeExtensions(node, model);

    }
}
