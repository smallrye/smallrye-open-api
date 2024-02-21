package io.smallrye.openapi.runtime.io.info;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.info.Info;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class InfoIO extends ModelIO<Info> {

    private static final String PROP_TERMS_OF_SERVICE = "termsOfService";
    private static final String PROP_TITLE = "title";
    private static final String PROP_VERSION = "version";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_LICENSE = "license";
    private static final String PROP_CONTACT = "contact";

    private final ContactIO contact;
    private final LicenseIO license;
    private final ExtensionIO extension;

    public InfoIO(AnnotationScannerContext context) {
        super(context, Names.INFO, Names.create(Info.class));
        contact = new ContactIO(context);
        license = new LicenseIO(context);
        extension = new ExtensionIO(context);
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
    public Info read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Info");

        Info info = new InfoImpl();
        info.setTitle(JsonUtil.stringProperty(node, PROP_TITLE));
        info.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        info.setTermsOfService(JsonUtil.stringProperty(node, PROP_TERMS_OF_SERVICE));
        info.setContact(contact.read(node.get(PROP_CONTACT)));
        info.setLicense(license.read(node.get(PROP_LICENSE)));
        info.setVersion(JsonUtil.stringProperty(node, PROP_VERSION));
        extension.readMap(node).forEach(info::addExtension);
        return info;
    }

    public Optional<ObjectNode> write(Info model) {
        return optionalJsonObject(model)
                .map(node -> {
                    JsonUtil.stringProperty(node, PROP_TITLE, model.getTitle());
                    JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                    JsonUtil.stringProperty(node, PROP_TERMS_OF_SERVICE, model.getTermsOfService());
                    setIfPresent(node, PROP_CONTACT, contact.write(model.getContact()));
                    setIfPresent(node, PROP_LICENSE, license.write(model.getLicense()));
                    JsonUtil.stringProperty(node, PROP_VERSION, model.getVersion());
                    setAllIfPresent(node, extension.write(model));
                    return node;
                });
    }
}
