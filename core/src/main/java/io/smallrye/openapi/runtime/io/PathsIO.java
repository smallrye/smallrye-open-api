package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.Paths;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class PathsIO extends ModelIO<Paths> {

    private final PathItemIO pathItemIO;
    private final ExtensionIO extensionIO;

    public PathsIO(AnnotationScannerContext context, OperationIO operationIO, ContentIO contentIO) {
        super(context, null, Names.create(Paths.class));
        pathItemIO = new PathItemIO(context, operationIO, contentIO);
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public Paths read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("@Paths annotation does not exist");
    }

    @Override
    public Paths read(ObjectNode node) {
        // LOG ...
        Paths paths = new PathsImpl();

        node.properties()
                .stream()
                .filter(not(ExtensionIO::isExtension))
                .filter(property -> property.getValue().isObject())
                .map(property -> entry(property.getKey(), pathItemIO.read((ObjectNode) property.getValue())))
                .forEach(pathItem -> paths.addPathItem(pathItem.getKey(), pathItem.getValue()));

        extensionIO.readMap(node).forEach(paths::addExtension);
        return paths;
    }

    public Optional<ObjectNode> write(Paths paths) {
        return optionalJsonObject(paths)
                .map(pathsNode -> {
                    if (paths.getPathItems() != null) {
                        paths.getPathItems().forEach((path, pathItem) -> {
                            setIfPresent(pathsNode, path, pathItemIO.write(pathItem));
                        });
                    }
                    setAllIfPresent(pathsNode, extensionIO.write(paths));
                    return pathsNode;
                });
    }

}
