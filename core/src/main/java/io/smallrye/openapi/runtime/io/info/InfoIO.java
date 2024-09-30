package io.smallrye.openapi.runtime.io.info;

import org.eclipse.microprofile.openapi.models.info.Info;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class InfoIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Info, V, A, O, AB, OB> {

    private static final String PROP_LICENSE = "license";
    private static final String PROP_CONTACT = "contact";

    public InfoIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.INFO, Names.create(Info.class));
    }

    @Override
    protected boolean setProperty(Info model, AnnotationValue value) {
        switch (value.name()) {
            case PROP_CONTACT:
                model.setContact(contactIO().read(value));
                return true;
            case PROP_LICENSE:
                model.setLicense(licenseIO().read(value));
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public Info read(AnnotationInstance annotation) {
        return read(Info.class, annotation);
    }
}
