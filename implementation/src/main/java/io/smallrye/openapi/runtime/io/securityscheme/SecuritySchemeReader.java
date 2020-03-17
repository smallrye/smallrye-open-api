package io.smallrye.openapi.runtime.io.securityscheme;

import static org.eclipse.microprofile.openapi.models.security.SecurityScheme.In;
import static org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Security Scheme annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#securitySchemeObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecuritySchemeReader {
    private static final Logger LOG = Logger.getLogger(SecuritySchemeReader.class);

    private SecuritySchemeReader() {
    }

    /**
     * Reads a map of SecurityScheme annotations.
     * 
     * @param annotationValue Map of {@literal @}SecurityScheme annotations
     * @return Map of SecurityScheme models
     */
    public static Map<String, SecurityScheme> readSecuritySchemes(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @SecurityScheme annotations.");
        Map<String, SecurityScheme> securitySchemes = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, SecuritySchemeConstant.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                securitySchemes.put(name, readSecurityScheme(nested));
            }
        }
        return securitySchemes;
    }

    /**
     * Reads the {@link SecurityScheme} OpenAPI nodes.
     * 
     * @param node map of json objects
     * @return Map of SecurityScheme models
     */
    public static Map<String, SecurityScheme> readSecuritySchemes(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, SecurityScheme> securitySchemes = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            securitySchemes.put(fieldName, readSecurityScheme(childNode));
        }

        return securitySchemes;
    }

    /**
     * Reads a SecurityScheme annotation into a model.
     * 
     * @param annotationInstance the {@literal @}SecurityScheme annotation
     * @return SecurityScheme model
     */
    public static SecurityScheme readSecurityScheme(final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @SecurityScheme annotation.");
        SecurityScheme securityScheme = new SecuritySchemeImpl();
        securityScheme
                .setType(JandexUtil.enumValue(annotationInstance, SecuritySchemeConstant.PROP_TYPE, Type.class));
        securityScheme
                .setDescription(JandexUtil.stringValue(annotationInstance, SecuritySchemeConstant.PROP_DESCRIPTION));
        securityScheme.setName(JandexUtil.stringValue(annotationInstance, SecuritySchemeConstant.PROP_API_KEY_NAME));
        securityScheme.setIn(JandexUtil.enumValue(annotationInstance, SecuritySchemeConstant.PROP_IN, In.class));
        securityScheme.setScheme(JandexUtil.stringValue(annotationInstance, SecuritySchemeConstant.PROP_SCHEME));
        securityScheme.setBearerFormat(
                JandexUtil.stringValue(annotationInstance, SecuritySchemeConstant.PROP_BEARER_FORMAT));
        securityScheme.setFlows(readOAuthFlows(annotationInstance.value(SecuritySchemeConstant.PROP_FLOWS)));
        securityScheme
                .setOpenIdConnectUrl(
                        JandexUtil.stringValue(annotationInstance, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.SecurityScheme));
        return securityScheme;
    }

    /**
     * Reads a {@link SecurityScheme} OpenAPI node.
     * 
     * @param node json node
     * @return SecurityScheme model
     */
    private static SecurityScheme readSecurityScheme(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        SecurityScheme model = new SecuritySchemeImpl();
        model.setRef(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_$REF));
        model.setType(readSecuritySchemeType(node.get(SecuritySchemeConstant.PROP_TYPE)));
        model.setDescription(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_DESCRIPTION));
        model.setName(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_NAME));
        model.setIn(readSecuritySchemeIn(node.get(SecuritySchemeConstant.PROP_IN)));
        model.setScheme(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_SCHEME));
        model.setBearerFormat(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_BEARER_FORMAT));
        model.setFlows(readOAuthFlows(node.get(SecuritySchemeConstant.PROP_FLOWS)));
        model.setOpenIdConnectUrl(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL));
        ExtensionReader.readExtensions(node, model);
        return model;
    }

    /**
     * Reads an OAuthFlows annotation into a model.
     * 
     * @param value the annotation value
     * @return OAuthFlows model
     */
    private static OAuthFlows readOAuthFlows(final AnnotationValue annotationValue) {
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
    private static OAuthFlows readOAuthFlows(final JsonNode node) {
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
    private static Map<String, String> readOAuthScopes(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a list of @OAuthScope annotations.");
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
     * Reads a {@link Scopes} OpenAPI node.
     * 
     * @param node json map
     * @return Map of name and description of the scope
     */
    private static Map<String, String> readOAuthScopes(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a json map of OAuthScope.");
        Map<String, String> scopes = new LinkedHashMap<>();
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

    /**
     * Reads a security scheme type.
     * 
     * @param node json node
     * @return Type enum
     */
    private static Type readSecuritySchemeType(final JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return SECURITY_SCHEME_TYPE_LOOKUP.get(node.asText());
    }

    /**
     * Reads a security scheme 'in' property.
     * 
     * @param node json node
     * @return In enum
     */
    private static In readSecuritySchemeIn(final JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return SECURITY_SCHEME_IN_LOOKUP.get(node.asText());
    }

    // helper methods for scanner
    public static List<AnnotationInstance> getSecuritySchemeAnnotations(final AnnotationTarget target) {
        return JandexUtil.getRepeatableAnnotation(target,
                SecuritySchemeConstant.DOTNAME_SECURITY_SCHEME,
                SecuritySchemeConstant.TYPE_SECURITY_SCHEMES);
    }

    public static String getSecuritySchemeName(AnnotationInstance annotation) {
        return JandexUtil.stringValue(annotation, SecuritySchemeConstant.PROP_SECURITY_SCHEME_NAME);
    }

    private static final Map<String, Type> SECURITY_SCHEME_TYPE_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, In> SECURITY_SCHEME_IN_LOOKUP = new LinkedHashMap<>();

    static {
        Type[] securitySchemeTypes = Type.values();
        for (Type type : securitySchemeTypes) {
            SECURITY_SCHEME_TYPE_LOOKUP.put(type.toString(), type);
        }

        In[] securitySchemeIns = In.values();
        for (In type : securitySchemeIns) {
            SECURITY_SCHEME_IN_LOOKUP.put(type.toString(), type);
        }
    }
}
