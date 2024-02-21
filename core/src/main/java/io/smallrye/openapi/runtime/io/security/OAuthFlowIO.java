package io.smallrye.openapi.runtime.io.security;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class OAuthFlowIO extends ModelIO<OAuthFlow> {

    private static final String PROP_SCOPES = "scopes";
    private static final String PROP_REFRESH_URL = "refreshUrl";
    private static final String PROP_TOKEN_URL = "tokenUrl";
    private static final String PROP_AUTHORIZATION_URL = "authorizationUrl";

    private final OAuthScopeIO oauthScopeIO;
    private final ExtensionIO extensionIO;

    protected OAuthFlowIO(AnnotationScannerContext context) {
        super(context, Names.OAUTH_FLOW, Names.create(OAuthFlow.class));
        oauthScopeIO = new OAuthScopeIO(context);
        extensionIO = new ExtensionIO(context);
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
    public OAuthFlow read(ObjectNode node) {
        IoLogging.logger.singleJsonObject("OAuthFlow");
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(JsonUtil.stringProperty(node, PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(JsonUtil.stringProperty(node, PROP_TOKEN_URL));
        flow.setRefreshUrl(JsonUtil.stringProperty(node, PROP_REFRESH_URL));
        flow.setScopes(oauthScopeIO.readMap(node.get(PROP_SCOPES)));
        flow.setExtensions(extensionIO.readMap(node));
        return flow;
    }

    @Override
    public Optional<ObjectNode> write(OAuthFlow model) {
        return optionalJsonObject(model).map(node -> {
            JsonUtil.stringProperty(node, PROP_AUTHORIZATION_URL, model.getAuthorizationUrl());
            JsonUtil.stringProperty(node, PROP_TOKEN_URL, model.getTokenUrl());
            JsonUtil.stringProperty(node, PROP_REFRESH_URL, model.getRefreshUrl());
            setIfPresent(node, PROP_SCOPES, oauthScopeIO.write(model.getScopes()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }

}
