package io.smallrye.openapi.runtime.io.links;

import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class LinkParameterIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Object, V, A, O, AB, OB> {

    private static final String PROP_EXPRESSION = "expression";

    public LinkParameterIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_SCOPE, Names.create(String.class));
    }

    @Override
    public String read(AnnotationInstance annotation) {
        return value(annotation, PROP_EXPRESSION);
    }
}
