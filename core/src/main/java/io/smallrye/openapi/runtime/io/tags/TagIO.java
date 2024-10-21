package io.smallrye.openapi.runtime.io.tags;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

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

    public List<String> readReferences(AnnotationInstance[] annotations) {
        return Optional.ofNullable(annotations)
                .map(Arrays::asList)
                .map(this::readReferences)
                .orElse(null);
    }

    public List<String> readReferences(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
                .map(a -> this.<String> value(a, PROP_REF))
                .filter(Objects::nonNull)
                .collect(toList());
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
        Tag tag = OASFactory.createTag();
        tag.setName(value(annotation, PROP_NAME));
        tag.setDescription(value(annotation, PROP_DESCRIPTION));
        tag.setExternalDocs(extDocIO().read(annotation.value(PROP_EXTERNAL_DOCS)));
        tag.setExtensions(extensionIO().readExtensible(annotation));

        return tag;
    }
}
