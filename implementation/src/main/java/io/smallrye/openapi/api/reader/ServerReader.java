package io.smallrye.openapi.api.reader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Server annotation
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
        ServerImpl server = new ServerImpl();
        server.setUrl(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_URL));
        server.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        server.setVariables(readServerVariables(annotationInstance.value(OpenApiConstants.PROP_VARIABLES)));
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
            String name = JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_NAME);
            if (name != null) {
                variables.put(name, readServerVariable(serverVariableAnno));
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
        variable.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        variable.setEnumeration(JandexUtil.stringListValue(annotationInstance, OpenApiConstants.PROP_ENUMERATION));
        variable.setDefaultValue(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DEFAULT_VALUE));
        return variable;
    }
}
