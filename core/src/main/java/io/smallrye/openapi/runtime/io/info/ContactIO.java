package io.smallrye.openapi.runtime.io.info;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ContactIO extends ModelIO<Contact> {

    private static final String PROP_NAME = "name";
    private static final String PROP_EMAIL = "email";
    private static final String PROP_URL = "url";

    private final ExtensionIO extension;

    public ContactIO(AnnotationScannerContext context) {
        super(context, Names.CONTACT, Names.create(Contact.class));
        extension = new ExtensionIO(context);
    }

    @Override
    public Contact read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Contact");
        Contact contact = new ContactImpl();
        contact.setName(value(annotation, PROP_NAME));
        contact.setUrl(value(annotation, PROP_URL));
        contact.setEmail(value(annotation, PROP_EMAIL));
        contact.setExtensions(extension.readExtensible(annotation));
        return contact;
    }

    public Contact read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Contact");
        Contact contact = new ContactImpl();
        contact.setName(JsonUtil.stringProperty(node, PROP_NAME));
        contact.setUrl(JsonUtil.stringProperty(node, PROP_URL));
        contact.setEmail(JsonUtil.stringProperty(node, PROP_EMAIL));
        extension.readMap(node).forEach(contact::addExtension);
        return contact;
    }

    public Optional<ObjectNode> write(Contact model) {
        return optionalJsonObject(model)
                .map(node -> {
                    JsonUtil.stringProperty(node, PROP_NAME, model.getName());
                    JsonUtil.stringProperty(node, PROP_URL, model.getUrl());
                    JsonUtil.stringProperty(node, PROP_EMAIL, model.getEmail());
                    setAllIfPresent(node, extension.write(model));
                    return node;
                });
    }
}
