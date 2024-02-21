package io.smallrye.openapi.runtime.io.security;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class SecuritySchemeIO extends MapModelIO<SecurityScheme> implements ReferenceIO {

    private static final String PROP_NAME = "name";
    private static final String PROP_BEARER_FORMAT = "bearerFormat";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_FLOWS = "flows";
    private static final String PROP_IN = "in";
    private static final String PROP_SCHEME = "scheme";
    private static final String PROP_OPEN_ID_CONNECT_URL = "openIdConnectUrl";
    private static final String PROP_TYPE = "type";
    private static final String PROP_API_KEY_NAME = "apiKeyName";
    private static final String PROP_SECURITY_SCHEME_NAME = "securitySchemeName";

    private final OAuthFlowsIO oauthFlowsIO;
    private final ExtensionIO extensionIO;

    public SecuritySchemeIO(AnnotationScannerContext context) {
        super(context, Names.SECURITY_SCHEME, Names.create(SecurityScheme.class));
        oauthFlowsIO = new OAuthFlowsIO(context);
        extensionIO = new ExtensionIO(context);
    }

    @Override
    protected Optional<String> getName(AnnotationInstance annotation) {
        return getName(annotation, PROP_SECURITY_SCHEME_NAME);
    }

    @Override
    public SecurityScheme read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@SecurityScheme");
        SecurityScheme securityScheme = new SecuritySchemeImpl();
        securityScheme.setType(enumValue(annotation, PROP_TYPE,SecurityScheme.Type.class));
        securityScheme.setDescription(value(annotation, PROP_DESCRIPTION));
        securityScheme.setName(value(annotation, PROP_API_KEY_NAME));
        securityScheme.setIn(enumValue(annotation, PROP_IN, SecurityScheme.In.class));
        securityScheme.setScheme(value(annotation, PROP_SCHEME));
        securityScheme.setBearerFormat(value(annotation, PROP_BEARER_FORMAT));
        securityScheme.setFlows(oauthFlowsIO.read(annotation.value(PROP_FLOWS)));
        securityScheme.setOpenIdConnectUrl(value(annotation, PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(ReferenceType.SECURITY_SCHEME.refValue(annotation));
        securityScheme.setExtensions(extensionIO.readExtensible(annotation));
        return securityScheme;
    }

    @Override
    public SecurityScheme read(ObjectNode node) {
        SecurityScheme model = new SecuritySchemeImpl();
        model.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        model.setType(readSecuritySchemeType(node.get(PROP_TYPE)));
        model.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        model.setName(JsonUtil.stringProperty(node, PROP_NAME));
        model.setIn(readSecuritySchemeIn(node.get(PROP_IN)));
        model.setScheme(JsonUtil.stringProperty(node, PROP_SCHEME));
        model.setBearerFormat(JsonUtil.stringProperty(node, PROP_BEARER_FORMAT));
        model.setFlows(oauthFlowsIO.read(node.get(PROP_FLOWS)));
        model.setOpenIdConnectUrl(JsonUtil.stringProperty(node, PROP_OPEN_ID_CONNECT_URL));
        model.setExtensions(extensionIO.readMap(node));
        return model;
    }

    @Override
    public Optional<ObjectNode> write(SecurityScheme model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                JsonUtil.enumProperty(node, PROP_TYPE, model.getType());
                JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                JsonUtil.stringProperty(node, PROP_NAME, model.getName());
                JsonUtil.enumProperty(node, PROP_IN, model.getIn());
                JsonUtil.stringProperty(node, PROP_SCHEME, model.getScheme());
                JsonUtil.stringProperty(node, PROP_BEARER_FORMAT, model.getBearerFormat());
                setIfPresent(node, PROP_FLOWS, oauthFlowsIO.write(model.getFlows()));
                JsonUtil.stringProperty(node, PROP_OPEN_ID_CONNECT_URL, model.getOpenIdConnectUrl());
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        });
    }

    /**
     * Reads a security scheme type.
     *
     * @param node json node
     * @return Type enum
     */
    private static SecurityScheme.Type readSecuritySchemeType(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .flatMap(type -> Arrays.stream(SecurityScheme.Type.values()).filter(value -> type.equals(value.toString())).findFirst())
                .orElse(null);
    }

    /**
     * Reads a security scheme 'in' property.
     *
     * @param node json node
     * @return In enum
     */
    private static SecurityScheme.In readSecuritySchemeIn(final JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .flatMap(type -> Arrays.stream(SecurityScheme.In.values()).filter(value -> type.equals(value.toString())).findFirst())
                .orElse(null);
    }
}
