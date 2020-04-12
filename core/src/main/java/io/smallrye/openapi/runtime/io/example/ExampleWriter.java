package io.smallrye.openapi.runtime.io.example;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.util.StringUtil;

/**
 * Writing the Example to json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#exampleObject">exampleObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExampleWriter {

    private ExampleWriter() {
    }

    /**
     * Writes a map of {@link Example} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param examples map of Example models
     */
    public static void writeExamples(ObjectNode parent, Map<String, Example> examples) {
        if (examples == null) {
            return;
        }
        ObjectNode examplesNode = parent.putObject(ComponentsConstant.PROP_EXAMPLES);
        for (Map.Entry<String, Example> entry : examples.entrySet()) {
            writeExample(examplesNode, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Writes a {@link Example} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeExample(ObjectNode parent, Example model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);

        if (StringUtil.isNotEmpty(model.getRef())) {
            JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
        } else {
            JsonUtil.stringProperty(node, ExampleConstant.PROP_SUMMARY, model.getSummary());
            JsonUtil.stringProperty(node, ExampleConstant.PROP_DESCRIPTION, model.getDescription());
            ObjectWriter.writeObject(node, ExampleConstant.PROP_VALUE, model.getValue());
            JsonUtil.stringProperty(node, ExampleConstant.PROP_EXTERNAL_VALUE, model.getExternalValue());
            ExtensionWriter.writeExtensions(node, model);
        }
    }
}
