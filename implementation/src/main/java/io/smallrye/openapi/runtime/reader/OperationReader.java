package io.smallrye.openapi.runtime.reader;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Operation from annotation or json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject
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
        operation.setSummary(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.OPERATION.PROP_SUMMARY));
        operation.setDescription(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.OPERATION.PROP_DESCRIPTION));
        operation.setExternalDocs(
                ExternalDocsReader.readExternalDocs(annotationInstance.value(MPOpenApiConstants.OPERATION.PROP_EXTERNAL_DOCS)));
        operation.setParameters(ParameterReader.readParametersList(context,
                annotationInstance.value(MPOpenApiConstants.OPERATION.PROP_PARAMETERS)));
        operation.setRequestBody(RequestBodyReader.readRequestBody(context,
                annotationInstance.value(MPOpenApiConstants.OPERATION.PROP_REQUEST_BODY)));
        operation.setResponses(ResponseReader.readResponses(context,
                annotationInstance.value(MPOpenApiConstants.OPERATION.PROP_RESPONSES)));
        operation.setSecurity(SecurityRequirementReader
                .readSecurityRequirements(annotationInstance.value(MPOpenApiConstants.OPERATION.PROP_SECURITY)));
        operation.setExtensions(
                ExtensionReader.readExtensions(context,
                        annotationInstance.value(MPOpenApiConstants.OPERATION.PROP_EXTENSIONS)));

        // Tags ?
        // Operation Id ?
        // Callbacks
        // Deprecated
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
        model.setTags(JsonUtil.readStringArray(node.get(MPOpenApiConstants.OPERATION.PROP_TAGS)));
        model.setSummary(JsonUtil.stringProperty(node, MPOpenApiConstants.OPERATION.PROP_SUMMARY));
        model.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.OPERATION.PROP_DESCRIPTION));
        model.setExternalDocs(ExternalDocsReader.readExternalDocs(node.get(MPOpenApiConstants.OPERATION.PROP_EXTERNAL_DOCS)));
        model.setOperationId(JsonUtil.stringProperty(node, MPOpenApiConstants.OPERATION.PROP_OPERATION_ID));
        model.setParameters(ParameterReader.readParameterList(node.get(MPOpenApiConstants.OPERATION.PROP_PARAMETERS)));
        model.setRequestBody(RequestBodyReader.readRequestBody(node.get(MPOpenApiConstants.OPERATION.PROP_REQUEST_BODY)));
        model.setResponses(ResponseReader.readResponses(node.get(MPOpenApiConstants.OPERATION.PROP_RESPONSES)));
        model.setCallbacks(CallbackReader.readCallbacks(node.get(MPOpenApiConstants.OPERATION.PROP_CALLBACKS)));
        model.setDeprecated(JsonUtil.booleanProperty(node, MPOpenApiConstants.OPERATION.PROP_DEPRECATED));
        model.setSecurity(
                SecurityRequirementReader.readSecurityRequirements(node.get(MPOpenApiConstants.OPERATION.PROP_SECURITY)));
        model.setServers(ServerReader.readServers(node.get(MPOpenApiConstants.OPERATION.PROP_SERVERS)));
        ExtensionReader.readExtensions(node, model);
        return model;
    }
}
