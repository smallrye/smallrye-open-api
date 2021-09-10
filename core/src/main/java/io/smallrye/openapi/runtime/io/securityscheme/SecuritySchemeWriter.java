package io.smallrye.openapi.runtime.io.securityscheme;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.components.ComponentsConstant;
import io.smallrye.openapi.runtime.io.extension.ExtensionWriter;
import io.smallrye.openapi.runtime.io.oauth.OAuthWriter;
import io.smallrye.openapi.runtime.util.StringUtil;

/**
 * Writing the Security Scheme to json
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#securitySchemeObject">securitySchemeObject</a>
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
     * @param parent the parent json node
     * @param securitySchemes map of SecurityScheme models
     */
    public static void writeSecuritySchemes(ObjectNode parent, Map<String, SecurityScheme> securitySchemes) {
        if (securitySchemes == null) {
            return;
        }
        ObjectNode securitySchemesNode = parent.putObject(ComponentsConstant.PROP_SECURITY_SCHEMES);
        for (Map.Entry<String, SecurityScheme> entry : securitySchemes.entrySet()) {
            writeSecurityScheme(securitySchemesNode, entry.getValue(), entry.getKey());
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

        if (StringUtil.isNotEmpty(model.getRef())) {
            JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
        } else {
            JsonUtil.enumProperty(node, SecuritySchemeConstant.PROP_TYPE, model.getType());
            JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_DESCRIPTION, model.getDescription());
            JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_NAME, model.getName());
            JsonUtil.enumProperty(node, SecuritySchemeConstant.PROP_IN, model.getIn());
            JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_SCHEME, model.getScheme());
            JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_BEARER_FORMAT, model.getBearerFormat());
            OAuthWriter.writeOAuthFlows(node, model.getFlows());
            JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL, model.getOpenIdConnectUrl());
            ExtensionWriter.writeExtensions(node, model);
        }
    }

}
