package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ExternalDocumentationIO extends ModelIO<ExternalDocumentation> {

    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_URL = "url";

    private final ExtensionIO extension;

    public ExternalDocumentationIO(AnnotationScannerContext context) {
        super(context, Names.EXTERNAL_DOCUMENTATION, Names.create(ExternalDocumentation.class));
        extension = new ExtensionIO(context);
    }

    @Override
    public ExternalDocumentation read(AnnotationInstance annotation) {
        IoLogging.logger.annotation("@ExternalDocumentation");
        ExternalDocumentation model = new ExternalDocumentationImpl();
        model.setDescription(value(annotation, PROP_DESCRIPTION));
        model.setUrl(value(annotation, PROP_URL));
        model.setExtensions(extension.readExtensible(annotation));
        return model;
    }

    @Override
    public ExternalDocumentation read(ObjectNode node) {
        ExternalDocumentation model = new ExternalDocumentationImpl();
        model.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        model.setUrl(JsonUtil.stringProperty(node, PROP_URL));
        extension.readMap(node).forEach(model::addExtension);
        return model;
    }

    public Optional<ObjectNode> write(ExternalDocumentation model) {
        return optionalJsonObject(model)
                .map(node -> {
                    JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                    JsonUtil.stringProperty(node, PROP_URL, model.getUrl());
                    setAllIfPresent(node, extension.write(model));
                    return node;
                });
    }
}
