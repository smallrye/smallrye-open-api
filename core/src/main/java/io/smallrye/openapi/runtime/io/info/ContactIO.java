package io.smallrye.openapi.runtime.io.info;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class ContactIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Contact, V, A, O, AB, OB> {

    public ContactIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.CONTACT, Names.create(Contact.class));
    }

    @Override
    public Contact read(AnnotationInstance annotation) {
        return read(Contact.class, annotation);
    }
}
