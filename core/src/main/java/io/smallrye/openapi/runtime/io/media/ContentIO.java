package io.smallrye.openapi.runtime.io.media;

import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.model.BaseModel;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class ContentIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Content, V, A, O, AB, OB> {

    private static final String[] EMPTY = new String[0];
    private static final String PROP_MEDIA_TYPE = "mediaType";

    /**
     * Simple enum to indicate whether an {@literal @}Content annotation being processed is
     * an input or an output.
     *
     * @author Eric Wittmann (eric.wittmann@gmail.com)
     */
    public enum Direction {
        INPUT,
        OUTPUT,
        PARAMETER
    }

    public static String[] defaultMediaTypes() {
        return new String[] { "*/*" };
    }

    public ContentIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.CONTENT, Names.create(Content.class));
    }

    public Content read(AnnotationValue annotations, Direction direction) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(annotationArray -> read(annotationArray, direction))
                .orElse(null);
    }

    private Content read(AnnotationInstance[] annotations, Direction direction) {
        IoLogging.logger.singleAnnotation("@Content");
        Content content = OASFactory.createContent();

        for (AnnotationInstance annotation : annotations) {
            String contentType = value(annotation, PROP_MEDIA_TYPE);
            MediaType mediaTypeModel = mediaTypeIO().read(annotation);

            if (contentType == null) {
                for (String mimeType : getDefaultMimeTypes(direction)) {
                    content.addMediaType(mimeType, maybeParseExamples(mimeType, mediaTypeModel, true));
                }
            } else {
                maybeParseExamples(contentType, mediaTypeModel, false);
                content.addMediaType(contentType, mediaTypeModel);
            }
        }

        return content;
    }

    private String[] getDefaultMimeTypes(Direction direction) {
        switch (direction) {
            case INPUT:
                return nonNullOrElse(scannerContext().getCurrentConsumes(), EMPTY);
            case OUTPUT:
                return nonNullOrElse(scannerContext().getCurrentProduces(), EMPTY);
            case PARAMETER:
                return defaultMediaTypes();
            default:
                return EMPTY;
        }
    }

    private MediaType maybeParseExamples(String contentType, MediaType model, boolean copyOnWrite) {
        boolean parseExamples;

        if (contentType.toUpperCase().contains("JSON")) {
            parseExamples = true;
        } else {
            Schema schema = model.getSchema();
            parseExamples = schema != null && SchemaSupport.getNonNullType(schema) != SchemaType.STRING;
        }

        if (parseExamples && (model.getExample() != null || model.getExamples() != null)) {
            if (copyOnWrite) {
                model = BaseModel.deepCopy(model, MediaType.class);
            }

            scannerContext().getUnparsedExamples().add(model);
        }

        return model;
    }

    static <T> T nonNullOrElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Override
    public Content read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("Reading a single @Content annotation is not supported");
    }
}
