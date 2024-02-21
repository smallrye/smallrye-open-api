package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.runtime.io.callbacks.CallbackIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.responses.APIResponsesIO;
import io.smallrye.openapi.runtime.io.security.SecurityRequirementIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class OperationIO extends ModelIO<Operation> {

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

    protected final APIResponsesIO responsesIO;
    private final CallbackIO callbackIO;
    private final ServerIO serverIO;
    protected final ExternalDocumentationIO externalDocIO;
    private final ParameterIO parameterIO;
    private final RequestBodyIO requestBodyIO;
    private final SecurityRequirementIO securityRequirementIO;
    protected final ExtensionIO extensionIO;

    public OperationIO(AnnotationScannerContext context, ContentIO contentIO, CallbackIO callbackIO) {
        this(context, Names.OPERATION, contentIO, callbackIO);
    }

    public OperationIO(AnnotationScannerContext context, DotName annotationName, ContentIO contentIO, CallbackIO callbackIO) {
        super(context, annotationName, Names.create(Operation.class));
        responsesIO = new APIResponsesIO(context, contentIO);
        this.callbackIO = callbackIO != null ? callbackIO : new CallbackIO(context, contentIO);
        serverIO = new ServerIO(context);
        externalDocIO = new ExternalDocumentationIO(context);
        parameterIO = new ParameterIO(context, contentIO);
        requestBodyIO = new RequestBodyIO(context, contentIO);
        securityRequirementIO = new SecurityRequirementIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public OperationIO(AnnotationScannerContext context, ContentIO contentIO) {
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
        operation.setExtensions(extensionIO.readExtensible(annotationInstance));
        operation.setOperationId(context.annotations().value(annotationInstance, PROP_OPERATION_ID));
        operation.setDeprecated(context.annotations().value(annotationInstance, PROP_DEPRECATED));
        return operation;
    }

    @Override
    public Operation read(ObjectNode node) {
        IoLogging.logger.singleJsonObject("Operation");
        Operation model = new OperationImpl();
        model.setTags(JsonUtil.readStringArray(node.get(PROP_TAGS)).orElse(null));
        model.setSummary(JsonUtil.stringProperty(node, PROP_SUMMARY));
        model.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        model.setExternalDocs(externalDocIO.read(node.get(PROP_EXTERNAL_DOCS)));
        model.setOperationId(JsonUtil.stringProperty(node, PROP_OPERATION_ID));
        model.setParameters(parameterIO.readList(node.get(PROP_PARAMETERS)));
        model.setRequestBody(requestBodyIO.read(node.get(PROP_REQUEST_BODY)));
        model.setResponses(responsesIO.read(node.get(PROP_RESPONSES)));
        model.setCallbacks(callbackIO.readMap(node.get(PROP_CALLBACKS)));
        model.setDeprecated(JsonUtil.booleanProperty(node, PROP_DEPRECATED).orElse(null));
        model.setSecurity(securityRequirementIO.readList(node.get(PROP_SECURITY)));
        model.setServers(serverIO.readList(node.get(PROP_SERVERS)));
        extensionIO.readMap(node).forEach(model::addExtension);
        return model;
    }

    public Optional<ObjectNode> write(Operation model) {
        return optionalJsonObject(model).map(node -> {
            ObjectWriter.writeStringArray(node, model.getTags(), PROP_TAGS);
            JsonUtil.stringProperty(node, PROP_SUMMARY, model.getSummary());
            JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
            setIfPresent(node, PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
            JsonUtil.stringProperty(node, PROP_OPERATION_ID, model.getOperationId());
            setIfPresent(node, PROP_PARAMETERS, parameterIO.write(model.getParameters()));
            setIfPresent(node, PROP_REQUEST_BODY, requestBodyIO.write(model.getRequestBody()));
            setIfPresent(node, PROP_RESPONSES, responsesIO.write(model.getResponses()));
            setIfPresent(node, PROP_CALLBACKS, callbackIO.write(model.getCallbacks()));
            JsonUtil.booleanProperty(node, PROP_DEPRECATED, model.getDeprecated());
            setIfPresent(node, PROP_SECURITY, securityRequirementIO.write(model.getSecurity()));
            setIfPresent(node, PROP_SERVERS, serverIO.write(model.getServers()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
