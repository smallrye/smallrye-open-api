package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class ExternalDocumentationIO<V, A extends V, O extends V, AB, OB>
        extends ModelIO<ExternalDocumentation, V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_URL = "url";

    private final ExtensionIO<V, A, O, AB, OB> extension;

    public ExternalDocumentationIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.EXTERNAL_DOCUMENTATION, Names.create(ExternalDocumentation.class));
        extension = new ExtensionIO<>(context);
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
    public ExternalDocumentation readObject(O node) {
        ExternalDocumentation model = new ExternalDocumentationImpl();
        jsonIO().getString(node, PROP_DESCRIPTION);
        model.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        model.setUrl(jsonIO().getString(node, PROP_URL));
        extension.readMap(node).forEach(model::addExtension);
        return model;
    }

    public Optional<O> write(ExternalDocumentation model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
            setIfPresent(node, PROP_URL, jsonIO().toJson(model.getUrl()));
            setAllIfPresent(node, extension.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
