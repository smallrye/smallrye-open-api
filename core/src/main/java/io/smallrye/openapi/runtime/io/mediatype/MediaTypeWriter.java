package io.smallrye.openapi.runtime.io.mediatype;

import org.eclipse.microprofile.openapi.models.media.MediaType;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.encoding.EncodingWriter;
import io.smallrye.openapi.runtime.io.example.ExampleWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.schema.SchemaWriter;

/**
 * Writer the Media type object to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#mediaTypeObject">mediaTypeObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class MediaTypeWriter {

    private MediaTypeWriter() {
    }

    /**
     * Writes a {@link MediaType} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param model the MediaType model
     * @param name name of the node
     */
    public static void writeMediaType(ObjectNode parent, MediaType model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        SchemaWriter.writeSchema(node, model.getSchema(), MediaTypeConstant.PROP_SCHEMA);
        ObjectWriter.writeObject(node, MediaTypeConstant.PROP_EXAMPLE, model.getExample());
        ExampleWriter.writeExamples(node, model.getExamples());
        EncodingWriter.writeEncodings(node, model.getEncoding());
        ExtensionWriter.writeExtensions(node, model);
    }

}
