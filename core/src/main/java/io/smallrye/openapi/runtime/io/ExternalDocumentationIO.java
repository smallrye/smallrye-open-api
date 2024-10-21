package io.smallrye.openapi.runtime.io;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.jboss.jandex.AnnotationInstance;

public class ExternalDocumentationIO<V, A extends V, O extends V, AB, OB>
        extends ModelIO<ExternalDocumentation, V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_URL = "url";

    public ExternalDocumentationIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.EXTERNAL_DOCUMENTATION, Names.create(ExternalDocumentation.class));
    }

    @Override
    public ExternalDocumentation read(AnnotationInstance annotation) {
        IoLogging.logger.annotation("@ExternalDocumentation");
        ExternalDocumentation model = OASFactory.createExternalDocumentation();
        model.setDescription(value(annotation, PROP_DESCRIPTION));
        model.setUrl(value(annotation, PROP_URL));
        model.setExtensions(extensionIO().readExtensible(annotation));
        return model;
    }
}
