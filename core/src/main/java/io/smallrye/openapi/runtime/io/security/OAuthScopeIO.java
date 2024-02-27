package io.smallrye.openapi.runtime.io.security;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class OAuthScopeIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<String, V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";

    protected OAuthScopeIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_SCOPE, Names.create(String.class));
    }

    @Override
    public String read(AnnotationInstance annotation) {
        return value(annotation, PROP_DESCRIPTION);
    }

    @Override
    public Map<String, String> readObjectMap(O node) {
        IoLogging.logger.jsonNodeMap(modelName.local());
        return jsonIO.properties(node)
                .stream()
                .filter(not(property -> jsonIO.isArray(property.getValue())))
                .filter(not(property -> jsonIO.isObject(property.getValue())))
                .map(property -> entry(property.getKey(), jsonIO.asString(property.getValue())))
                .collect(toLinkedMap());
    }

    @Override
    public String readObject(O node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<O> write(Map<String, String> models) {
        return optionalJsonObject(models).map(node -> {
            models.entrySet()
                    .stream()
                    .filter(e -> Objects.nonNull(e.getValue()))
                    .forEach(model -> setIfPresent(node, model.getKey(), jsonIO.toJson(model.getValue())));
            return node;
        }).map(jsonIO::buildObject);
    }

    @Override
    public Optional<O> write(String model) {
        throw new UnsupportedOperationException();
    }

}
