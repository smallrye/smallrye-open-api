package io.smallrye.openapi.runtime.io.links;

import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class LinkParameterIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Object, V, A, O, AB, OB> {

    private static final String PROP_EXPRESSION = "expression";

    protected LinkParameterIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.OAUTH_SCOPE, Names.create(String.class));
    }

    @Override
    public String read(AnnotationInstance annotation) {
        return value(annotation, PROP_EXPRESSION);
    }

    @Override
    public Map<String, Object> readObjectMap(O node) {
        IoLogging.logger.jsonNodeMap(modelName.local());
        return jsonIO().properties(node)
                .stream()
                .filter(not(property -> jsonIO().isArray(property.getValue())))
                .filter(not(property -> jsonIO().isObject(property.getValue())))
                .map(property -> entry(property.getKey(), jsonIO().fromJson(property.getValue())))
                .collect(toLinkedMap());
    }

    @Override
    public Object readObject(O node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<O> write(Map<String, Object> models) {
        return optionalJsonObject(models).map(node -> {
            models.forEach((key, value) -> setIfPresent(node, key, jsonIO().toJson(value)));
            return node;
        }).map(jsonIO()::buildObject);
    }

    @Override
    public Optional<O> write(Object model) {
        throw new UnsupportedOperationException();
    }

}
