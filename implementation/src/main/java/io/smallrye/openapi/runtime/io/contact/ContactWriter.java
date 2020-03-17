package io.smallrye.openapi.runtime.io.contact;

import org.eclipse.microprofile.openapi.models.info.Contact;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.info.InfoConstant;

/**
 * This write the Contact to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#contactObject">contactObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ContactWriter {

    private ContactWriter() {
    }

    /**
     * Writes the {@link Contact} model to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    public static void writeContact(ObjectNode parent, Contact model) {
        if (model == null) {
            return;
        }
        ObjectNode node = JsonUtil.objectNode();
        parent.set(InfoConstant.PROP_CONTACT, node);

        JsonUtil.stringProperty(node, ContactConstant.PROP_NAME, model.getName());
        JsonUtil.stringProperty(node, ContactConstant.PROP_URL, model.getUrl());
        JsonUtil.stringProperty(node, ContactConstant.PROP_EMAIL, model.getEmail());
        ExtensionWriter.writeExtensions(node, model);
    }

}
