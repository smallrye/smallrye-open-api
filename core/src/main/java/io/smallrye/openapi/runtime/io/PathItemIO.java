package io.smallrye.openapi.runtime.io;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.PathItem.HttpMethod;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;

public class PathItemIO<V, A extends V, O extends V, AB, OB> extends ModelIO<PathItem, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_SERVERS = "servers";
    private static final String PROP_SUMMARY = "summary";
    private static final Set<String> OPERATION_PROPS = Arrays.asList(PathItem.HttpMethod.values())
            .stream()
            .map(Enum::toString)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    private final ServerIO<V, A, O, AB, OB> serverIO;
    protected final OperationIO<V, A, O, AB, OB> operationIO;
    private final ParameterIO<V, A, O, AB, OB> parameterIO;
    protected final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public PathItemIO(IOContext<V, A, O, AB, OB> context, OperationIO<V, A, O, AB, OB> operationIO,
            ContentIO<V, A, O, AB, OB> contentIO) {
        super(context, null, Names.create(PathItem.class));
        serverIO = new ServerIO<>(context);
        this.operationIO = operationIO;
        parameterIO = new ParameterIO<>(context, contentIO);
        extensionIO = new ExtensionIO<>(context);
    }

    @Override
    public PathItem read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("@PathItem annotation does not exist");
    }

    public PathItem read(AnnotationInstance[] annotations) {
        PathItem pathItem = new PathItemImpl();

        Arrays.stream(annotations)
                .filter(annotation -> Objects.nonNull(value(annotation, "method")))
                .forEach(annotation -> {
                    String method = value(annotation, "method");
                    Operation operation = operationIO.read(annotation);
                    operation.setExtensions(extensionIO.readExtensible(annotation));
                    pathItem.setOperation(HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)), operation);
                });

        return pathItem;
    }

    @Override
    public PathItem readObject(O node) {
        IoLogging.logger.singleJsonNode("PathItem");
        PathItem pathItem = new PathItemImpl();
        pathItem.setRef(readReference(node));
        pathItem.setSummary(jsonIO.getString(node, PROP_SUMMARY));
        pathItem.setDescription(jsonIO.getString(node, PROP_DESCRIPTION));

        jsonIO.properties(node)
                .stream()
                .filter(entry -> OPERATION_PROPS.contains(entry.getKey()))
                .forEach(entry -> {
                    HttpMethod method = HttpMethod.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
                    Operation operation = operationIO.readValue(entry.getValue());
                    pathItem.setOperation(method, operation);
                });

        pathItem.setParameters(parameterIO.readList(jsonIO.getValue(node, PROP_PARAMETERS)));
        pathItem.setServers(serverIO.readList(jsonIO.getValue(node, PROP_SERVERS)));
        pathItem.setExtensions(extensionIO.readObjectMap(node));

        return pathItem;
    }

    @Override
    public Optional<O> write(PathItem model) {
        return optionalJsonObject(model).map(node -> write(model, node)).map(jsonIO::buildObject);
    }

    private OB write(PathItem model, OB node) {
        setReference(node, model);
        setIfPresent(node, PROP_SUMMARY, jsonIO.toJson(model.getSummary()));
        setIfPresent(node, PROP_DESCRIPTION, jsonIO.toJson(model.getDescription()));

        model.getOperations()
                .forEach((method, operation) -> setIfPresent(node, method.name().toLowerCase(), operationIO.write(operation)));

        setIfPresent(node, PROP_PARAMETERS, parameterIO.write(model.getParameters()));
        setIfPresent(node, PROP_SERVERS, serverIO.write(model.getServers()));
        setAllIfPresent(node, extensionIO.write(model));
        return node;
    }
}
