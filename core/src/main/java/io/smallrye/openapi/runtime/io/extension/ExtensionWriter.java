package io.smallrye.openapi.runtime.io.extension;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.Extensible;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.ObjectWriter;

/**
 * Writing the Extension to json
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#specificationExtensions">specificationExtensions</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExtensionWriter {

    private ExtensionWriter() {
    }

    /**
     * Writes extensions to the JSON tree.
     * 
     * @param node the json node
     * @param model the Extensible model
     */
    public static void writeExtensions(ObjectNode node, Extensible<?> model) {
        Map<String, Object> extensions = model.getExtensions();
        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : extensions.entrySet()) {
            String key = entry.getKey();
            if (!ExtensionConstant.isExtensionField(key)) {
                key = ExtensionConstant.EXTENSION_PROPERTY_PREFIX + key;
            }
            Object value = entry.getValue();
            ObjectWriter.writeObject(node, key, value);
        }
    }

}
