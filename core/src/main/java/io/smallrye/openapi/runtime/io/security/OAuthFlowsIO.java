package io.smallrye.openapi.runtime.io.security;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class OAuthFlowsIO<V, A extends V, O extends V, AB, OB> extends ModelIO<OAuthFlows, V, A, O, AB, OB> {

    private static final String PROP_AUTHORIZATION_CODE = "authorizationCode";
    private static final String PROP_CLIENT_CREDENTIALS = "clientCredentials";
    private static final String PROP_IMPLICIT = "implicit";
    private static final String PROP_PASSWORD = "password";

    public OAuthFlowsIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_FLOWS, Names.create(OAuthFlows.class));
    }

    @Override
    public OAuthFlows read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@OAuthFlows");
        OAuthFlows flows = OASFactory.createOAuthFlows();
        flows.setImplicit(oauthFlowIO().read(annotation.value(PROP_IMPLICIT)));
        flows.setPassword(oauthFlowIO().read(annotation.value(PROP_PASSWORD)));
        flows.setClientCredentials(oauthFlowIO().read(annotation.value(PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(oauthFlowIO().read(annotation.value(PROP_AUTHORIZATION_CODE)));
        flows.setExtensions(extensionIO().readExtensible(annotation));
        return flows;
    }
}
