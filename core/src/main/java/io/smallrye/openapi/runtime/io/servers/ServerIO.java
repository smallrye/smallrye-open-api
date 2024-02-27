package io.smallrye.openapi.runtime.io.servers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class ServerIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Server, V, A, O, AB, OB> {

    private static final String PROP_VARIABLES = "variables";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_URL = "url";

    private final ServerVariableIO<V, A, O, AB, OB> serverVariableIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public ServerIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SERVER, Names.create(Server.class));
        serverVariableIO = new ServerVariableIO<>(context);
        extensionIO = new ExtensionIO<>(context);
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
    public List<Server> readList(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO::isArray)
                .map(jsonIO::asArray)
                .map(jsonIO::entries)
                .map(Collection::stream)
                .map(elements -> {
                    IoLogging.logger.jsonArray("Server");
                    return elements.filter(jsonIO::isObject)
                            .map(jsonIO::asObject)
                            .map(this::readObject)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public Server readObject(O node) {
        IoLogging.logger.singleJsonNode("Server");
        Server server = new ServerImpl();
        server.setUrl(jsonIO.getString(node, PROP_URL));
        server.setDescription(jsonIO.getString(node, PROP_DESCRIPTION));
        server.setVariables(serverVariableIO.readMap(jsonIO.getValue(node, PROP_VARIABLES)));
        server.setExtensions(extensionIO.readMap(node));
        return server;
    }

    public Optional<A> write(List<Server> models) {
        return optionalJsonArray(models).map(array -> {
            models.forEach(model -> {
                OB entry = jsonIO.createObject();
                write(model, entry);
                jsonIO.add(array, jsonIO.buildObject(entry));
            });
            return array;
        }).map(jsonIO::buildArray);
    }

    public Optional<O> write(Server model) {
        return optionalJsonObject(model).map(node -> {
            write(model, node);
            return node;
        }).map(jsonIO::buildObject);
    }

    private void write(Server model, OB node) {
        setIfPresent(node, PROP_URL, jsonIO.toJson(model.getUrl()));
        setIfPresent(node, PROP_DESCRIPTION, jsonIO.toJson(model.getDescription()));
        setIfPresent(node, PROP_VARIABLES, serverVariableIO.write(model.getVariables()));
        setAllIfPresent(node, extensionIO.write(model));
    }

}
