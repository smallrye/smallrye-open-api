package io.smallrye.openapi.runtime.io.discriminator;

import org.eclipse.microprofile.openapi.models.media.Discriminator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;

/**
 * Writing the Discriminator to json
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#discriminatorObject">discriminatorObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class DiscriminatorWriter {

    private DiscriminatorWriter() {
    }

    /**
     * Writes a {@link Discriminator} object to the JSON tree.
     *
     * @param parent the parent json node
     * @param model the Discriminator model
     */
    public static void writeDiscriminator(ObjectNode parent, Discriminator model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(SchemaConstant.PROP_DISCRIMINATOR);
        JsonUtil.stringProperty(node, DiscriminatorConstant.PROP_PROPERTY_NAME, model.getPropertyName());
        ObjectWriter.writeStringMap(node, model.getMapping(), DiscriminatorConstant.PROP_MAPPING);
    }

}
