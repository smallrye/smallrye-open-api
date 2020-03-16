package io.smallrye.openapi.runtime.io.paths;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.operation.OperationReader;
import io.smallrye.openapi.runtime.io.parameter.ParameterReader;
import io.smallrye.openapi.runtime.io.server.ServerReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Paths from annotation or json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#pathsObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class PathsReader {
    private static final Logger LOG = Logger.getLogger(PathsReader.class);

    private PathsReader() {
    }

    /**
     * Reads the {@link Paths} OpenAPI nodes.
     * 
     * @param node json object
     * @return Paths model
     */
    public static Paths readPaths(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        // LOG ...
        Paths paths = new PathsImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith(ExtensionConstant.EXTENSION_PROPERTY_PREFIX)) {
                continue;
            }
            paths.addPathItem(fieldName, readPathItem(node.get(fieldName)));
        }
        ExtensionReader.readExtensions(node, paths);
        return paths;
    }

    /**
     * Reads the PathItem.
     * Also used in CallbackOperation
     * 
     * @param context the scanning context
     * @param annotationValue the annotation value
     * @return PathItem model
     */
    public static PathItem readPathItem(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }

        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        PathItem pathItem = new PathItemImpl();
        for (AnnotationInstance operationAnno : nestedArray) {
            String method = JandexUtil.stringValue(operationAnno, PathsConstant.PROP_METHOD);
            Operation operation = OperationReader.readOperation(context, operationAnno);
            if (method == null) {
                continue;
            }
            try {
                PropertyDescriptor descriptor = new PropertyDescriptor(method.toUpperCase(), pathItem.getClass());
                Method mutator = descriptor.getWriteMethod();
                mutator.invoke(pathItem, operation);
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Error reading a CallbackOperation annotation.", e);
            }
        }
        return pathItem;
    }

    /**
     * Reads a {@link PathItem} OpenAPI node.
     * 
     * @param node json object
     * @return PathItem model
     */
    public static PathItem readPathItem(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing PathItem json nodes.");
        PathItem pathItem = new PathItemImpl();
        pathItem.setRef(JsonUtil.stringProperty(node, PathsConstant.PROP_$REF));
        pathItem.setSummary(JsonUtil.stringProperty(node, PathsConstant.PROP_SUMMARY));
        pathItem.setDescription(JsonUtil.stringProperty(node, PathsConstant.PROP_DESCRIPTION));
        pathItem.setGET(OperationReader.readOperation(node.get(PathsConstant.PROP_GET)));
        pathItem.setPUT(OperationReader.readOperation(node.get(PathsConstant.PROP_PUT)));
        pathItem.setPOST(OperationReader.readOperation(node.get(PathsConstant.PROP_POST)));
        pathItem.setDELETE(OperationReader.readOperation(node.get(PathsConstant.PROP_DELETE)));
        pathItem.setOPTIONS(OperationReader.readOperation(node.get(PathsConstant.PROP_OPTIONS)));
        pathItem.setHEAD(OperationReader.readOperation(node.get(PathsConstant.PROP_HEAD)));
        pathItem.setPATCH(OperationReader.readOperation(node.get(PathsConstant.PROP_PATCH)));
        pathItem.setTRACE(OperationReader.readOperation(node.get(PathsConstant.PROP_TRACE)));
        pathItem.setParameters(ParameterReader.readParameterList(node.get(PathsConstant.PROP_PARAMETERS)));
        pathItem.setServers(ServerReader.readServers(node.get(PathsConstant.PROP_SERVERS)));
        ExtensionReader.readExtensions(node, pathItem);
        return pathItem;
    }

}
