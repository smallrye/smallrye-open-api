package io.smallrye.openapi.api.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.tags.Tag;

import io.smallrye.openapi.model.Extensions;

/**
 * An implementation of OASFilter that scans the OpenAPI model and removes
 * any tags from the top-level `tags` array that are not referenced by any
 * operation in the paths.
 */
public class UnusedTagFilter implements OASFilter {

    /**
     * Set of tag names that are referenced by at least one operation.
     */
    private final Set<String> referencedTagNames = new HashSet<>();

    @Override
    public Operation filterOperation(Operation operation) {
        List<String> operationTags = operation.getTags();

        if (operationTags != null) {
            referencedTagNames.addAll(operationTags);
        }

        return operation;
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        List<Tag> tags = openAPI.getTags();

        if (tags == null || tags.isEmpty()) {
            return;
        }

        List<Tag> tagsToKeep = new ArrayList<>();

        for (Tag tag : tags) {
            String tagName = tag.getName();

            // Keep tags that are referenced by operations
            if (tagName != null && referencedTagNames.contains(tagName)) {
                tagsToKeep.add(tag);
                continue;
            }

            // Keep tags marked with x-smallrye-directives=retain
            if (Extensions.getDirectives((Extensible<?>) tag).contains("retain")) {
                tagsToKeep.add(tag);
                continue;
            }

            // Tag is unused and not marked to retain, log removal
            UtilLogging.logger.unusedTagRemoved(tagName != null ? tagName : "<unnamed>");
        }

        // Update the tags list if any were removed
        if (tagsToKeep.size() != tags.size()) {
            openAPI.setTags(tagsToKeep);
        }
    }
}
