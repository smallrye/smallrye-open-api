package io.smallrye.openapi.runtime.io.headers;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;

public class HeaderIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Header, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_SCHEMA = "schema";
    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";

    public HeaderIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.HEADER, Names.create(Header.class));
    }

    @Override
    public Header read(AnnotationInstance annotation) {
        Header header = read(Header.class, annotation, "name");

        if (header.getExample() != null || header.getExamples() != null) {
            /*
             * Save the header for later parsing. The schema may not yet be set
             * so we do not know if it should be parsed.
             */
            scannerContext().getUnparsedExamples().add(header);
        }

        return header;
    }

    @Override
    protected boolean setProperty(Header model, AnnotationValue value) {
        switch (value.name()) {
            case PROP_SCHEMA:
                model.setSchema(schemaIO().read(value));
                return true;
            case PROP_EXAMPLES:
                model.setExamples(exampleObjectIO().readMap(value));
                return true;
            case PROP_EXAMPLE:
                model.setExample(value.asString());
                return true;
            default:
                break;
        }

        return false;
    }
}
