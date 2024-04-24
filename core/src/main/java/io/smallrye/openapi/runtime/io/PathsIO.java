package io.smallrye.openapi.runtime.io;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.Paths;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class PathsIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Paths, V, A, O, AB, OB> {

    public PathsIO(IOContext<V, A, O, AB, OB> context) {
        super(context, null, Names.create(Paths.class));
    }

    @Override
    public Paths read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("@Paths annotation does not exist");
    }

    @Override
    public Paths readObject(O node) {
        // LOG ...
        Paths paths = new PathsImpl();

        jsonIO().properties(node)
                .stream()
                .filter(not(ExtensionIO::isExtension))
                .filter(property -> jsonIO().isObject(property.getValue()))
                .map(property -> entry(property.getKey(), pathItemIO().readObject(jsonIO().asObject(property.getValue()))))
                .forEach(pathItem -> paths.addPathItem(pathItem.getKey(), pathItem.getValue()));

        extensionIO().readMap(node).forEach(paths::addExtension);
        return paths;
    }

    public Optional<O> write(Paths paths) {
        return optionalJsonObject(paths).map(pathsNode -> {
            if (paths.getPathItems() != null) {
                paths.getPathItems().forEach((path, pathItem) -> setIfPresent(pathsNode, path, pathItemIO().write(pathItem)));
            }
            setAllIfPresent(pathsNode, extensionIO().write(paths));
            return pathsNode;
        }).map(jsonIO()::buildObject);
    }

}
