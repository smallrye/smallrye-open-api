package io.smallrye.openapi.runtime.io.callbacks;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.OperationIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.security.SecurityIO;

public class CallbackOperationIO<V, A extends V, O extends V, AB, OB> extends OperationIO<V, A, O, AB, OB> {

    private final RequestBodyIO<V, A, O, AB, OB> requestBodyIO;
    private final ParameterIO<V, A, O, AB, OB> parameterIO;
    private final SecurityIO<V, A, O, AB, OB> securityIO;

    public CallbackOperationIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO,
            CallbackIO<V, A, O, AB, OB> callbackIO, ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.CALLBACK_OPERATION, contentIO, callbackIO, extensionIO);
        parameterIO = new ParameterIO<>(context, contentIO, extensionIO);
        requestBodyIO = new RequestBodyIO<>(context, contentIO, extensionIO);
        securityIO = new SecurityIO<>(context, extensionIO);
    }

    @Override
    public Operation read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@CallbackOperation");
        Operation operation = new OperationImpl();
        operation.setSummary(value(annotation, PROP_SUMMARY));
        operation.setDescription(value(annotation, PROP_DESCRIPTION));
        operation.setExternalDocs(externalDocIO.read(annotation.value(PROP_EXTERNAL_DOCS)));
        operation.setParameters(parameterIO.readList(annotation.value(PROP_PARAMETERS)));
        operation.setRequestBody(requestBodyIO.read(annotation.value(PROP_REQUEST_BODY)));
        operation.setResponses(responsesIO.read(annotation.value(PROP_RESPONSES)));
        operation.setSecurity(securityIO.readRequirements(
                annotation.value(PROP_SECURITY),
                annotation.value(PROP_SECURITY_SETS)));
        operation.setExtensions(extensionIO.readExtensible(annotation));
        operation.setOperationId(value(annotation, PROP_OPERATION_ID));
        operation.setDeprecated(value(annotation, PROP_DEPRECATED));

        return operation;
    }

}
