package io.smallrye.openapi.api.reader;

import static org.eclipse.microprofile.openapi.models.security.SecurityScheme.In;
import static org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
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
        Map<String, SecurityScheme> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readSecurityScheme(nested));
            }
        }
        return map;
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
        securityScheme.setType(JandexUtil.enumValue(annotationInstance, OpenApiConstants.PROP_TYPE, Type.class));
        securityScheme.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        securityScheme.setName(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_API_KEY_NAME));
        securityScheme.setIn(JandexUtil.enumValue(annotationInstance, OpenApiConstants.PROP_IN, In.class));
        securityScheme.setScheme(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_SCHEME));
        securityScheme.setBearerFormat(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_BEARER_FORMAT));
        securityScheme.setFlows(readOAuthFlows(annotationInstance.value(OpenApiConstants.PROP_FLOWS)));
        securityScheme
                .setOpenIdConnectUrl(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.SecurityScheme));
        return securityScheme;
    }

    /**
     * Reads an OAuthFlows annotation into a model.
     * 
     * @param value
     */
    private static OAuthFlows readOAuthFlows(final AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlows annotation.");
        AnnotationInstance annotation = value.asNested();
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(readOAuthFlow(annotation.value(OpenApiConstants.PROP_IMPLICIT)));
        flows.setPassword(readOAuthFlow(annotation.value(OpenApiConstants.PROP_PASSWORD)));
        flows.setClientCredentials(readOAuthFlow(annotation.value(OpenApiConstants.PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(readOAuthFlow(annotation.value(OpenApiConstants.PROP_AUTHORIZATION_CODE)));
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
        flow.setAuthorizationUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_TOKEN_URL));
        flow.setRefreshUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_REFRESH_URL));
        flow.setScopes(readOAuthScopes(annotation.value(OpenApiConstants.PROP_SCOPES)));
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
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name != null) {
                String description = JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION);
                scopes.put(name, description);
            }
        }
        return scopes;
    }
}
