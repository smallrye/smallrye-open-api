package io.smallrye.openapi.runtime.io.operation;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
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

    private OperationReader() {
    }

    /**
     * Reads a single Operation annotation.
     * 
     * @param context the scanning context
     * @param methodInfo the method
     * @return Operation model
     */
    public static Operation readOperation(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance,
            final MethodInfo methodInfo) {

        if (annotationInstance != null) {
            IoLogging.logger.singleAnnotation("@Operation");
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

            operation.setOperationId(JandexUtil.optionalStringValue(annotationInstance, OperationConstant.PROP_OPERATION_ID)
                    .orElse(getOperationId(context, methodInfo)));
            operation
                    .setDeprecated(JandexUtil.booleanValue(annotationInstance, OperationConstant.PROP_DEPRECATED).orElse(null));

            return operation;
        } else if (shouldDoAutoGenerate(context)) {
            Operation operation = new OperationImpl();
            operation.setOperationId(getOperationId(context, methodInfo));
            return operation;
        } else {
            return null;
        }
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
        IoLogging.logger.singleJsonObject("Operation");
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
        model.setDeprecated(JsonUtil.booleanProperty(node, OperationConstant.PROP_DEPRECATED).orElse(null));
        model.setSecurity(
                SecurityRequirementReader.readSecurityRequirements(node.get(OperationConstant.PROP_SECURITY)).orElse(null));
        model.setServers(ServerReader.readServers(node.get(OperationConstant.PROP_SERVERS)).orElse(null));
        ExtensionReader.readExtensions(node, model);
        return model;
    }

    // Helpers for scanner classes
    public static boolean operationIsHidden(final MethodInfo method) {
        AnnotationInstance operationAnnotation = method.annotation(OperationConstant.DOTNAME_OPERATION);
        if (operationAnnotation != null) {
            // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
            return operationAnnotation.value(OperationConstant.PROP_HIDDEN) != null
                    && operationAnnotation.value(OperationConstant.PROP_HIDDEN).asBoolean();
        }
        return false;
    }

    public static AnnotationInstance getOperationAnnotation(final MethodInfo method) {
        return method.annotation(OperationConstant.DOTNAME_OPERATION);
    }

    /**
     * This might (depending on config) auto generate a operation Id from the method and class names.
     * Or not.
     * 
     * @return an operation id, maybe
     */
    private static String getOperationId(final AnnotationScannerContext context,
            final MethodInfo method) {
        if (shouldDoAutoGenerate(context) && method != null) {
            OpenApiConfig.OperationIdStrategy operationIdStrategy = context.getConfig().getOperationIdStrategy();
            switch (operationIdStrategy) {
                case METHOD:
                    return method.name();
                case CLASS_METHOD:
                    return method.declaringClass().name().withoutPackagePrefix() + "_" + method.name();
                case PACKAGE_CLASS_METHOD:
                    return method.declaringClass().name() + "_" + method.name();
                default:
                    return null;
            }
        }
        return null;

    }

    private static boolean shouldDoAutoGenerate(final AnnotationScannerContext context) {
        // Try from config
        OpenApiConfig config = context.getConfig();
        OpenApiConfig.OperationIdStrategy operationIdStrategy = config.getOperationIdStrategy();
        return operationIdStrategy != null;
    }
}
