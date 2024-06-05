package io.smallrye.openapi.runtime.io.security;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;

public class SecuritySchemeIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<SecurityScheme, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

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

    public SecuritySchemeIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SECURITY_SCHEME, Names.create(SecurityScheme.class));
    }

    @Override
    protected Optional<String> getName(AnnotationInstance annotation) {
        return getName(annotation, PROP_SECURITY_SCHEME_NAME);
    }

    @Override
    public SecurityScheme read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@SecurityScheme");
        SecurityScheme securityScheme = new SecuritySchemeImpl();
        securityScheme.setType(enumValue(annotation, PROP_TYPE, SecurityScheme.Type.class));
        securityScheme.setDescription(value(annotation, PROP_DESCRIPTION));
        securityScheme.setName(value(annotation, PROP_API_KEY_NAME));
        securityScheme.setIn(enumValue(annotation, PROP_IN, SecurityScheme.In.class));
        securityScheme.setScheme(value(annotation, PROP_SCHEME));
        securityScheme.setBearerFormat(value(annotation, PROP_BEARER_FORMAT));
        securityScheme.setFlows(oauthFlowsIO().read(annotation.value(PROP_FLOWS)));
        securityScheme.setOpenIdConnectUrl(value(annotation, PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(ReferenceType.SECURITY_SCHEME.refValue(annotation));
        securityScheme.setExtensions(extensionIO().readExtensible(annotation));
        return securityScheme;
    }

    @Override
    public SecurityScheme readObject(O node) {
        SecurityScheme model = new SecuritySchemeImpl();
        model.setRef(readReference(node));
        model.setType(enumValue(jsonIO().getValue(node, PROP_TYPE), SecurityScheme.Type.class));
        model.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        model.setName(jsonIO().getString(node, PROP_NAME));
        model.setIn(enumValue(jsonIO().getValue(node, PROP_IN), SecurityScheme.In.class));
        model.setScheme(jsonIO().getString(node, PROP_SCHEME));
        model.setBearerFormat(jsonIO().getString(node, PROP_BEARER_FORMAT));
        model.setFlows(oauthFlowsIO().readValue(jsonIO().getValue(node, PROP_FLOWS)));
        model.setOpenIdConnectUrl(jsonIO().getString(node, PROP_OPEN_ID_CONNECT_URL));
        model.setExtensions(extensionIO().readMap(node));
        return model;
    }

    @Override
    public Optional<O> write(SecurityScheme model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
                setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
            } else {
                setIfPresent(node, PROP_TYPE, jsonIO().toJson(model.getType()));
                setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                setIfPresent(node, PROP_NAME, jsonIO().toJson(model.getName()));
                setIfPresent(node, PROP_IN, jsonIO().toJson(model.getIn()));
                setIfPresent(node, PROP_SCHEME, jsonIO().toJson(model.getScheme()));
                setIfPresent(node, PROP_BEARER_FORMAT, jsonIO().toJson(model.getBearerFormat()));
                setIfPresent(node, PROP_FLOWS, oauthFlowsIO().write(model.getFlows()));
                setIfPresent(node, PROP_OPEN_ID_CONNECT_URL, jsonIO().toJson(model.getOpenIdConnectUrl()));
                setAllIfPresent(node, extensionIO().write(model));
            }

            return node;
        }).map(jsonIO()::buildObject);
    }
}
