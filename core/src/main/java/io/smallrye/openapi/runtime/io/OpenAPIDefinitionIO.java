package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.models.OpenAPIImpl;

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
    public static final String PROP_WEBHOOKS = "webhooks";

    public OpenAPIDefinitionIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OPENAPI_DEFINITION, Names.create(OpenAPI.class));
    }

    @Override
    public OpenAPI read(AnnotationInstance annotation) {
        IoLogging.logger.annotation("@OpenAPIDefinition");

        OpenAPI openApi = new OpenAPIImpl();
        openApi.setOpenapi(SmallRyeOASConfig.Defaults.VERSION);
        openApi.setInfo(infoIO().read(annotation.value(PROP_INFO)));
        openApi.setTags(tagIO().readList(annotation.value(PROP_TAGS)));
        openApi.setServers(serverIO().readList(annotation.value(PROP_SERVERS)));
        openApi.setSecurity(
                securityIO().readRequirements(annotation.value(PROP_SECURITY), annotation.value(PROP_SECURITY_SETS)));
        openApi.setExternalDocs(extDocIO().read(annotation.value(PROP_EXTERNAL_DOCS)));
        openApi.setComponents(componentsIO().read(annotation.value(PROP_COMPONENTS)));
        openApi.setExtensions(extensionIO().readExtensible(annotation));

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
        openApi.setInfo(infoIO().readValue(jsonIO().getValue(node, PROP_INFO)));
        openApi.setTags(tagIO().readList(jsonIO().getValue(node, PROP_TAGS)));
        openApi.setServers(serverIO().readList(jsonIO().getValue(node, PROP_SERVERS)));
        openApi.setSecurity(securityIO().readRequirements(jsonIO().getValue(node, PROP_SECURITY)));
        openApi.setExternalDocs(extDocIO().readValue(jsonIO().getValue(node, PROP_EXTERNAL_DOCS)));
        openApi.setComponents(componentsIO().readValue(jsonIO().getValue(node, PROP_COMPONENTS)));
        openApi.setPaths(pathsIO().readValue(jsonIO().getValue(node, PROP_PATHS)));
        openApi.setWebhooks(pathItemIO().readMap(jsonIO().getValue(node, PROP_WEBHOOKS)));
        openApi.setExtensions(extensionIO().readMap(node));
        return openApi;
    }

    @Override
    public Optional<O> write(OpenAPI model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_OPENAPI, jsonIO().toJson(model.getOpenapi()));
            setIfPresent(node, PROP_INFO, infoIO().write(model.getInfo()));
            setIfPresent(node, PROP_EXTERNAL_DOCS, extDocIO().write(model.getExternalDocs()));
            setIfPresent(node, PROP_SERVERS, serverIO().write(model.getServers()));
            setIfPresent(node, PROP_SECURITY, securityIO().write(model.getSecurity()));
            setIfPresent(node, PROP_TAGS, tagIO().write(model.getTags()));
            setIfPresent(node, PROP_PATHS, pathsIO().write(model.getPaths()));
            setIfPresent(node, PROP_WEBHOOKS, pathItemIO().write(model.getWebhooks()));
            setIfPresent(node, PROP_COMPONENTS, componentsIO().write(model.getComponents()));
            setAllIfPresent(node, extensionIO().write(model));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
