package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.info.InfoIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.responses.APIResponsesIO;
import io.smallrye.openapi.runtime.io.security.SecurityIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;
import io.smallrye.openapi.runtime.io.tags.TagIO;

public class OpenAPIDefinitionIO<V, A extends V, O extends V, AB, OB> extends ModelIO<OpenAPI, V, A, O, AB, OB> {

    public static final String PROP_COMPONENTS = "components";
    public static final String PROP_EXTERNAL_DOCS = "externalDocs";
    public static final String PROP_INFO = "info";
    public static final String PROP_OPENAPI = "openapi";
    public static final String PROP_PATHS = "paths";
    public static final String PROP_SECURITY = "security";
    public static final String PROP_SECURITY_SETS = "securitySets";
    public static final String PROP_SERVERS = "servers";
    public static final String PROP_TAGS = "tags";

    private final InfoIO<V, A, O, AB, OB> infoIO;
    private final TagIO<V, A, O, AB, OB> tagIO;
    private final ServerIO<V, A, O, AB, OB> serverIO;
    private final SecurityIO<V, A, O, AB, OB> securityIO;
    private final PathsIO<V, A, O, AB, OB> pathsIO;
    private final OperationIO<V, A, O, AB, OB> operationIO;
    private final ComponentsIO<V, A, O, AB, OB> componentIO;
    private final APIResponsesIO<V, A, O, AB, OB> responsesIO;
    private final ExternalDocumentationIO<V, A, O, AB, OB> externalDocIO;
    private final ParameterIO<V, A, O, AB, OB> parameterIO;
    private final RequestBodyIO<V, A, O, AB, OB> requestBodyIO;
    private final SchemaIO<V, A, O, AB, OB> schemaIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public OpenAPIDefinitionIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OPENAPI_DEFINITION, Names.create(OpenAPI.class));
        ContentIO<V, A, O, AB, OB> contentIO = new ContentIO<>(context);
        infoIO = new InfoIO<>(context);
        tagIO = new TagIO<>(context);
        serverIO = new ServerIO<>(context);
        securityIO = new SecurityIO<>(context);
        operationIO = new OperationIO<>(context, contentIO);
        pathsIO = new PathsIO<>(context, operationIO, contentIO);
        componentIO = new ComponentsIO<>(context, contentIO);
        responsesIO = new APIResponsesIO<>(context, contentIO);
        externalDocIO = new ExternalDocumentationIO<>(context);
        parameterIO = new ParameterIO<>(context, contentIO);
        requestBodyIO = new RequestBodyIO<>(context, contentIO);
        schemaIO = new SchemaIO<>(context);
        extensionIO = new ExtensionIO<>(context);
    }

    public TagIO<V, A, O, AB, OB> tags() {
        return tagIO;
    }

    public ServerIO<V, A, O, AB, OB> servers() {
        return serverIO;
    }

    public SecurityIO<V, A, O, AB, OB> security() {
        return securityIO;
    }

    public OperationIO<V, A, O, AB, OB> operations() {
        return operationIO;
    }

    public ComponentsIO<V, A, O, AB, OB> components() {
        return componentIO;
    }

    public APIResponsesIO<V, A, O, AB, OB> responses() {
        return responsesIO;
    }

    public ExternalDocumentationIO<V, A, O, AB, OB> externalDocumentation() {
        return externalDocIO;
    }

    public ParameterIO<V, A, O, AB, OB> parameters() {
        return parameterIO;
    }

    public RequestBodyIO<V, A, O, AB, OB> requestBodies() {
        return requestBodyIO;
    }

    public SchemaIO<V, A, O, AB, OB> schemas() {
        return schemaIO;
    }

    public ExtensionIO<V, A, O, AB, OB> extensions() {
        return extensionIO;
    }

    @Override
    public OpenAPI read(AnnotationInstance annotation) {
        IoLogging.logger.annotation("@OpenAPIDefinition");

        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(OpenApiConstants.OPEN_API_VERSION);
        openApi.setInfo(infoIO.read(annotation.value(PROP_INFO)));
        openApi.setTags(tagIO.readList(annotation.value(PROP_TAGS)));
        openApi.setServers(serverIO.readList(annotation.value(PROP_SERVERS)));
        openApi.setSecurity(securityIO.readRequirements(annotation.value(PROP_SECURITY), annotation.value(PROP_SECURITY_SETS)));
        openApi.setExternalDocs(externalDocIO.read(annotation.value(PROP_EXTERNAL_DOCS)));
        openApi.setComponents(componentIO.read(annotation.value(PROP_COMPONENTS)));
        openApi.setExtensions(extensionIO.readExtensible(annotation));

        return openApi;
    }

    /**
     * Reads a OpenAPIDefinition Json node.
     *
     * @param node
     *        the Json node
     */
    @Override
    public OpenAPI readObject(O node) {
        IoLogging.logger.jsonNode("OpenAPIDefinition");
        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(jsonIO().getString(node, PROP_OPENAPI));
        openApi.setInfo(infoIO.readValue(jsonIO().getValue(node, PROP_INFO)));
        openApi.setTags(tagIO.readList(jsonIO().getValue(node, PROP_TAGS)));
        openApi.setServers(serverIO.readList(jsonIO().getValue(node, PROP_SERVERS)));
        openApi.setSecurity(securityIO.readRequirements(jsonIO().getValue(node, PROP_SECURITY)));
        openApi.setExternalDocs(externalDocIO.readValue(jsonIO().getValue(node, PROP_EXTERNAL_DOCS)));
        openApi.setComponents(componentIO.readValue(jsonIO().getValue(node, PROP_COMPONENTS)));
        openApi.setPaths(pathsIO.readValue(jsonIO().getValue(node, PROP_PATHS)));
        openApi.setExtensions(extensionIO.readMap(node));
        return openApi;
    }

    @Override
    public Optional<O> write(OpenAPI model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_OPENAPI, jsonIO().toJson(model.getOpenapi()));
            setIfPresent(node, PROP_INFO, infoIO.write(model.getInfo()));
            setIfPresent(node, PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
            setIfPresent(node, PROP_SERVERS, serverIO.write(model.getServers()));
            setIfPresent(node, PROP_SECURITY, securityIO.write(model.getSecurity()));
            setIfPresent(node, PROP_TAGS, tagIO.write(model.getTags()));
            setIfPresent(node, PROP_PATHS, pathsIO.write(model.getPaths()));
            setIfPresent(node, PROP_COMPONENTS, componentIO.write(model.getComponents()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
