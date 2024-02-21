package io.smallrye.openapi.runtime.io.callbacks;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.OperationIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.security.SecurityIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class CallbackOperationIO extends OperationIO {

    private final RequestBodyIO requestBodyIO;
    private final ParameterIO parameterIO;
    private final SecurityIO securityIO;

    public CallbackOperationIO(AnnotationScannerContext context, ContentIO contentIO, CallbackIO callbackIO) {
        super(context, Names.CALLBACK_OPERATION, contentIO, callbackIO);
        parameterIO = new ParameterIO(context, contentIO);
        requestBodyIO = new RequestBodyIO(context, contentIO);
        securityIO = new SecurityIO(context);
    }

    @Override
    public Operation read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@CallbackOperation");
        Operation operation = new OperationImpl();
        operation.setSummary(context.annotations().value(annotation, PROP_SUMMARY));
        operation.setDescription(context.annotations().value(annotation, PROP_DESCRIPTION));
        operation.setExternalDocs(externalDocIO.read(annotation.value(PROP_EXTERNAL_DOCS)));
        operation.setParameters(parameterIO.readList(annotation.value(PROP_PARAMETERS)));
        operation.setRequestBody(requestBodyIO.read(annotation.value(PROP_REQUEST_BODY)));
        operation.setResponses(responsesIO.read(annotation.value(PROP_RESPONSES)));
        operation.setSecurity(securityIO.readRequirements(
            annotation.value(PROP_SECURITY),
            annotation.value(PROP_SECURITY_SETS)));
        operation.setExtensions(extensionIO.readExtensible(annotation));
        operation.setOperationId(context.annotations().value(annotation, PROP_OPERATION_ID));
        operation.setDeprecated(context.annotations().value(annotation, PROP_DEPRECATED));

        return operation;
    }

}
