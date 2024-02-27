package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.callbacks.CallbackIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.responses.APIResponsesIO;
import io.smallrye.openapi.runtime.io.security.SecurityRequirementIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;

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

    protected final APIResponsesIO<V, A, O, AB, OB> responsesIO;
    private final CallbackIO<V, A, O, AB, OB> callbackIO;
    private final ServerIO<V, A, O, AB, OB> serverIO;
    protected final ExternalDocumentationIO<V, A, O, AB, OB> externalDocIO;
    private final ParameterIO<V, A, O, AB, OB> parameterIO;
    private final RequestBodyIO<V, A, O, AB, OB> requestBodyIO;
    private final SecurityRequirementIO<V, A, O, AB, OB> securityRequirementIO;
    protected final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public OperationIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO,
            CallbackIO<V, A, O, AB, OB> callbackIO) {
        this(context, Names.OPERATION, contentIO, callbackIO);
    }

    public OperationIO(IOContext<V, A, O, AB, OB> context, DotName annotationName, ContentIO<V, A, O, AB, OB> contentIO,
            CallbackIO<V, A, O, AB, OB> callbackIO) {
        super(context, annotationName, Names.create(Operation.class));
        responsesIO = new APIResponsesIO<>(context, contentIO);
        this.callbackIO = callbackIO != null ? callbackIO : new CallbackIO<>(context, contentIO);
        serverIO = new ServerIO<>(context);
        externalDocIO = new ExternalDocumentationIO<>(context);
        parameterIO = new ParameterIO<>(context, contentIO);
        requestBodyIO = new RequestBodyIO<>(context, contentIO);
        securityRequirementIO = new SecurityRequirementIO<>(context);
        extensionIO = new ExtensionIO<>(context);
    }

    public OperationIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO) {
        this(context, contentIO, null);
    }

    public boolean isHidden(AnnotationTarget target) {
        return Optional.ofNullable(getAnnotation(target))
                .map(annotation -> this.<Boolean> value(annotation, PROP_HIDDEN))
                .orElse(false);
    }

    @Override
    public Operation read(AnnotationInstance annotationInstance) {
        IoLogging.logger.singleAnnotation("@Operation");
        Operation operation = new OperationImpl();
        operation.setSummary(context.annotations().value(annotationInstance, PROP_SUMMARY));
        operation.setDescription(context.annotations().value(annotationInstance, PROP_DESCRIPTION));
        operation.setOperationId(context.annotations().value(annotationInstance, PROP_OPERATION_ID));
        operation.setDeprecated(context.annotations().value(annotationInstance, PROP_DEPRECATED));
        operation.setExtensions(extensionIO.readExtensible(annotationInstance));
        return operation;
    }

    @Override
    public Operation readObject(O node) {
        IoLogging.logger.singleJsonObject("Operation");
        Operation model = new OperationImpl();
        model.setTags(jsonIO.getArray(node, PROP_TAGS, jsonIO::asString).orElse(null));
        model.setSummary(jsonIO.getString(node, PROP_SUMMARY));
        model.setDescription(jsonIO.getString(node, PROP_DESCRIPTION));
        model.setExternalDocs(externalDocIO.readValue(jsonIO.getValue(node, PROP_EXTERNAL_DOCS)));
        model.setOperationId(jsonIO.getString(node, PROP_OPERATION_ID));
        model.setParameters(parameterIO.readList(jsonIO.getValue(node, PROP_PARAMETERS)));
        model.setRequestBody(requestBodyIO.readValue(jsonIO.getValue(node, PROP_REQUEST_BODY)));
        model.setResponses(responsesIO.readValue(jsonIO.getValue(node, PROP_RESPONSES)));
        model.setCallbacks(callbackIO.readMap(jsonIO.getValue(node, PROP_CALLBACKS)));
        model.setDeprecated(jsonIO.getBoolean(node, PROP_DEPRECATED));
        model.setSecurity(securityRequirementIO.readList(jsonIO.getValue(node, PROP_SECURITY)));
        model.setServers(serverIO.readList(jsonIO.getValue(node, PROP_SERVERS)));
        extensionIO.readMap(node).forEach(model::addExtension);
        return model;
    }

    public Optional<O> write(Operation model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_TAGS, jsonIO.toJson(model.getTags()));
            setIfPresent(node, PROP_SUMMARY, jsonIO.toJson(model.getSummary()));
            setIfPresent(node, PROP_DESCRIPTION, jsonIO.toJson(model.getDescription()));
            setIfPresent(node, PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
            setIfPresent(node, PROP_OPERATION_ID, jsonIO.toJson(model.getOperationId()));
            setIfPresent(node, PROP_PARAMETERS, parameterIO.write(model.getParameters()));
            setIfPresent(node, PROP_REQUEST_BODY, requestBodyIO.write(model.getRequestBody()));
            setIfPresent(node, PROP_RESPONSES, responsesIO.write(model.getResponses()));
            setIfPresent(node, PROP_CALLBACKS, callbackIO.write(model.getCallbacks()));
            setIfPresent(node, PROP_DEPRECATED, jsonIO.toJson(model.getDeprecated()));
            setIfPresent(node, PROP_SECURITY, securityRequirementIO.write(model.getSecurity()));
            setIfPresent(node, PROP_SERVERS, serverIO.write(model.getServers()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO::buildObject);
    }
}
