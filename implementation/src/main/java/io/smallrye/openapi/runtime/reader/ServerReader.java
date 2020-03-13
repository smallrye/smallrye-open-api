package io.smallrye.openapi.runtime.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Server annotation and json node
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerReader {
    private static final Logger LOG = Logger.getLogger(ServerReader.class);

    private ServerReader() {
    }

    /**
     * Reads any Server annotations.The annotation value is an array of Server annotations.
     * 
     * @param annotationValue an Array of {@literal @}Server annotations
     * @return a List of Server models
     */
    public static List<Server> readServers(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an array of @Server annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        List<Server> servers = new ArrayList<>();
        for (AnnotationInstance serverAnno : nestedArray) {
            servers.add(readServer(serverAnno));
        }
        return servers;
    }

    /**
     * Reads a list of {@link Server} OpenAPI nodes.
     * 
     * @param node the json array
     * @return a List of Server models
     */
    public static List<Server> readServers(final JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        LOG.debug("Processing an array of Server json nodes.");
        ArrayNode nodes = (ArrayNode) node;
        List<Server> rval = new ArrayList<>(nodes.size());
        for (JsonNode serverNode : nodes) {
            rval.add(readServer(serverNode));
        }
        return rval;
    }

    /**
     * Reads a single Server annotation.
     * 
     * @param annotationValue the {@literal @}Server annotation
     * @return a Server model
     */
    public static Server readServer(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        return readServer(annotationValue.asNested());
    }

    /**
     * Reads a single Server annotation.
     * 
     * @param annotationInstance the {@literal @}Server annotations instance
     * @return Server model
     */
    public static Server readServer(final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Server annotation.");
        Server server = new ServerImpl();
        server.setUrl(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.SERVER.PROP_URL));
        server.setDescription(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.SERVER.PROP_DESCRIPTION));
        server.setVariables(readServerVariables(annotationInstance.value(MPOpenApiConstants.SERVER.PROP_VARIABLES)));
        return server;
    }

    /**
     * Reads a list of {@link Server} OpenAPI nodes.
     * 
     * @param node the json array
     * @return a List of Server models
     */
    public static Server readServer(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single Server json node.");

        Server server = new ServerImpl();
        server.setUrl(JsonUtil.stringProperty(node, MPOpenApiConstants.SERVER.PROP_URL));
        server.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.SERVER.PROP_DESCRIPTION));
        server.setVariables(readServerVariables(node.get(MPOpenApiConstants.SERVER.PROP_VARIABLES)));
        ExtensionReader.readExtensions(node, server);
        return server;
    }

    /**
     * Reads an array of ServerVariable annotations, returning a new {@link ServerVariables} model. The
     * annotation value is an array of ServerVariable annotations.
     * 
     * @param annotationValue an arrays of {@literal @}ServerVariable annotations
     * @return a Map of Variable name and ServerVariable model
     */
    private static Map<String, ServerVariable> readServerVariables(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an array of @ServerVariable annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (AnnotationInstance serverVariableAnno : nestedArray) {
            String name = JandexUtil.stringValue(serverVariableAnno, MPOpenApiConstants.SERVER_VARIABLE.PROP_NAME);
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
    private static Map<String, ServerVariable> readServerVariables(final JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing a map of ServerVariable json node.");
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (Iterator<String> iterator = node.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            if (!fieldName.toLowerCase().startsWith(MPOpenApiConstants.EXTENSIONS.EXTENSION_PROPERTY_PREFIX)) {
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
                JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.SERVER_VARIABLE.PROP_DESCRIPTION));
        variable.setEnumeration(
                JandexUtil.stringListValue(annotationInstance, MPOpenApiConstants.SERVER_VARIABLE.PROP_ENUMERATION));
        variable.setDefaultValue(
                JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.SERVER_VARIABLE.PROP_DEFAULT_VALUE));
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
        JsonNode enumNode = node.get(MPOpenApiConstants.SERVER_VARIABLE.PROP_ENUM);
        if (enumNode != null && enumNode.isArray()) {
            List<String> enums = new ArrayList<>(enumNode.size());
            for (JsonNode n : enumNode) {
                enums.add(n.asText());
            }
            variable.setEnumeration(enums);
        }
        variable.setDefaultValue(JsonUtil.stringProperty(node, MPOpenApiConstants.SERVER_VARIABLE.PROP_DEFAULT));
        variable.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.SERVER_VARIABLE.PROP_DESCRIPTION));
        ExtensionReader.readExtensions(node, variable);
        return variable;
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getServerAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                MPOpenApiConstants.SERVER.TYPE_SERVER,
                MPOpenApiConstants.SERVER.TYPE_SERVERS);
    }

}
