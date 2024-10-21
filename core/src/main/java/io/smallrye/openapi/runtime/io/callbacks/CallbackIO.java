package io.smallrye.openapi.runtime.io.callbacks;

import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;

public class CallbackIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Callback, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_OPERATIONS = "operations";
    private static final String PROP_CALLBACK_URL_EXPRESSION = "callbackUrlExpression";
    private static final String PROP_PATH_ITEM_REF = "pathItemRef";

    public CallbackIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.CALLBACK, DotName.createSimple(Callback.class));
    }

    @Override
    public Callback read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Callback");
        Callback callback = OASFactory.createCallback();
        callback.setRef(ReferenceType.CALLBACK.refValue(annotation));

        Optional.ofNullable(this.<String> value(annotation, PROP_PATH_ITEM_REF))
                .map(ReferenceType.PATH_ITEM::referenceOf)
                .ifPresent(ref -> callback.addPathItem(
                        value(annotation, PROP_CALLBACK_URL_EXPRESSION),
                        OASFactory.createPathItem().ref(ref)));

        Optional.ofNullable(value(annotation, PROP_OPERATIONS))
                .map(AnnotationInstance[].class::cast)
                .map(pathItemIO()::readCallbackOperations)
                .ifPresent(pathItem -> callback.addPathItem(
                        value(annotation, PROP_CALLBACK_URL_EXPRESSION),
                        pathItem));

        callback.setExtensions(extensionIO().readExtensible(annotation));
        return callback;
    }
}
