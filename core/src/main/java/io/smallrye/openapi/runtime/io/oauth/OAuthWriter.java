package io.smallrye.openapi.runtime.io.oauth;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeConstant;

/**
 * Writing the Security Scheme to json
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#oauthFlowsObject">oauthFlowsObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class OAuthWriter {

    private OAuthWriter() {
    }

    /**
     * Writes a {@link OAuthFlows} object to the JSON tree.
     * 
     * @param parent the parent json node
     * @param model the OAuthFlows model
     */
    public static void writeOAuthFlows(ObjectNode parent, OAuthFlows model) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(SecuritySchemeConstant.PROP_FLOWS);
        writeOAuthFlow(node, model.getImplicit(), SecuritySchemeConstant.PROP_IMPLICIT);
        writeOAuthFlow(node, model.getPassword(), SecuritySchemeConstant.PROP_PASSWORD);
        writeOAuthFlow(node, model.getClientCredentials(), SecuritySchemeConstant.PROP_CLIENT_CREDENTIALS);
        writeOAuthFlow(node, model.getAuthorizationCode(), SecuritySchemeConstant.PROP_AUTHORIZATION_CODE);
        ExtensionWriter.writeExtensions(node, model);
    }

    /**
     * Writes a {@link OAuthFlow} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeOAuthFlow(ObjectNode parent, OAuthFlow model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_AUTHORIZATION_URL, model.getAuthorizationUrl());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_TOKEN_URL, model.getTokenUrl());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_REFRESH_URL, model.getRefreshUrl());
        ObjectWriter.writeStringMap(node, model.getScopes(), SecuritySchemeConstant.PROP_SCOPES);
        ExtensionWriter.writeExtensions(node, model);
    }
}
