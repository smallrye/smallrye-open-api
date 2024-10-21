package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.runtime.io.IOContext.OpenApiVersion;

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

        OpenAPI openApi = OASFactory.createOpenAPI();
        openApi.setOpenapi(SmallRyeOASConfig.Defaults.VERSION);
        openApi.setInfo(infoIO().read(annotation.value(PROP_INFO)));
        openApi.setTags(tagIO().readList(annotation.value(PROP_TAGS)));
        openApi.setServers(serverIO().readList(annotation.value(PROP_SERVERS)));
        openApi.setSecurity(
                securityIO().readRequirements(annotation.value(PROP_SECURITY), annotation.value(PROP_SECURITY_SETS)));
        openApi.setExternalDocs(extDocIO().read(annotation.value(PROP_EXTERNAL_DOCS)));
        openApi.setWebhooks(pathItemIO().readMap(annotation.value(PROP_WEBHOOKS)));
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
        String version = jsonIO().getString(node, PROP_OPENAPI);
        setOpenApiVersion(OpenApiVersion.fromString(version));
        return readObject(OpenAPI.class, node);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<O> write(OpenAPI model) {
        String version = model.getOpenapi();
        setOpenApiVersion(OpenApiVersion.fromString(version));
        return (Optional<O>) jsonIO().toJson(model, this);
    }
}
