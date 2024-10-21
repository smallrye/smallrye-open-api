package io.smallrye.openapi.internal.models;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.tags.Tag;

public class OpenAPI extends AbstractOpenAPI { // NOSONAR

    private static final Map<String, MergeDirective> MERGE = Map.of("openapi", MergeDirective.PRESERVE_VALUE);

    @Override
    protected MergeDirective mergeDirective(String name) {
        return MERGE.getOrDefault(name, MergeDirective.MERGE_VALUES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenAPI addTag(Tag tag) {
        if (tag == null) {
            return this;
        }

        String tagName = tag.getName();
        List<Tag> tags = getListProperty("tags");

        if (tagName == null || tags == null || tags.stream().noneMatch(t -> tagName.equals(t.getName()))) {
            addListPropertyEntry("tags", tag);
        }

        return this;
    }

}
