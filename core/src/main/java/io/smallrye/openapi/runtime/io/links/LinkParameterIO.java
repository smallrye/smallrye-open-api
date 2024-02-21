package io.smallrye.openapi.runtime.io.links;

import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class LinkParameterIO extends MapModelIO<Object> {

    private static final String PROP_EXPRESSION = "expression";

    protected LinkParameterIO(AnnotationScannerContext context) {
        super(context, Names.OAUTH_SCOPE, Names.create(String.class));
    }

    @Override
    public String read(AnnotationInstance annotation) {
        return value(annotation, PROP_EXPRESSION);
    }

    @Override
    public Map<String, Object> readMap(ObjectNode node) {
        IoLogging.logger.jsonNodeMap(modelName.local());
        return node.properties()
                .stream()
                .filter(property -> property.getValue().isValueNode())
                .map(property -> entry(property.getKey(), JsonUtil.readObject(property.getValue())))
                .collect(toLinkedMap());
    }

    @Override
    public Object read(ObjectNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ObjectNode> write(Map<String, Object> models) {
        return optionalJsonObject(models).map(node -> {
            models.forEach((key, value) -> ObjectWriter.writeObject(node, key, value));
            return node;
        });
    }

    @Override
    public Optional<ObjectNode> write(Object model) {
        throw new UnsupportedOperationException();
    }

}
