package io.smallrye.openapi.runtime.io.headers;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;

public class HeaderIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Header, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_SCHEMA = "schema";
    private static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    private static final String PROP_REQUIRED = "required";
    private static final String PROP_DEPRECATED = "deprecated";

    public HeaderIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.HEADER, Names.create(Header.class));
    }

    @Override
    public Header read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Header");
        Header header = OASFactory.createHeader();
        header.setRef(ReferenceType.HEADER.refValue(annotation));
        header.setDescription(value(annotation, PROP_DESCRIPTION));
        header.setSchema(schemaIO().read(annotation.value(PROP_SCHEMA)));
        header.setRequired(value(annotation, PROP_REQUIRED));
        header.setDeprecated(value(annotation, PROP_DEPRECATED));
        header.setAllowEmptyValue(value(annotation, PROP_ALLOW_EMPTY_VALUE));
        header.setExtensions(extensionIO().readExtensible(annotation));
        return header;
    }
}
