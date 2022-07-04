package io.smallrye.openapi.runtime.io.servervariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the ServerVariable annotation and json node
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#serverVariableObject">serverVariableObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerVariableReader {

    private ServerVariableReader() {
    }

    /**
     * Reads an array of ServerVariable annotations, returning a new {@link ServerVariable} model. The
     * annotation value is an array of ServerVariable annotations.
     * 
     * @param annotationValue an arrays of {@literal @}ServerVariable annotations
     * @return a Map of Variable name and ServerVariable model
     */
    public static Map<String, ServerVariable> readServerVariables(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsArray("@ServerVariable");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (AnnotationInstance serverVariableAnno : nestedArray) {
            String name = JandexUtil.stringValue(serverVariableAnno, ServerVariableConstant.PROP_NAME);
            if (name != null) {
                variables.put(name, readServerVariable(context, serverVariableAnno));
            }
        }
        return variables;
    }

    /**
     * Reads the {@link ServerVariable} OpenAPI node.
     * 
     * @param node the json node
     * @return a Map of Variable name and ServerVariable model
     */
    public static Map<String, ServerVariable> readServerVariables(final JsonNode node) {
        if (node == null) {
            return null;
        }
        IoLogging.logger.jsonNodeMap("ServerVariable");
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (Iterator<String> iterator = node.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            if (!ExtensionConstant.isExtensionField(fieldName)) {
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
    private static ServerVariable readServerVariable(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@ServerVariable");
        ServerVariable variable = new ServerVariableImpl();
        variable.setDescription(
                JandexUtil.stringValue(annotationInstance, ServerVariableConstant.PROP_DESCRIPTION));
        variable.setEnumeration(
                JandexUtil.stringListValue(annotationInstance, ServerVariableConstant.PROP_ENUMERATION).orElse(null));
        variable.setDefaultValue(
                JandexUtil.stringValue(annotationInstance, ServerVariableConstant.PROP_DEFAULT_VALUE));
        variable.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
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
        IoLogging.logger.singleJsonNode("ServerVariable");
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
