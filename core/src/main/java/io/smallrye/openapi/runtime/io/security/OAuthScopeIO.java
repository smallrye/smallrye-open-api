package io.smallrye.openapi.runtime.io.security;

import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class OAuthScopeIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<String, V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";

    public OAuthScopeIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_SCOPE, Names.create(String.class));
    }

    @Override
    public String read(AnnotationInstance annotation) {
        return value(annotation, PROP_DESCRIPTION);
    }
}
