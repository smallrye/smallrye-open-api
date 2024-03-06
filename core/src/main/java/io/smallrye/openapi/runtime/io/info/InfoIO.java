package io.smallrye.openapi.runtime.io.info;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.info.Info;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class InfoIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Info, V, A, O, AB, OB> {

    private static final String PROP_TERMS_OF_SERVICE = "termsOfService";
    private static final String PROP_TITLE = "title";
    private static final String PROP_VERSION = "version";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_LICENSE = "license";
    private static final String PROP_CONTACT = "contact";

    private final ContactIO<V, A, O, AB, OB> contact;
    private final LicenseIO<V, A, O, AB, OB> license;
    private final ExtensionIO<V, A, O, AB, OB> extension;

    public InfoIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.INFO, Names.create(Info.class));
        contact = new ContactIO<>(context);
        license = new LicenseIO<>(context);
        extension = new ExtensionIO<>(context);
    }

    @Override
    public Info read(AnnotationInstance annotation) {
        IoLogging.logger.annotation("@Info");
        Info info = new InfoImpl();
        info.setTitle(value(annotation, PROP_TITLE));
        info.setDescription(value(annotation, PROP_DESCRIPTION));
        info.setTermsOfService(value(annotation, PROP_TERMS_OF_SERVICE));
        info.setContact(contact.read(annotation.value(PROP_CONTACT)));
        info.setLicense(license.read(annotation.value(PROP_LICENSE)));
        info.setVersion(value(annotation, PROP_VERSION));
        info.setExtensions(extension.readExtensible(annotation));
        return info;
    }

    /**
     * Reads an {@link Info} OpenAPI node.
     *
     * @param node the json node
     * @return Info model
     */
    @Override
    public Info readObject(O node) {
        IoLogging.logger.singleJsonNode("Info");
        Info info = new InfoImpl();
        info.setTitle(jsonIO().getString(node, PROP_TITLE));
        info.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        info.setTermsOfService(jsonIO().getString(node, PROP_TERMS_OF_SERVICE));
        info.setContact(contact.readValue(jsonIO().getValue(node, PROP_CONTACT)));
        info.setLicense(license.readValue(jsonIO().getValue(node, PROP_LICENSE)));
        info.setVersion(jsonIO().getString(node, PROP_VERSION));
        info.setExtensions(extension.readMap(node));
        return info;
    }

    public Optional<O> write(Info model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_TITLE, jsonIO().toJson(model.getTitle()));
            setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
            setIfPresent(node, PROP_TERMS_OF_SERVICE, jsonIO().toJson(model.getTermsOfService()));
            setIfPresent(node, PROP_CONTACT, contact.write(model.getContact()));
            setIfPresent(node, PROP_LICENSE, license.write(model.getLicense()));
            setIfPresent(node, PROP_VERSION, jsonIO().toJson(model.getVersion()));
            setAllIfPresent(node, extension.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
