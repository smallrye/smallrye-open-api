package io.smallrye.openapi.runtime.io.discriminator;

import org.eclipse.microprofile.openapi.models.media.Discriminator;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.media.DiscriminatorImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;

/**
 * Reading the Discriminator from an annotation or json
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">discriminatorObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class DiscriminatorReader {

    private DiscriminatorReader() {
    }

    /**
     * Reads a {@link Discriminator} OpenAPI node.
     * 
     * @param node the json node
     * @return Discriminator model
     */
    public static Discriminator readDiscriminator(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        Discriminator discriminator = new DiscriminatorImpl();
        discriminator.setPropertyName(JsonUtil.stringProperty(node, DiscriminatorConstant.PROP_PROPERTY_NAME));
        discriminator.setMapping(JsonUtil.readStringMap(node.get(DiscriminatorConstant.PROP_MAPPING)).orElse(null));
        return discriminator;
    }
}
