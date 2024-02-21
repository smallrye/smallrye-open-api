package io.smallrye.openapi.runtime.io.media;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ContentIO extends ModelIO<Content> {

    private final MediaTypeIO mediaTypeIO;

    public ContentIO(AnnotationScannerContext context) {
        super(context, Names.CONTENT, Names.create(Content.class));
        mediaTypeIO = new MediaTypeIO(context, this);
    }

    public Content read(AnnotationValue annotations, ContentDirection direction) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(annotationArray -> read(annotationArray, direction))
                .orElse(null);
    }

    private Content read(AnnotationInstance[] annotations, ContentDirection direction) {
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

    private String[] getDefaultMimeTypes(ContentDirection direction) {
        if (direction == ContentDirection.INPUT && context.getCurrentConsumes() != null) {
            return context.getCurrentConsumes();
        } else if (direction == ContentDirection.OUTPUT && context.getCurrentProduces() != null) {
            return context.getCurrentProduces();
        } else if (direction == ContentDirection.PARAMETER) {
            return OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
        } else {
            return new String[] {};
        }
    }

    @Override
    public Content read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("Reading a single @Content annotation is not supported");
    }

    @Override
    public Content read(ObjectNode node) {
        Content content = new ContentImpl();

        node.properties().forEach(property -> content.addMediaType(property.getKey(), mediaTypeIO.read(property.getValue())));

        return content;
    }

    public Optional<ObjectNode> write(Content model) {
        return optionalJsonObject(model).map(node -> {
            if (model.getMediaTypes() != null) {
                model.getMediaTypes().forEach((key, mediaType) -> setIfPresent(node, key, mediaTypeIO.write(mediaType)));
            }
            return node;
        });
    }
}
