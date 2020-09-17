package io.smallrye.openapi.runtime.io.oauth;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionConstant;
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
        IoLogging.logger.singleAnnotation("@OAuthFlows");
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
        IoLogging.logger.singleJsonObject("OAuthFlows");
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
        IoLogging.logger.singleAnnotation("@OAuthFlow");
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
        IoLogging.logger.singleJsonObject("OAuthFlow");
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
    private static Map<String, String> readOAuthScopes(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsList("@OAuthScope");
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        Map<String, String> scopes = new LinkedHashMap<>();
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
    public static Map<String, String> readOAuthScopes(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        IoLogging.logger.jsonMap("OAuthScope");
        Map<String, String> scopes = new LinkedHashMap<>();

        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            if (ExtensionConstant.isExtensionField(fieldName)) {
                continue;
            }
            String value = JsonUtil.stringProperty(node, fieldName);
            scopes.put(fieldName, value);
        }
        return scopes;
    }
}
