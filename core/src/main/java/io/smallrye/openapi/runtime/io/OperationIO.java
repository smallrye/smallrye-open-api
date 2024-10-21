package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

public class OperationIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Operation, V, A, O, AB, OB> {

    protected static final String PROP_OPERATION_ID = "operationId";
    protected static final String PROP_TAGS = "tags";
    protected static final String PROP_DESCRIPTION = "description";
    protected static final String PROP_SECURITY = "security";
    protected static final String PROP_SECURITY_SETS = "securitySets";
    protected static final String PROP_REQUEST_BODY = "requestBody";
    protected static final String PROP_PARAMETERS = "parameters";
    protected static final String PROP_SERVERS = "servers";
    protected static final String PROP_SUMMARY = "summary";
    protected static final String PROP_DEPRECATED = "deprecated";
    protected static final String PROP_CALLBACKS = "callbacks";
    protected static final String PROP_HIDDEN = "hidden";
    protected static final String PROP_RESPONSES = "responses";
    protected static final String PROP_EXTERNAL_DOCS = "externalDocs";

    public OperationIO(IOContext<V, A, O, AB, OB> context) {
        this(context, Names.OPERATION);
    }

    public OperationIO(IOContext<V, A, O, AB, OB> context, DotName annotationName) {
        super(context, annotationName, Names.create(Operation.class));
    }

    public boolean isHidden(AnnotationTarget target) {
        return Optional.ofNullable(getAnnotation(target))
                .map(annotation -> this.<Boolean> value(annotation, PROP_HIDDEN))
                .orElse(false);
    }

    @Override
    public Operation read(AnnotationInstance annotationInstance) {
        IoLogging.logger.singleAnnotation("@Operation");
        Operation operation = OASFactory.createOperation();
        operation.setSummary(value(annotationInstance, PROP_SUMMARY));
        operation.setDescription(value(annotationInstance, PROP_DESCRIPTION));
        operation.setOperationId(value(annotationInstance, PROP_OPERATION_ID));
        operation.setDeprecated(value(annotationInstance, PROP_DEPRECATED));
        operation.setExtensions(extensionIO().readExtensible(annotationInstance));
        return operation;
    }
}
