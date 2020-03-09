package io.smallrye.openapi.api.reader;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.responses.APIResponsesImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Callback annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class CallbackOperationReader {
    private static final Logger LOG = Logger.getLogger(CallbackOperationReader.class);

    private CallbackOperationReader() {
    }

    /**
     * Reads a single CallbackOperation annotation.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}CallbackOperation annotation
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Operation model
     */
    public static Operation readOperation(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @CallbackOperation annotation.");
        Operation operation = new OperationImpl();
        operation.setSummary(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_SUMMARY));
        operation.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        operation.setExternalDocs(
                ExternalDocsReader.readExternalDocs(annotationInstance.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        operation.setParameters(ParameterReader.readParametersAsList(context,
                annotationInstance.value(OpenApiConstants.PROP_PARAMETERS), currentConsumes, currentProduces));
        operation.setRequestBody(RequestBodyReader.readRequestBody(context,
                annotationInstance.value(OpenApiConstants.PROP_REQUEST_BODY), currentConsumes, currentProduces));
        operation.setResponses(readCallbackOperationResponses(context,
                annotationInstance.value(OpenApiConstants.PROP_RESPONSES), currentConsumes, currentProduces));
        operation.setSecurity(SecurityReader.readSecurity(annotationInstance.value(OpenApiConstants.PROP_SECURITY)));
        operation.setExtensions(
                ExtensionReader.readExtensions(context, annotationInstance.value(OpenApiConstants.PROP_EXTENSIONS)));
        return operation;
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     * 
     * @param context the scanning context
     * @param annotationValue {@literal @}APIResponse annotation
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return APIResponses model
     */
    private static APIResponses readCallbackOperationResponses(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a list of @APIResponse annotations into an APIResponses model.");
        APIResponses responses = new APIResponsesImpl();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String responseCode = JandexUtil.stringValue(nested, OpenApiConstants.PROP_RESPONSE_CODE);
            if (responseCode != null) {
                responses.addAPIResponse(responseCode,
                        ResponseObjectReader.readResponse(context, nested, currentConsumes, currentProduces));
            }
        }
        return responses;
    }
}
