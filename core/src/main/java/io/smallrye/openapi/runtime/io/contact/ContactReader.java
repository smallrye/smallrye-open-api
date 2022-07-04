package io.smallrye.openapi.runtime.io.contact;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * This reads the Contact from annotations or json
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#contactObject">contactObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ContactReader {

    private ContactReader() {
    }

    /**
     * Reads an Contact annotation.
     *
     * @param annotationValue the {@literal @}Contact annotation
     * @return Contact model
     */
    public static Contact readContact(final AnnotationScannerContext context, final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@Contact");
        AnnotationInstance nested = annotationValue.asNested();
        Contact contact = new ContactImpl();
        contact.setName(JandexUtil.stringValue(nested, ContactConstant.PROP_NAME));
        contact.setUrl(JandexUtil.stringValue(nested, ContactConstant.PROP_URL));
        contact.setEmail(JandexUtil.stringValue(nested, ContactConstant.PROP_EMAIL));
        contact.setExtensions(ExtensionReader.readExtensions(context, nested));
        return contact;
    }

    /**
     * Reads an {@link Contact} OpenAPI node.
     *
     * @param node the json node
     * @return Contact model
     */
    public static Contact readContact(final JsonNode node) {
        if (node == null) {
            return null;
        }
        IoLogging.logger.singleJsonNode("Contact");
        Contact contact = new ContactImpl();
        contact.setName(JsonUtil.stringProperty(node, ContactConstant.PROP_NAME));
        contact.setUrl(JsonUtil.stringProperty(node, ContactConstant.PROP_URL));
        contact.setEmail(JsonUtil.stringProperty(node, ContactConstant.PROP_EMAIL));
        ExtensionReader.readExtensions(node, contact);
        return contact;
    }

}
