package io.smallrye.openapi.runtime.io.info;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class LicenseIO extends ModelIO<License> {

    private static final String PROP_NAME = "name";
    private static final String PROP_URL = "url";

    private final ExtensionIO extension;

    public LicenseIO(AnnotationScannerContext context) {
        super(context, Names.LICENSE, Names.create(License.class));
        extension = new ExtensionIO(context);
    }

    @Override
    public License read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@License");
        License license = new LicenseImpl();
        license.setName(value(annotation, PROP_NAME));
        license.setUrl(value(annotation, PROP_URL));
        license.setExtensions(extension.readExtensible(annotation));
        return license;
    }

    @Override
    public License read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("License");
        License license = new LicenseImpl();
        license.setName(JsonUtil.stringProperty(node, PROP_NAME));
        license.setUrl(JsonUtil.stringProperty(node, PROP_URL));
        extension.readMap(node).forEach(license::addExtension);
        return license;
    }

    public Optional<ObjectNode> write(License model) {
        return optionalJsonObject(model)
                .map(node -> {
                    JsonUtil.stringProperty(node, PROP_NAME, model.getName());
                    JsonUtil.stringProperty(node, PROP_URL, model.getUrl());
                    setAllIfPresent(node, extension.write(model));
                    return node;
                });
    }
}
