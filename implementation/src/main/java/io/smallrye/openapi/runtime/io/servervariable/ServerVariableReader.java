package io.smallrye.openapi.runtime.io.servervariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the ServerVariable annotation and json node
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverVariableObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerVariableReader {
    private static final Logger LOG = Logger.getLogger(ServerVariableReader.class);

    private ServerVariableReader() {
    }

    /**
     * Reads an array of ServerVariable annotations, returning a new {@link ServerVariables} model. The
     * annotation value is an array of ServerVariable annotations.
     * 
     * @param annotationValue an arrays of {@literal @}ServerVariable annotations
     * @return a Map of Variable name and ServerVariable model
     */
    public static Map<String, ServerVariable> readServerVariables(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an array of @ServerVariable annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (AnnotationInstance serverVariableAnno : nestedArray) {
            String name = JandexUtil.stringValue(serverVariableAnno, ServerVariableConstant.PROP_NAME);
            if (name != null) {
                variables.put(name, readServerVariable(serverVariableAnno));
            }
        }
        return variables;
    }

    /**
     * Reads the {@link ServerVariables} OpenAPI node.
     * 
     * @param node
     * @return a Map of Variable name and ServerVariable model
     */
    public static Map<String, ServerVariable> readServerVariables(final JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing a map of ServerVariable json node.");
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (Iterator<String> iterator = node.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            if (!fieldName.toLowerCase().startsWith(ExtensionConstant.EXTENSION_PROPERTY_PREFIX)) {
                JsonNode varNode = node.get(fieldName);
                variables.put(fieldName, readServerVariable(varNode));
            }
        }

        return variables;
    }

    /**
     * Reads a single ServerVariable annotation.
     * 
     * @param annotationInstance the {@literal @}ServerVariable annotation
     * @return the ServerVariable model
     */
    private static ServerVariable readServerVariable(final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @ServerVariable annotation.");
        ServerVariable variable = new ServerVariableImpl();
        variable.setDescription(
                JandexUtil.stringValue(annotationInstance, ServerVariableConstant.PROP_DESCRIPTION));
        variable.setEnumeration(
                JandexUtil.stringListValue(annotationInstance, ServerVariableConstant.PROP_ENUMERATION));
        variable.setDefaultValue(
                JandexUtil.stringValue(annotationInstance, ServerVariableConstant.PROP_DEFAULT_VALUE));
        return variable;
    }

    /**
     * Reads a list of {@link ServerVariable} OpenAPI nodes.
     * 
     * @param node the json node
     * @return the ServerVariable model
     */
    private static ServerVariable readServerVariable(JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing a single ServerVariable json node.");
        ServerVariable variable = new ServerVariableImpl();
        JsonNode enumNode = node.get(ServerVariableConstant.PROP_ENUM);
        if (enumNode != null && enumNode.isArray()) {
            List<String> enums = new ArrayList<>(enumNode.size());
            for (JsonNode n : enumNode) {
                enums.add(n.asText());
            }
            variable.setEnumeration(enums);
        }
        variable.setDefaultValue(JsonUtil.stringProperty(node, ServerVariableConstant.PROP_DEFAULT));
        variable.setDescription(JsonUtil.stringProperty(node, ServerVariableConstant.PROP_DESCRIPTION));
        ExtensionReader.readExtensions(node, variable);
        return variable;
    }

}
