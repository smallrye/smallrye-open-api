package io.smallrye.openapi.runtime.io.tag;

import java.util.List;

import org.eclipse.microprofile.openapi.models.tags.Tag;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.definition.DefinitionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsWriter;

/**
 * Writing the Tag to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#tagObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class TagWriter {

    private TagWriter() {
    }

    /**
     * Writes the {@link Tag} model array to the JSON tree.
     * 
     * @param node
     * @param tags
     */
    public static void writeTags(ObjectNode node, List<Tag> tags) {
        if (tags == null) {
            return;
        }
        ArrayNode array = node.putArray(DefinitionConstant.PROP_TAGS);
        for (Tag tag : tags) {
            ObjectNode tagNode = array.addObject();
            JsonUtil.stringProperty(tagNode, TagConstant.PROP_NAME, tag.getName());
            JsonUtil.stringProperty(tagNode, TagConstant.PROP_DESCRIPTION, tag.getDescription());
            ExternalDocsWriter.writeExternalDocumentation(tagNode, tag.getExternalDocs());
            ExtensionWriter.writeExtensions(tagNode, tag);
        }
    }

}
