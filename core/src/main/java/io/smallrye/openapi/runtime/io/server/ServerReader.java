package io.smallrye.openapi.runtime.io.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.servervariable.ServerVariableReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Server annotation and json node
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#serverObject">serverObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerReader {

    private ServerReader() {
    }

    /**
     * Reads any Server annotations.The annotation value is an array of Server annotations.
     *
     * @param annotationValue an Array of {@literal @}Server annotations
     * @return a List of Server models
     */
    public static Optional<List<Server>> readServers(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue != null) {
            IoLogging.logger.annotationsArray("@Server");
            AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
            List<Server> servers = new ArrayList<>();
            for (AnnotationInstance serverAnno : nestedArray) {
                servers.add(readServer(context, serverAnno));
            }
            return Optional.of(servers);
        }
        return Optional.empty();
    }

    /**
     * Reads a list of {@link Server} OpenAPI nodes.
     *
     * @param node the json array
     * @return a List of Server models
     */
    public static Optional<List<Server>> readServers(final JsonNode node) {
        if (node != null && node.isArray()) {
            IoLogging.logger.jsonArray("Server");
            ArrayNode nodes = (ArrayNode) node;
            List<Server> rval = new ArrayList<>(nodes.size());
            for (JsonNode serverNode : nodes) {
                rval.add(readServer(serverNode));
            }
            return Optional.of(rval);
        }
        return Optional.empty();
    }

    /**
     * Reads a single Server annotation.
     *
     * @param annotationValue the {@literal @}Server annotation
     * @return a Server model
     */
    public static Server readServer(final AnnotationScannerContext context, final AnnotationValue annotationValue) {
        if (annotationValue != null) {
            return readServer(context, annotationValue.asNested());
        }
        return null;
    }

    /**
     * Reads a single Server annotation.
     *
     * @param annotationInstance the {@literal @}Server annotations instance
     * @return Server model
     */
    public static Server readServer(final AnnotationScannerContext context, final AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            IoLogging.logger.singleAnnotation("@Server");
            Server server = new ServerImpl();
            server.setUrl(JandexUtil.stringValue(annotationInstance, ServerConstant.PROP_URL));
            server.setDescription(JandexUtil.stringValue(annotationInstance, ServerConstant.PROP_DESCRIPTION));
            server.setVariables(
                    ServerVariableReader.readServerVariables(context, annotationInstance.value(ServerConstant.PROP_VARIABLES)));
            server.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
            return server;
        }
        return null;
    }

    /**
     * Reads a list of {@link Server} OpenAPI nodes.
     *
     * @param node the json array
     * @return a List of Server models
     */
    public static Server readServer(final JsonNode node) {
        if (node != null && node.isObject()) {
            IoLogging.logger.singleJsonNode("Server");
            Server server = new ServerImpl();
            server.setUrl(JsonUtil.stringProperty(node, ServerConstant.PROP_URL));
            server.setDescription(JsonUtil.stringProperty(node, ServerConstant.PROP_DESCRIPTION));
            server.setVariables(ServerVariableReader.readServerVariables(node.get(ServerConstant.PROP_VARIABLES)));
            ExtensionReader.readExtensions(node, server);
            return server;
        }
        return null;
    }

    // helper methods for scanners
    public static List<AnnotationInstance> getServerAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                ServerConstant.DOTNAME_SERVER,
                ServerConstant.DOTNAME_SERVERS);
    }

}
