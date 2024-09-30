package io.smallrye.openapi.runtime.io.security;

import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;

public class SecuritySchemeIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<SecurityScheme, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

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
        SecurityScheme securityScheme = OASFactory.createSecurityScheme();
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
}
