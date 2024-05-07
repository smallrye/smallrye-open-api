package io.smallrye.openapi.runtime.io.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.models.tags.TagImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;

public class TagIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Tag, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_NAME = "name";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_REFS = "refs";
    private static final String PROP_REF = "ref";
    private static final String PROP_EXTERNAL_DOCS = "externalDocs";

    public TagIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.TAG, Names.create(Tag.class));
    }

    public List<String> readReferences(AnnotationTarget target) {
        Stream<String> tagsRefs = Optional
                .ofNullable(scannerContext().annotations().<String[]> getAnnotationValue(target, Names.TAGS, PROP_REFS))
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
        tag.setExternalDocs(extDocIO().read(annotation.value(PROP_EXTERNAL_DOCS)));
        tag.setExtensions(extensionIO().readExtensible(annotation));

        return tag;
    }

    public List<Tag> readList(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isArray)
                .map(jsonIO()::asArray)
                .map(jsonIO()::entries)
                .map(Collection::stream)
                .map(elements -> {
                    IoLogging.logger.jsonArray("Tag");
                    return elements.filter(jsonIO()::isObject)
                            .map(jsonIO()::asObject)
                            .map(this::readObject)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public Tag readObject(O node) {
        IoLogging.logger.singleJsonNode("Tag");
        Tag tag = new TagImpl();
        tag.setName(jsonIO().getString(node, PROP_NAME));
        tag.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        tag.setExternalDocs(extDocIO().readValue(jsonIO().getValue(node, PROP_EXTERNAL_DOCS)));
        tag.setExtensions(extensionIO().readMap(node));
        return tag;
    }

    public Optional<A> write(List<Tag> models) {
        return optionalJsonArray(models).map(array -> {
            models.forEach(model -> write(model).ifPresent(v -> jsonIO().add(array, v)));
            return array;
        }).map(jsonIO()::buildArray);
    }

    public Optional<O> write(Tag model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_NAME, jsonIO().toJson(model.getName()));
            setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
            setIfPresent(node, PROP_EXTERNAL_DOCS, extDocIO().write(model.getExternalDocs()));
            setAllIfPresent(node, extensionIO().write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
