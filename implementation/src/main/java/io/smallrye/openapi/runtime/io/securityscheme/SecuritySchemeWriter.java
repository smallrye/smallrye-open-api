package io.smallrye.openapi.runtime.io.securityscheme;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;

/**
 * Writing the Security Scheme to json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#securitySchemeObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecuritySchemeWriter {

    private SecuritySchemeWriter() {
    }

    /**
     * Writes a map of {@link SecurityScheme} to the JSON tree.
     * 
     * @param parent
     * @param securitySchemes
     */
    public static void writeSecuritySchemes(ObjectNode parent, Map<String, SecurityScheme> securitySchemes) {
        if (securitySchemes == null) {
            return;
        }
        ObjectNode securitySchemesNode = parent.putObject(ComponentsConstant.PROP_SECURITY_SCHEMES);
        for (String securitySchemeName : securitySchemes.keySet()) {
            writeSecurityScheme(securitySchemesNode, securitySchemes.get(securitySchemeName), securitySchemeName);
        }
    }

    /**
     * Writes a {@link SecurityScheme} object to the JSON tree.
     * 
     * @param parent
     * @param model
     * @param name
     */
    private static void writeSecurityScheme(ObjectNode parent, SecurityScheme model, String name) {
        if (model == null) {
            return;
        }
        ObjectNode node = parent.putObject(name);
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_$REF, model.getRef());
        JsonUtil.enumProperty(node, SecuritySchemeConstant.PROP_TYPE, model.getType());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_DESCRIPTION, model.getDescription());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_NAME, model.getName());
        JsonUtil.enumProperty(node, SecuritySchemeConstant.PROP_IN, model.getIn());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_SCHEME, model.getScheme());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_BEARER_FORMAT, model.getBearerFormat());
        writeOAuthFlows(node, model.getFlows());
        JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL, model.getOpenIdConnectUrl());
        ExtensionWriter.writeExtensions(node, model);
    }

    /**
     * Writes a {@link OAuthFlows} object to the JSON tree.
     * 
     * @param parent
     * @param model
     */
    private static void writeOAuthFlows(ObjectNode parent, OAuthFlows model) {
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
