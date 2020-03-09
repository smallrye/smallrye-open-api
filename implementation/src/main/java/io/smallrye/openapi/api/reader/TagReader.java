package io.smallrye.openapi.api.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.tags.TagImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Tag annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#tagObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class TagReader {
    private static final Logger LOG = Logger.getLogger(TagReader.class);

    private TagReader() {
    }

    /**
     * Reads any Tag annotations.The annotation
     * value is an array of Tag annotations.
     * 
     * @param annotationValue an array of {@literal @}Tag annotations
     * @return List of Tag models
     */
    public static List<Tag> readTags(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an array of @Tag annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        List<Tag> tags = new ArrayList<>();
        for (AnnotationInstance tagAnno : nestedArray) {
            if (!JandexUtil.isRef(tagAnno)) {
                tags.add(readTag(tagAnno));
            }
        }
        return tags;
    }

    /**
     * Reads a single Tag annotation.
     * 
     * @param annotationInstance {@literal @}Tag annotation, must not be null
     * @return Tag model
     */
    public static Tag readTag(final AnnotationInstance annotationInstance) {
        Objects.requireNonNull(annotationInstance, "Tag annotation must not be null");
        LOG.debug("Processing a single @Tag annotation.");
        Tag tag = new TagImpl();
        tag.setName(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_NAME));
        tag.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        tag.setExternalDocs(ExternalDocsReader.readExternalDocs(annotationInstance.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        return tag;
    }
}
