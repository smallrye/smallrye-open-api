package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

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
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class OpenAPIDefinitionIO extends ModelIO<OpenAPI> {

    public static final String PROP_COMPONENTS = "components";
    public static final String PROP_EXTERNAL_DOCS = "externalDocs";
    public static final String PROP_INFO = "info";
    public static final String PROP_OPENAPI = "openapi";
    public static final String PROP_PATHS = "paths";
    public static final String PROP_SECURITY = "security";
    public static final String PROP_SECURITY_SETS = "securitySets";
    public static final String PROP_SERVERS = "servers";
    public static final String PROP_TAGS = "tags";

    private final InfoIO infoIO;
    private final TagIO tagIO;
    private final ServerIO serverIO;
    private final SecurityIO securityIO;
    private final PathsIO pathsIO;
    private final OperationIO operationIO;
    private final ComponentsIO componentIO;
    private final APIResponsesIO responsesIO;
    private final ExternalDocumentationIO externalDocIO;
    private final ParameterIO parameterIO;
    private final RequestBodyIO requestBodyIO;
    private final SchemaIO schemaIO;
    private final ExtensionIO extensionIO;

    public OpenAPIDefinitionIO(AnnotationScannerContext context) {
        super(context, Names.OPENAPI_DEFINITION, Names.create(OpenAPI.class));
        ContentIO contentIO = new ContentIO(context);
        infoIO = new InfoIO(context);
        tagIO = new TagIO(context);
        serverIO = new ServerIO(context);
        securityIO = new SecurityIO(context);
        operationIO = new OperationIO(context, contentIO);
        pathsIO = new PathsIO(context, operationIO, contentIO);
        componentIO = new ComponentsIO(context, contentIO);
        responsesIO = new APIResponsesIO(context, contentIO);
        externalDocIO = new ExternalDocumentationIO(context);
        parameterIO = new ParameterIO(context, contentIO);
        requestBodyIO = new RequestBodyIO(context, contentIO);
        schemaIO = new SchemaIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public TagIO tags() {
        return tagIO;
    }

    public ServerIO servers() {
        return serverIO;
    }

    public SecurityIO security() {
        return securityIO;
    }

    public OperationIO operations() {
        return operationIO;
    }

    public ComponentsIO components() {
        return componentIO;
    }

    public APIResponsesIO responses() {
        return responsesIO;
    }

    public ExternalDocumentationIO externalDocumentation() {
        return externalDocIO;
    }

    public ParameterIO parameters() {
        return parameterIO;
    }

    public RequestBodyIO requestBodies() {
        return requestBodyIO;
    }

    public SchemaIO schemas() {
        return schemaIO;
    }

    public ExtensionIO extensions() {
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
     * @param openApi
     *        the OpenAPI model
     * @param node
     *        the Json node
     */
    @Override
    public OpenAPI read(ObjectNode node) {
        IoLogging.logger.jsonNode("OpenAPIDefinition");

        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(JsonUtil.stringProperty(node, PROP_OPENAPI));
        openApi.setInfo(infoIO.read(node.get(PROP_INFO)));
        openApi.setTags(tagIO.readList(node.get(PROP_TAGS)));
        openApi.setServers(serverIO.readList(node.get(PROP_SERVERS)));
        openApi.setSecurity(securityIO.readRequirements(node.get(PROP_SECURITY)));
        openApi.setExternalDocs(externalDocIO.read(node.get(PROP_EXTERNAL_DOCS)));
        openApi.setComponents(componentIO.read(node.get(PROP_COMPONENTS)));
        openApi.setPaths(pathsIO.read(node.get(PROP_PATHS)));
        extensionIO.readMap(node).forEach(openApi::addExtension);

        return openApi;
    }

    @Override
    public Optional<ObjectNode> write(OpenAPI model) {
        return optionalJsonObject(model).map(node -> {
            JsonUtil.stringProperty(node, PROP_OPENAPI, model.getOpenapi());
            setIfPresent(node, PROP_INFO, infoIO.write(model.getInfo()));
            setIfPresent(node, PROP_EXTERNAL_DOCS, externalDocIO.write(model.getExternalDocs()));
            setIfPresent(node, PROP_SERVERS, serverIO.write(model.getServers()));
            setIfPresent(node, PROP_SECURITY, securityIO.write(model.getSecurity()));
            setIfPresent(node, PROP_TAGS, tagIO.write(model.getTags()));
            setIfPresent(node, PROP_PATHS, pathsIO.write(model.getPaths()));
            setIfPresent(node, PROP_COMPONENTS, componentIO.write(model.getComponents()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }
}
