package io.smallrye.openapi.runtime.io.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.servervariable.ServerVariableReader;
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
        server.setUrl(JandexUtil.stringValue(annotationInstance, ServerConstant.PROP_URL));
        server.setDescription(JandexUtil.stringValue(annotationInstance, ServerConstant.PROP_DESCRIPTION));
        server.setVariables(ServerVariableReader.readServerVariables(annotationInstance.value(ServerConstant.PROP_VARIABLES)));
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
        server.setUrl(JsonUtil.stringProperty(node, ServerConstant.PROP_URL));
        server.setDescription(JsonUtil.stringProperty(node, ServerConstant.PROP_DESCRIPTION));
        server.setVariables(ServerVariableReader.readServerVariables(node.get(ServerConstant.PROP_VARIABLES)));
        ExtensionReader.readExtensions(node, server);
        return server;
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getServerAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                ServerConstant.DOTNAME_SERVER,
                ServerConstant.DOTNAME_SERVERS);
    }

}
