package io.smallrye.openapi.runtime.io.info;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class ContactIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Contact, V, A, O, AB, OB> {

    private static final String PROP_NAME = "name";
    private static final String PROP_EMAIL = "email";
    private static final String PROP_URL = "url";

    private final ExtensionIO<V, A, O, AB, OB> extension;

    public ContactIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.CONTACT, Names.create(Contact.class));
        extension = new ExtensionIO<>(context);
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

    @Override
    public Contact readObject(O node) {
        IoLogging.logger.singleJsonNode("Contact");
        Contact contact = new ContactImpl();
        contact.setName(jsonIO.getString(node, PROP_NAME));
        contact.setUrl(jsonIO.getString(node, PROP_URL));
        contact.setEmail(jsonIO.getString(node, PROP_EMAIL));
        extension.readMap(node).forEach(contact::addExtension);
        return contact;
    }

    @Override
    public Optional<O> write(Contact model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_NAME, jsonIO.toJson(model.getName()));
            setIfPresent(node, PROP_URL, jsonIO.toJson(model.getUrl()));
            setIfPresent(node, PROP_EMAIL, jsonIO.toJson(model.getEmail()));
            setAllIfPresent(node, extension.write(model));
            return node;
        }).map(jsonIO::buildObject);
    }
}
