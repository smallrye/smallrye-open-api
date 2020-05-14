package io.smallrye.openapi.runtime.io.oauth;

import java.util.Iterator;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.Scopes;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.api.models.security.ScopesImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.securityscheme.SecuritySchemeConstant;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Oauth flow annotation
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#oauthFlowsObject">oauthFlowsObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class OAuthReader {
    private static final Logger LOG = Logger.getLogger(OAuthReader.class);

    private OAuthReader() {
    }

    /**
     * Reads an OAuthFlows annotation into a model.
     * 
     * @param annotationValue the annotation value
     * @return OAuthFlows model
     */
    public static OAuthFlows readOAuthFlows(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlows annotation.");
        AnnotationInstance annotation = annotationValue.asNested();
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(readOAuthFlow(annotation.value(SecuritySchemeConstant.PROP_IMPLICIT)));
        flows.setPassword(readOAuthFlow(annotation.value(SecuritySchemeConstant.PROP_PASSWORD)));
        flows.setClientCredentials(readOAuthFlow(annotation.value(SecuritySchemeConstant.PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(readOAuthFlow(annotation.value(SecuritySchemeConstant.PROP_AUTHORIZATION_CODE)));
        return flows;
    }

    /**
     * Reads a {@link OAuthFlows} OpenAPI node.
     * 
     * @param node the json object
     * @return OAuthFlows model
     */
    public static OAuthFlows readOAuthFlows(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single OAuthFlows json object.");
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(readOAuthFlow(node.get(SecuritySchemeConstant.PROP_IMPLICIT)));
        flows.setPassword(readOAuthFlow(node.get(SecuritySchemeConstant.PROP_PASSWORD)));
        flows.setClientCredentials(readOAuthFlow(node.get(SecuritySchemeConstant.PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(readOAuthFlow(node.get(SecuritySchemeConstant.PROP_AUTHORIZATION_CODE)));
        ExtensionReader.readExtensions(node, flows);
        return flows;
    }

    /**
     * Reads a single OAuthFlow annotation into a model.
     * 
     * @param annotationValue {@literal @}OAuthFlow annotation
     * @return OAuthFlow model
     */
    private static OAuthFlow readOAuthFlow(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlow annotation.");
        AnnotationInstance annotation = annotationValue.asNested();
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(JandexUtil.stringValue(annotation, SecuritySchemeConstant.PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(JandexUtil.stringValue(annotation, SecuritySchemeConstant.PROP_TOKEN_URL));
        flow.setRefreshUrl(JandexUtil.stringValue(annotation, SecuritySchemeConstant.PROP_REFRESH_URL));
        flow.setScopes(readOAuthScopes(annotation.value(SecuritySchemeConstant.PROP_SCOPES)));
        return flow;
    }

    /**
     * Reads a {@link OAuthFlow} OpenAPI node.
     * 
     * @param node json object
     * @return OAuthFlow model
     */
    private static OAuthFlow readOAuthFlow(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single OAuthFlow json object.");
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_TOKEN_URL));
        flow.setRefreshUrl(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_REFRESH_URL));
        flow.setScopes(readOAuthScopes(node.get(SecuritySchemeConstant.PROP_SCOPES)));
        ExtensionReader.readExtensions(node, flow);
        return flow;
    }

    /**
     * Reads an array of OAuthScope annotations into a Scopes model.
     * 
     * @param annotationValue {@literal @}OAuthScope annotation
     * @return Map of name and description of the scope
     */
    // TODO: Update return type and remove warning suppression for MicroProfile OpenAPI 2.0
    @SuppressWarnings("deprecation")
    private static Scopes readOAuthScopes(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a list of @OAuthScope annotations.");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Scopes scopes = new ScopesImpl();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, SecuritySchemeConstant.PROP_NAME);
            if (name != null) {
                String description = JandexUtil.stringValue(nested, SecuritySchemeConstant.PROP_DESCRIPTION);
                scopes.put(name, description);
            }
        }
        return scopes;
    }

    /**
     * Reads a map of OAuth scopes.
     * 
     * @param node json map
     * @return Map of name and description of the scope
     */
    // TODO: Update return type and remove warning suppression for MicroProfile OpenAPI 2.0
    @SuppressWarnings("deprecation")
    public static Scopes readOAuthScopes(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a json map of OAuthScope.");
        Scopes scopes = new ScopesImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            if (ExtensionReader.isExtensionField(fieldName)) {
                continue;
            }
            String value = JsonUtil.stringProperty(node, fieldName);
            scopes.put(fieldName, value);
        }
        return scopes;
    }
}
