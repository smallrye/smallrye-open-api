package io.smallrye.openapi.runtime.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.tags.TagImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Reading the Tag from annotation or json
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
     * Reads a list of {@link Tag} OpenAPI nodes.
     * 
     * @param node the json array node
     * @return List of Tag models
     */
    public static List<Tag> readTags(final JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        LOG.debug("Processing an array of Tag json nodes.");
        ArrayNode nodes = (ArrayNode) node;
        List<Tag> rval = new ArrayList<>(nodes.size());
        for (JsonNode tagNode : nodes) {
            rval.add(readTag(tagNode));
        }
        return rval;
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
        tag.setName(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.TAG.PROP_NAME));
        tag.setDescription(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.TAG.PROP_DESCRIPTION));
        tag.setExternalDocs(
                ExternalDocsReader.readExternalDocs(annotationInstance.value(MPOpenApiConstants.TAG.PROP_EXTERNAL_DOCS)));
        return tag;
    }

    /**
     * Reads a {@link Tag} OpenAPI node.
     * 
     * @param node the json node
     * @return Tag model
     */
    private static Tag readTag(final JsonNode node) {
        LOG.debug("Processing a single Tag json node.");
        Tag tag = new TagImpl();
        tag.setName(JsonUtil.stringProperty(node, MPOpenApiConstants.TAG.PROP_NAME));
        tag.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.TAG.PROP_DESCRIPTION));
        tag.setExternalDocs(ExternalDocsReader.readExternalDocs(node.get(MPOpenApiConstants.TAG.PROP_EXTERNAL_DOCS)));
        ExtensionReader.readExtensions(node, tag);
        return tag;
    }

    // Helpers for scanner classes
    public static boolean hasTagAnnotation(final AnnotationTarget target) {
        return TypeUtil.hasAnnotation(target, MPOpenApiConstants.TAG.TYPE_TAG) ||
                TypeUtil.hasAnnotation(target, MPOpenApiConstants.TAG.TYPE_TAGS);
    }

    public static List<AnnotationInstance> getTagAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                MPOpenApiConstants.TAG.TYPE_TAG,
                MPOpenApiConstants.TAG.TYPE_TAGS);
    }

}
