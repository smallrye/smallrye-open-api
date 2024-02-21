package io.smallrye.openapi.runtime.io.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.tags.TagImpl;
import io.smallrye.openapi.runtime.io.ExternalDocumentationIO;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class TagIO extends ModelIO<Tag> implements ReferenceIO {

    private static final String PROP_NAME = "name";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_REFS = "refs";
    private static final String PROP_REF = "ref";
    private static final String PROP_EXTERNAL_DOCS = "externalDocs";

    private final ExternalDocumentationIO externalDocIO;
    private final ExtensionIO extensionIO;

    public TagIO(AnnotationScannerContext context) {
        super(context, Names.TAG, Names.create(Tag.class));
        externalDocIO = new ExternalDocumentationIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public List<String> readReferences(AnnotationTarget target) {
        Stream<String> tagsRefs = Optional
                .ofNullable(context.annotations().<String[]> getAnnotationValue(target, Names.TAGS, PROP_REFS))
                .map(Arrays::stream)
                .orElseGet(Stream::empty);
        Stream<String> tagRefs = getRepeatableAnnotations(target)
                .stream()
                .filter(this::isReference)
                .map(tag -> value(tag, PROP_REF));

        return Stream.concat(tagsRefs, tagRefs).collect(Collectors.toList());
    }

    public List<Tag> readList(AnnotationTarget target) {
        return readList(getRepeatableAnnotations(target));
    }

    public List<Tag> readList(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readList)
                .orElse(null);
    }

    public List<Tag> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<Tag> readList(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
                .filter(not(this::isReference))
                .map(this::read)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Tag read(AnnotationInstance annotation) {
        Objects.requireNonNull(annotation, "Tag annotation must not be null");
        IoLogging.logger.singleAnnotation("@Tag");
        Tag tag = new TagImpl();
        tag.setName(value(annotation, PROP_NAME));
        tag.setDescription(value(annotation, PROP_DESCRIPTION));
        tag.setExternalDocs(externalDocIO.read(annotation.value(PROP_EXTERNAL_DOCS)));
        tag.setExtensions(extensionIO.readExtensible(annotation));

        return tag;
    }

    public List<Tag> readList(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isArray)
                .map(ArrayNode.class::cast)
                .map(ArrayNode::elements)
                .map(elements -> Spliterators.spliteratorUnknownSize(elements, Spliterator.ORDERED))
                .map(elements -> StreamSupport.stream(elements, false))
                .map(elements -> {
                    IoLogging.logger.jsonArray("Tag");
                    return elements.filter(JsonNode::isObject)
                            .map(ObjectNode.class::cast)
                            .map(this::read)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public Tag read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Tag");
        Tag tag = new TagImpl();
        tag.setName(JsonUtil.stringProperty(node, PROP_NAME));
        tag.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        tag.setExternalDocs(externalDocIO.read(node.get(PROP_EXTERNAL_DOCS)));
        extensionIO.readMap(node).forEach(tag::addExtension);
        return tag;
    }

    public Optional<ArrayNode> write(List<Tag> models) {
        return optionalJsonArray(models).map(array -> {
            for (Tag model : models) {
                write(model).ifPresent(array::add);
            }
            return array;
        });
    }

    public Optional<ObjectNode> write(Tag model) {
        return optionalJsonObject(model).map(node -> {
            JsonUtil.stringProperty(node, PROP_NAME, model.getName());
            JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
            setIfPresent(node, PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
