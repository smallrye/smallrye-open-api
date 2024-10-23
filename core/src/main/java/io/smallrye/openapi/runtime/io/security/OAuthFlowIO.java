package io.smallrye.openapi.runtime.io.security;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class OAuthFlowIO<V, A extends V, O extends V, AB, OB> extends ModelIO<OAuthFlow, V, A, O, AB, OB> {

    private static final String PROP_SCOPES = "scopes";
    private static final String PROP_REFRESH_URL = "refreshUrl";
    private static final String PROP_TOKEN_URL = "tokenUrl";
    private static final String PROP_AUTHORIZATION_URL = "authorizationUrl";

    public OAuthFlowIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_FLOW, Names.create(OAuthFlow.class));
    }

    @Override
    public OAuthFlow read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@OAuthFlow");
        OAuthFlow flow = OASFactory.createOAuthFlow();
        flow.setAuthorizationUrl(value(annotation, PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(value(annotation, PROP_TOKEN_URL));
        flow.setRefreshUrl(value(annotation, PROP_REFRESH_URL));
        flow.setScopes(oauthScopeIO().readMap(annotation.value(PROP_SCOPES)));
        flow.setExtensions(extensionIO().readExtensible(annotation));
        return flow;
    }

}
