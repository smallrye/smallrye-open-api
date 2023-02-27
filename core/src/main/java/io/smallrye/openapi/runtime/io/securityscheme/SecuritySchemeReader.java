package io.smallrye.openapi.runtime.io.securityscheme;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme.In;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.oauth.OAuthReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Security Scheme annotation
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#securitySchemeObject">securitySchemeObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SecuritySchemeReader {

    private SecuritySchemeReader() {
    }

    /**
     * Reads a map of SecurityScheme annotations.
     *
     * @param context scanning context
     * @param annotationValue Map of {@literal @}SecurityScheme annotations
     * @return Map of SecurityScheme models
     */
    public static Map<String, SecurityScheme> readSecuritySchemes(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsMap("@SecurityScheme");
        Map<String, SecurityScheme> securitySchemes = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, SecuritySchemeConstant.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                securitySchemes.put(name, readSecurityScheme(context, nested));
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
     * @param context scanning context
     * @param annotationInstance the {@literal @}SecurityScheme annotation
     * @return SecurityScheme model
     */
    public static SecurityScheme readSecurityScheme(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@SecurityScheme");
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
        securityScheme
                .setFlows(OAuthReader.readOAuthFlows(context, annotationInstance.value(SecuritySchemeConstant.PROP_FLOWS)));
        securityScheme
                .setOpenIdConnectUrl(
                        JandexUtil.stringValue(annotationInstance, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.SECURITY_SCHEME));
        securityScheme.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
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
        model.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        model.setType(readSecuritySchemeType(node.get(SecuritySchemeConstant.PROP_TYPE)));
        model.setDescription(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_DESCRIPTION));
        model.setName(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_NAME));
        model.setIn(readSecuritySchemeIn(node.get(SecuritySchemeConstant.PROP_IN)));
        model.setScheme(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_SCHEME));
        model.setBearerFormat(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_BEARER_FORMAT));
        model.setFlows(OAuthReader.readOAuthFlows(node.get(SecuritySchemeConstant.PROP_FLOWS)));
        model.setOpenIdConnectUrl(JsonUtil.stringProperty(node, SecuritySchemeConstant.PROP_OPEN_ID_CONNECT_URL));
        ExtensionReader.readExtensions(node, model);
        return model;
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
