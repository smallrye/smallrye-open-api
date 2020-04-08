package io.smallrye.openapi.runtime.io.parameter;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.Parameterizable;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.content.ContentWriter;
import io.smallrye.openapi.runtime.io.example.ExampleWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.schema.SchemaWriter;
import io.smallrye.openapi.runtime.util.StringUtil;

/**
 * Writing Parameter to json
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#parameter-object">parameter-object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ParameterWriter {

    private ParameterWriter() {
    }

    /**
     * Writes a map of {@link Parameter} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param parameters map of Parameter models
     */
    public static void writeParameters(ObjectNode parent, Map<String, Parameter> parameters) {
        if (parameters == null) {
            return;
        }
        ObjectNode parametersNode = parent.putObject(ComponentsConstant.PROP_PARAMETERS);
        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            writeParameter(parametersNode, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Writes a {@link Parameter} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeParameter(ObjectNode parent, Parameter model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        writeParameter(node, model);
    }

    /**
     * Writes a list of {@link Parameter} to the JSON tree.
     * 
     * @param parent the parent json node
     * @param models list of Parameter models
     */
    public static void writeParameterList(ObjectNode parent, List<Parameter> models) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(ComponentsConstant.PROP_PARAMETERS);
        for (Parameter model : models) {
            ObjectNode paramNode = node.addObject();
            writeParameter(paramNode, model);
        }
    }

    /**
     * Writes a {@link Parameter} into the JSON node.
     * 
     * @param node
     * @param model
     */
    private static void writeParameter(ObjectNode node, Parameter model) {
        if (StringUtil.isNotEmpty(model.getRef())) {
            JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
        } else {
            JsonUtil.stringProperty(node, Parameterizable.PROP_NAME, model.getName());
            JsonUtil.enumProperty(node, ParameterConstant.PROP_IN, model.getIn());
            JsonUtil.stringProperty(node, Parameterizable.PROP_DESCRIPTION, model.getDescription());
            JsonUtil.booleanProperty(node, Parameterizable.PROP_REQUIRED, model.getRequired());
            SchemaWriter.writeSchema(node, model.getSchema(), Parameterizable.PROP_SCHEMA);
            JsonUtil.booleanProperty(node, Parameterizable.PROP_ALLOW_EMPTY_VALUE, model.getAllowEmptyValue());
            JsonUtil.booleanProperty(node, Parameterizable.PROP_DEPRECATED, model.getDeprecated());
            JsonUtil.enumProperty(node, Parameterizable.PROP_STYLE, model.getStyle());
            JsonUtil.booleanProperty(node, Parameterizable.PROP_EXPLODE, model.getExplode());
            JsonUtil.booleanProperty(node, ParameterConstant.PROP_ALLOW_RESERVED, model.getAllowReserved());
            ObjectWriter.writeObject(node, Parameterizable.PROP_EXAMPLE, model.getExample());
            ExampleWriter.writeExamples(node, model.getExamples());
            ContentWriter.writeContent(node, model.getContent());
            ExtensionWriter.writeExtensions(node, model);
        }
    }

}
