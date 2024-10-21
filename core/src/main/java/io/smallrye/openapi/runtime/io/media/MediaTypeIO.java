package io.smallrye.openapi.runtime.io.media;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class MediaTypeIO<V, A extends V, O extends V, AB, OB> extends ModelIO<MediaType, V, A, O, AB, OB> {

    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_ENCODING = "encoding";
    private static final String PROP_SCHEMA = "schema";

    public MediaTypeIO(IOContext<V, A, O, AB, OB> context) {
        super(context, null, Names.create(MediaType.class));
    }

    @Override
    public MediaType read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotationAs("@Content", "MediaType");
        MediaType mediaType = OASFactory.createMediaType();
        mediaType.setExamples(exampleObjectIO().readMap(annotation.value(PROP_EXAMPLES)));
        mediaType.setExample(exampleObjectIO().parseValue(value(annotation, PROP_EXAMPLE)));
        mediaType.setSchema(schemaIO().read(annotation.value(PROP_SCHEMA)));
        mediaType.setEncoding(encodingIO().readMap(annotation.value(PROP_ENCODING)));
        mediaType.setExtensions(extensionIO().readExtensible(annotation));
        return mediaType;
    }
}
