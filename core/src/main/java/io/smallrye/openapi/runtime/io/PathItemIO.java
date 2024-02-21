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

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class PathItemIO extends ModelIO<PathItem> implements ReferenceIO {

    private static final String PROP_REF = "$ref";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_SERVERS = "servers";
    private static final String PROP_SUMMARY = "summary";
    private static final Set<String> OPERATION_PROPS = Arrays.asList(PathItem.HttpMethod.values())
            .stream()
            .map(Enum::toString)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    private final ServerIO serverIO;
    protected final OperationIO operationIO;
    private final ParameterIO parameterIO;
    protected final ExtensionIO extensionIO;

    public PathItemIO(AnnotationScannerContext context, OperationIO operationIO, ContentIO contentIO) {
        super(context, null, Names.create(PathItem.class));
        serverIO = new ServerIO(context);
        this.operationIO = operationIO;
        parameterIO = new ParameterIO(context, contentIO);
        extensionIO = new ExtensionIO(context);
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
    public PathItem read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("PathItem");
        PathItem pathItem = new PathItemImpl();
        pathItem.setRef(readReference(node));
        pathItem.setSummary(JsonUtil.stringProperty(node, PROP_SUMMARY));
        pathItem.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));

        node.properties().stream()
                .filter(entry -> OPERATION_PROPS.contains(entry.getKey()))
                .forEach(entry -> {
                    HttpMethod method = HttpMethod.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
                    Operation operation = operationIO.read(entry.getValue());
                    pathItem.setOperation(method, operation);
                });

        pathItem.setParameters(parameterIO.readList(node.get(PROP_PARAMETERS)));
        pathItem.setServers(serverIO.readList(node.get(PROP_SERVERS)));

        extensionIO.readMap(node).forEach(pathItem::addExtension);

        return pathItem;
    }

    public Optional<ObjectNode> write(PathItem model) {
        return optionalJsonObject(model).map(node -> write(model, node));
    }

    private ObjectNode write(PathItem model, ObjectNode node) {
        JsonUtil.stringProperty(node, PROP_REF, model.getRef());
        JsonUtil.stringProperty(node, PROP_SUMMARY, model.getSummary());
        JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());

        model.getOperations()
                .forEach((method, operation) -> setIfPresent(node, method.name().toLowerCase(), operationIO.write(operation)));

        setIfPresent(node, PROP_PARAMETERS, parameterIO.write(model.getParameters()));
        setIfPresent(node, PROP_SERVERS, serverIO.write(model.getServers()));
        setAllIfPresent(node, extensionIO.write(model));
        return node;
    }
}
