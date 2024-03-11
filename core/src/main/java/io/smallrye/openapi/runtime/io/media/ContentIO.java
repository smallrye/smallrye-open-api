package io.smallrye.openapi.runtime.io.media;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class ContentIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Content, V, A, O, AB, OB> {

    private static final String[] EMPTY = new String[0];

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

    private final MediaTypeIO<V, A, O, AB, OB> mediaTypeIO;

    public ContentIO(IOContext<V, A, O, AB, OB> context, ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.CONTENT, Names.create(Content.class));
        mediaTypeIO = new MediaTypeIO<>(context, this, extensionIO);
    }

    public Content read(AnnotationValue annotations, Direction direction) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(annotationArray -> read(annotationArray, direction))
                .orElse(null);
    }

    private Content read(AnnotationInstance[] annotations, Direction direction) {
        IoLogging.logger.singleAnnotation("@Content");
        Content content = new ContentImpl();

        for (AnnotationInstance annotation : annotations) {
            String contentType = value(annotation, OpenApiConstants.PROP_MEDIA_TYPE);
            MediaType mediaTypeModel = mediaTypeIO.read(annotation);

            if (contentType == null) {
                for (String mimeType : getDefaultMimeTypes(direction)) {
                    content.addMediaType(mimeType, mediaTypeModel);
                }
            } else {
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
                return OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
            default:
                return EMPTY;
        }
    }

    static <T> T nonNullOrElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Override
    public Content read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("Reading a single @Content annotation is not supported");
    }

    @Override
    public Content readObject(O node) {
        Content content = new ContentImpl();

        jsonIO().properties(node)
                .forEach(property -> content.addMediaType(property.getKey(), mediaTypeIO.readValue(property.getValue())));

        return content;
    }

    @Override
    public Optional<O> write(Content model) {
        return optionalJsonObject(model).map(node -> {
            if (model.getMediaTypes() != null) {
                model.getMediaTypes().forEach((key, mediaType) -> setIfPresent(node, key, mediaTypeIO.write(mediaType)));
            }
            return node;
        }).map(jsonIO()::buildObject);
    }
}
