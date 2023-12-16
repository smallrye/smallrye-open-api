package io.smallrye.openapi.runtime.io.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.tags.TagImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Tag from annotation or json
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#tagObject">tagObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class TagReader {

    private TagReader() {
    }

    /**
     * Reads any Tag annotations.The annotation
     * value is an array of Tag annotations.
     *
     * @param context scanning context
     * @param annotationValue an array of {@literal @}Tag annotations
     * @return List of Tag models
     */
    public static Optional<List<Tag>> readTags(final AnnotationScannerContext context, final AnnotationValue annotationValue) {
        if (annotationValue != null) {
            IoLogging.logger.annotationsArray("@Tag");
            AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
            List<Tag> tags = new ArrayList<>();
            for (AnnotationInstance tagAnno : nestedArray) {
                if (!JandexUtil.isRef(tagAnno)) {
                    tags.add(readTag(context, tagAnno));
                }
            }
            return Optional.of(tags);
        }
        return Optional.empty();
    }

    /**
     * Reads a list of {@link Tag} OpenAPI nodes.
     *
     * @param node the json array node
     * @return List of Tag models
     */
    public static Optional<List<Tag>> readTags(final JsonNode node) {
        if (node != null && node.isArray()) {
            IoLogging.logger.jsonArray("Tag");
            ArrayNode nodes = (ArrayNode) node;
            List<Tag> rval = new ArrayList<>(nodes.size());
            for (JsonNode tagNode : nodes) {
                rval.add(readTag(tagNode));
            }
            return Optional.of(rval);
        }
        return Optional.empty();
    }

    /**
     * Reads a single Tag annotation.
     *
     * @param context scanning context
     * @param annotationInstance {@literal @}Tag annotation, must not be null
     * @return Tag model
     */
    public static Tag readTag(final AnnotationScannerContext context, final AnnotationInstance annotationInstance) {
        Objects.requireNonNull(annotationInstance, "Tag annotation must not be null");
        IoLogging.logger.singleAnnotation("@Tag");
        Tag tag = new TagImpl();
        tag.setName(context.annotations().value(annotationInstance, TagConstant.PROP_NAME));
        tag.setDescription(context.annotations().value(annotationInstance, TagConstant.PROP_DESCRIPTION));
        tag.setExternalDocs(
                ExternalDocsReader.readExternalDocs(context,
                        annotationInstance.value(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        tag.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
        return tag;
    }

    /**
     * Reads a {@link Tag} OpenAPI node.
     *
     * @param node the json node
     * @return Tag model
     */
    private static Tag readTag(final JsonNode node) {
        IoLogging.logger.singleJsonNode("Tag");
        Tag tag = new TagImpl();
        tag.setName(JsonUtil.stringProperty(node, TagConstant.PROP_NAME));
        tag.setDescription(JsonUtil.stringProperty(node, TagConstant.PROP_DESCRIPTION));
        tag.setExternalDocs(ExternalDocsReader.readExternalDocs(node.get(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        ExtensionReader.readExtensions(node, tag);
        return tag;
    }

    // Helpers for scanner classes
    public static boolean hasTagAnnotation(AnnotationScannerContext context, AnnotationTarget target) {
        return context.annotations().hasAnnotation(target, TagConstant.DOTNAME_TAG, TagConstant.DOTNAME_TAGS);
    }

    public static List<AnnotationInstance> getTagAnnotations(AnnotationScannerContext context, AnnotationTarget target) {
        return context.annotations().getRepeatableAnnotation(target,
                TagConstant.DOTNAME_TAG,
                TagConstant.DOTNAME_TAGS);
    }

}
