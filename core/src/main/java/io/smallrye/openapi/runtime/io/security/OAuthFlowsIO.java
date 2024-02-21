package io.smallrye.openapi.runtime.io.security;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class OAuthFlowsIO extends ModelIO<OAuthFlows> {

    private static final String PROP_AUTHORIZATION_CODE = "authorizationCode";
    private static final String PROP_CLIENT_CREDENTIALS = "clientCredentials";
    private static final String PROP_IMPLICIT = "implicit";
    private static final String PROP_PASSWORD = "password";

    private final ExtensionIO extensionIO;
    private final OAuthFlowIO oauthFlowIO;

    protected OAuthFlowsIO(AnnotationScannerContext context) {
        super(context, Names.OAUTH_FLOWS, Names.create(OAuthFlows.class));
        extensionIO = new ExtensionIO(context);
        oauthFlowIO = new OAuthFlowIO(context);
    }

    @Override
    public OAuthFlows read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@OAuthFlows");
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(oauthFlowIO.read(annotation.value(PROP_IMPLICIT)));
        flows.setPassword(oauthFlowIO.read(annotation.value(PROP_PASSWORD)));
        flows.setClientCredentials(oauthFlowIO.read(annotation.value(PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(oauthFlowIO.read(annotation.value(PROP_AUTHORIZATION_CODE)));
        flows.setExtensions(extensionIO.readExtensible(annotation));
        return flows;
    }

    @Override
    public OAuthFlows read(ObjectNode node) {
        IoLogging.logger.singleJsonObject("OAuthFlows");
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(oauthFlowIO.read(node.get(PROP_IMPLICIT)));
        flows.setPassword(oauthFlowIO.read(node.get(PROP_PASSWORD)));
        flows.setClientCredentials(oauthFlowIO.read(node.get(PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(oauthFlowIO.read(node.get(PROP_AUTHORIZATION_CODE)));
        flows.setExtensions(extensionIO.readMap(node));
        return flows;
    }

    @Override
    public Optional<ObjectNode> write(OAuthFlows model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_IMPLICIT, oauthFlowIO.write(model.getImplicit()));
            setIfPresent(node, PROP_PASSWORD, oauthFlowIO.write(model.getPassword()));
            setIfPresent(node, PROP_CLIENT_CREDENTIALS, oauthFlowIO.write(model.getClientCredentials()));
            setIfPresent(node, PROP_AUTHORIZATION_CODE, oauthFlowIO.write(model.getAuthorizationCode()));
            setAllIfPresent(node, extensionIO.write(model));
            return node;
        });
    }

}
