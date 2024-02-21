package io.smallrye.openapi.runtime.io.security;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class OAuthScopeIO extends MapModelIO<String> {

    private static final String PROP_DESCRIPTION = "description";

    protected OAuthScopeIO(AnnotationScannerContext context) {
        super(context, Names.OAUTH_SCOPE, Names.create(String.class));
    }

    @Override
    public String read(AnnotationInstance annotation) {
        return value(annotation, PROP_DESCRIPTION);
    }

    @Override
    public Map<String, String> readMap(ObjectNode node) {
        IoLogging.logger.jsonNodeMap(modelName.local());
        return node.properties()
                .stream()
                .filter(property -> property.getValue().isValueNode())
                .map(property -> entry(property.getKey(), property.getValue().asText()))
                .collect(toLinkedMap());
    }

    @Override
    public String read(ObjectNode node) {
        throw new UnsupportedOperationException();
    }

    public Optional<ObjectNode> write(Map<String, String> models) {
        return optionalJsonObject(models)
                .map(node -> {
                    models.entrySet()
                            .stream()
                            .filter(e -> Objects.nonNull(e.getValue()))
                            .forEach(model -> node.put(model.getKey(), model.getValue()));
                    return node;
                });
    }

    @Override
    public Optional<ObjectNode> write(String model) {
        throw new UnsupportedOperationException();
    }

}
