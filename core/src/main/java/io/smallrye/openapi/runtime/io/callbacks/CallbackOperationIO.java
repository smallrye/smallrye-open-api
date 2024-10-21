package io.smallrye.openapi.runtime.io.callbacks;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.OperationIO;

public class CallbackOperationIO<V, A extends V, O extends V, AB, OB> extends OperationIO<V, A, O, AB, OB> {

    public CallbackOperationIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.CALLBACK_OPERATION);
    }

    @Override
    public Operation read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@CallbackOperation");
        Operation operation = OASFactory.createOperation();
        operation.setSummary(value(annotation, PROP_SUMMARY));
        operation.setDescription(value(annotation, PROP_DESCRIPTION));
        operation.setExternalDocs(extDocIO().read(annotation.value(PROP_EXTERNAL_DOCS)));
        operation.setParameters(parameterIO().readList(annotation.value(PROP_PARAMETERS)));
        operation.setRequestBody(requestBodyIO().read(annotation.value(PROP_REQUEST_BODY)));
        operation.setResponses(apiResponsesIO().read(annotation.value(PROP_RESPONSES)));
        operation.setSecurity(securityIO().readRequirements(
                annotation.value(PROP_SECURITY),
                annotation.value(PROP_SECURITY_SETS)));
        operation.setExtensions(extensionIO().readExtensible(annotation));
        operation.setOperationId(value(annotation, PROP_OPERATION_ID));
        operation.setDeprecated(value(annotation, PROP_DEPRECATED));

        return operation;
    }

}
