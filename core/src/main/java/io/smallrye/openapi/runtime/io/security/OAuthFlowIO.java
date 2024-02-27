package io.smallrye.openapi.runtime.io.security;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class OAuthFlowIO<V, A extends V, O extends V, AB, OB> extends ModelIO<OAuthFlow, V, A, O, AB, OB> {

    private static final String PROP_SCOPES = "scopes";
    private static final String PROP_REFRESH_URL = "refreshUrl";
    private static final String PROP_TOKEN_URL = "tokenUrl";
    private static final String PROP_AUTHORIZATION_URL = "authorizationUrl";

    private final OAuthScopeIO<V, A, O, AB, OB> oauthScopeIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    protected OAuthFlowIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_FLOW, Names.create(OAuthFlow.class));
        oauthScopeIO = new OAuthScopeIO<>(context);
        extensionIO = new ExtensionIO<>(context);
    }

    @Override
    public OAuthFlow read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@OAuthFlow");
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(context.annotations().value(annotation, PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(context.annotations().value(annotation, PROP_TOKEN_URL));
        flow.setRefreshUrl(context.annotations().value(annotation, PROP_REFRESH_URL));
        flow.setScopes(oauthScopeIO.readMap(annotation.value(PROP_SCOPES)));
        flow.setExtensions(extensionIO.readExtensible(annotation));
        return flow;
    }

    @Override
    public OAuthFlow readObject(O node) {
        IoLogging.logger.singleJsonObject("OAuthFlow");
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(jsonIO.getString(node, PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(jsonIO.getString(node, PROP_TOKEN_URL));
        flow.setRefreshUrl(jsonIO.getString(node, PROP_REFRESH_URL));
        flow.setScopes(oauthScopeIO.readMap(jsonIO.getValue(node, PROP_SCOPES)));
        flow.setExtensions(extensionIO.readMap(node));
        return flow;
    }

    @Override
    public Optional<O> write(OAuthFlow model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_AUTHORIZATION_URL, jsonIO.toJson(model.getAuthorizationUrl()));
            setIfPresent(node, PROP_TOKEN_URL, jsonIO.toJson(model.getTokenUrl()));
            setIfPresent(node, PROP_REFRESH_URL, jsonIO.toJson(model.getRefreshUrl()));
            setIfPresent(node, PROP_SCOPES, oauthScopeIO.write(model.getScopes()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        }).map(jsonIO::buildObject);
    }

}
