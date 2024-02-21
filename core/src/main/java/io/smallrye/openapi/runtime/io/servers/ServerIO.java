package io.smallrye.openapi.runtime.io.servers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ServerIO extends ModelIO<Server> {

    private static final String PROP_VARIABLES = "variables";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_URL = "url";

    private final ServerVariableIO serverVariableIO;
    private final ExtensionIO extensionIO;

    public ServerIO(AnnotationScannerContext context) {
        super(context, Names.SERVER, Names.create(Server.class));
        serverVariableIO = new ServerVariableIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public List<Server> readList(AnnotationTarget target) {
        return readList(getRepeatableAnnotations(target));
    }

    public List<Server> readList(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readList)
                .orElse(null);
    }

    public List<Server> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<Server> readList(Collection<AnnotationInstance> annotations) {
        IoLogging.logger.annotationsArray("@Server");
        return annotations.stream()
                .map(this::read)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Server read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Server");
        Server server = new ServerImpl();
        server.setUrl(context.annotations().value(annotation, PROP_URL));
        server.setDescription(context.annotations().value(annotation, PROP_DESCRIPTION));
        server.setVariables(serverVariableIO.readMap(annotation.value(PROP_VARIABLES)));
        server.setExtensions(extensionIO.readExtensible(annotation));
        return server;
    }

    /**
     * Reads a list of {@link Server} OpenAPI nodes.
     *
     * @param node
     *        the json array
     * @return a List of Server models
     */
    public List<Server> readList(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isArray)
                .map(ArrayNode.class::cast)
                .map(ArrayNode::elements)
                .map(elements -> Spliterators.spliteratorUnknownSize(elements, Spliterator.ORDERED))
                .map(elements -> StreamSupport.stream(elements, false))
                .map(elements -> {
                    IoLogging.logger.jsonArray("Server");
                    return elements.filter(JsonNode::isObject)
                            .map(ObjectNode.class::cast)
                            .map(this::read)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public Server read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Server");
        Server server = new ServerImpl();
        server.setUrl(JsonUtil.stringProperty(node, PROP_URL));
        server.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        server.setVariables(serverVariableIO.readMap(node.get(PROP_VARIABLES)));
        extensionIO.readMap(node).forEach(server::addExtension);
        return server;
    }

    public Optional<ArrayNode> write(List<Server> models) {
        return optionalJsonArray(models)
                .map(array -> {
                    models.forEach(model -> write(model, array.addObject()));
                    return array;
                });
    }

    public Optional<ObjectNode> write(Server model) {
        return optionalJsonObject(model)
                .map(node -> {
                    write(model, node);
                    return node;
                });
    }

    private void write(Server model, ObjectNode node) {
        JsonUtil.stringProperty(node, PROP_URL, model.getUrl());
        JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
        setIfPresent(node, PROP_VARIABLES, serverVariableIO.write(model.getVariables()));
        setAllIfPresent(node, extensionIO.write(model));
    }

}
