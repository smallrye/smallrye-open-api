package io.smallrye.openapi.runtime.io.operation;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.callback.CallbackReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsConstant;
import io.smallrye.openapi.runtime.io.externaldocs.ExternalDocsReader;
import io.smallrye.openapi.runtime.io.parameter.ParameterReader;
import io.smallrye.openapi.runtime.io.requestbody.RequestBodyReader;
import io.smallrye.openapi.runtime.io.response.ResponseReader;
import io.smallrye.openapi.runtime.io.securityrequirement.SecurityRequirementReader;
import io.smallrye.openapi.runtime.io.server.ServerReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Operation from annotation or json
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject">operationObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class OperationReader {
    private static final Logger LOG = Logger.getLogger(OperationReader.class);

    private OperationReader() {
    }

    /**
     * Reads a single Operation annotation.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}CallbackOperation annotation
     * @return Operation model
     */
    public static Operation readOperation(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Operation annotation.");
        Operation operation = new OperationImpl();
        operation.setSummary(JandexUtil.stringValue(annotationInstance, OperationConstant.PROP_SUMMARY));
        operation.setDescription(JandexUtil.stringValue(annotationInstance, OperationConstant.PROP_DESCRIPTION));
        operation.setExternalDocs(
                ExternalDocsReader.readExternalDocs(annotationInstance.value(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        operation.setParameters(ParameterReader.readParametersList(context,
                annotationInstance.value(OperationConstant.PROP_PARAMETERS)).orElse(null));
        operation.setRequestBody(RequestBodyReader.readRequestBody(context,
                annotationInstance.value(OperationConstant.PROP_REQUEST_BODY)));
        operation.setResponses(ResponseReader.readResponses(context,
                annotationInstance.value(OperationConstant.PROP_RESPONSES)));
        operation.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(annotationInstance.value(OperationConstant.PROP_SECURITY)).orElse(null));
        operation.setExtensions(
                ExtensionReader.readExtensions(context,
                        annotationInstance.value(OperationConstant.PROP_EXTENSIONS)));

        // Below is only used in Jax-rs ??
        // Operation Id ??
        operation.setOperationId(JandexUtil.stringValue(annotationInstance, OperationConstant.PROP_OPERATION_ID));
        // Deprecated ??
        operation.setDeprecated(JandexUtil.booleanValue(annotationInstance, OperationConstant.PROP_DEPRECATED));

        // Below is not used ?
        // Tags ?
        // Callbacks
        // Servers
        return operation;
    }

    /**
     * Reads a {@link Operation} OpenAPI node.
     * 
     * @param node json object
     * @return Operation model
     */
    public static Operation readOperation(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single Operation json object.");
        Operation model = new OperationImpl();
        model.setTags(JsonUtil.readStringArray(node.get(OperationConstant.PROP_TAGS)).orElse(null));
        model.setSummary(JsonUtil.stringProperty(node, OperationConstant.PROP_SUMMARY));
        model.setDescription(JsonUtil.stringProperty(node, OperationConstant.PROP_DESCRIPTION));
        model.setExternalDocs(ExternalDocsReader.readExternalDocs(node.get(ExternalDocsConstant.PROP_EXTERNAL_DOCS)));
        model.setOperationId(JsonUtil.stringProperty(node, OperationConstant.PROP_OPERATION_ID));
        model.setParameters(ParameterReader.readParameterList(node.get(OperationConstant.PROP_PARAMETERS)).orElse(null));
        model.setRequestBody(RequestBodyReader.readRequestBody(node.get(OperationConstant.PROP_REQUEST_BODY)));
        model.setResponses(ResponseReader.readResponses(node.get(OperationConstant.PROP_RESPONSES)));
        model.setCallbacks(CallbackReader.readCallbacks(node.get(OperationConstant.PROP_CALLBACKS)));
        model.setDeprecated(JsonUtil.booleanProperty(node, OperationConstant.PROP_DEPRECATED));
        model.setSecurity(
                SecurityRequirementReader.readSecurityRequirements(node.get(OperationConstant.PROP_SECURITY)).orElse(null));
        model.setServers(ServerReader.readServers(node.get(OperationConstant.PROP_SERVERS)).orElse(null));
        ExtensionReader.readExtensions(node, model);
        return model;
    }

    // Helpers for scanner classes
    public static boolean methodHasOperationAnnotation(final MethodInfo method) {
        return method.hasAnnotation(OperationConstant.DOTNAME_OPERATION);
    }

    public static boolean operationIsHidden(final MethodInfo method) {
        AnnotationInstance operationAnnotation = method.annotation(OperationConstant.DOTNAME_OPERATION);
        // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
        return operationAnnotation.value(OperationConstant.PROP_HIDDEN) != null
                && operationAnnotation.value(OperationConstant.PROP_HIDDEN).asBoolean();
    }

    public static AnnotationInstance getOperationAnnotation(final MethodInfo method) {
        return method.annotation(OperationConstant.DOTNAME_OPERATION);
    }

}
