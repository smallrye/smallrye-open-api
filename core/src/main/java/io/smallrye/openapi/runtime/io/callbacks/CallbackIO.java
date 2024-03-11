package io.smallrye.openapi.runtime.io.callbacks;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.api.models.callbacks.CallbackImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.PathItemIO;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;

public class CallbackIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Callback, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_OPERATIONS = "operations";
    private static final String PROP_CALLBACK_URL_EXPRESSION = "callbackUrlExpression";

    private final CallbackOperationIO<V, A, O, AB, OB> callbackOperationIO;
    private final PathItemIO<V, A, O, AB, OB> pathItemIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public CallbackIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO) {
        super(context, Names.CALLBACK, DotName.createSimple(Callback.class));
        callbackOperationIO = new CallbackOperationIO<>(context, contentIO, this);
        pathItemIO = new PathItemIO<>(context, callbackOperationIO, contentIO);
        extensionIO = new ExtensionIO<>(context);
    }

    @Override
    public Callback read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Callback");
        Callback callback = new CallbackImpl();
        callback.setRef(ReferenceType.CALLBACK.refValue(annotation));

        Optional.ofNullable(value(annotation, PROP_OPERATIONS))
                .map(AnnotationInstance[].class::cast)
                .map(pathItemIO::read)
                .ifPresent(pathItem -> callback.addPathItem(
                        value(annotation, PROP_CALLBACK_URL_EXPRESSION),
                        pathItem));

        callback.setExtensions(extensionIO.readExtensible(annotation));
        return callback;
    }

    @Override
    public Callback readObject(O node) {
        IoLogging.logger.singleJsonNode("Callback");
        Callback callback = new CallbackImpl();
        callback.setRef(readReference(node));

        jsonIO.properties(node)
                .stream()
                .filter(not(ExtensionIO::isExtension))
                .filter(not(this::isReference))
                .filter(property -> jsonIO.isObject(property.getValue()))
                .map(property -> entry(property.getKey(), pathItemIO.readValue(property.getValue())))
                .forEach(pathItem -> callback.addPathItem(pathItem.getKey(), pathItem.getValue()));

        extensionIO.readMap(node).forEach(callback::addExtension);

        return callback;
    }

    @Override
    public Optional<O> write(Callback model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
            } else {
                Optional.ofNullable(model.getPathItems())
                        .ifPresent(items -> items.forEach((key, value) -> setIfPresent(node, key, pathItemIO.write(value))));

                setAllIfPresent(node, extensionIO.write(model));
            }
            return node;
        }).map(jsonIO::buildObject);
    }
}
