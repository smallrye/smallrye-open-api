package io.smallrye.openapi.runtime.io.callbacks;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.callbacks.CallbackImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.PathItemIO;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class CallbackIO extends MapModelIO<Callback> implements ReferenceIO {

    private static final String PROP_OPERATIONS = "operations";
    private static final String PROP_CALLBACK_URL_EXPRESSION = "callbackUrlExpression";

    private final ExtensionIO extensionIO;
    private final CallbackOperationIO callbackOperationIO;
    private final PathItemIO pathItemIO;

    public CallbackIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.CALLBACK, DotName.createSimple(Callback.class));
        extensionIO = new ExtensionIO(context);
        callbackOperationIO = new CallbackOperationIO(context, contentIO, this);
        pathItemIO = new PathItemIO(context, callbackOperationIO, contentIO);
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
    public Callback read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("Callback");
        Callback callback = new CallbackImpl();
        callback.setRef(readReference(node));

        node.properties()
                .stream()
                .filter(not(ExtensionIO::isExtension))
                .filter(not(this::isReference))
                .filter(property -> property.getValue().isObject())
                .map(property -> entry(property.getKey(), pathItemIO.read((ObjectNode) property.getValue())))
                .forEach(pathItem -> callback.addPathItem(pathItem.getKey(), pathItem.getValue()));

        extensionIO.readMap(node).forEach(callback::addExtension);

        return callback;
    }

    @Override
    public Optional<ObjectNode> write(Callback model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                Optional.ofNullable(model.getPathItems())
                        .ifPresent(items -> items.forEach((key, value) -> setIfPresent(node, key, pathItemIO.write(value))));

                setAllIfPresent(node, extensionIO.write(model));
            }
            return node;
        });
    }
}
