package io.smallrye.openapi.runtime.io;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.PathItem.HttpMethod;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.model.ReferenceType;

public class PathItemIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<PathItem, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_PARAMETERS = "parameters";
    private static final String PROP_SERVERS = "servers";
    private static final String PROP_SUMMARY = "summary";

    // Annotation properties
    private static final String PROP_OPERATIONS = "operations";

    public PathItemIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.PATH_ITEM, Names.create(PathItem.class));
    }

    @Override
    public PathItem read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@PathItem");
        PathItem pathItem = OASFactory.createPathItem();

        pathItem.setRef(ReferenceType.PATH_ITEM.refValue(annotation));
        pathItem.setDescription(value(annotation, PROP_DESCRIPTION));
        pathItem.setSummary(value(annotation, PROP_SUMMARY));
        pathItem.setServers(serverIO().readList(annotation.value(PROP_SERVERS)));
        pathItem.setParameters(parameterIO().readList(annotation.value(PROP_PARAMETERS)));

        Optional.ofNullable(annotation.value(PROP_OPERATIONS))
                .map(AnnotationValue::asNestedArray)
                .ifPresent(annotations -> readOperationsInto(pathItem, annotations, pathItemOperationIO()));

        pathItem.setExtensions(extensionIO().readExtensible(annotation));
        return pathItem;
    }

    /**
     * Convert an array of {@link CallbackOperation} annotations into a {@code PathItem}.
     *
     * @param annotations the {@code CallbackOperation} annotation instances
     * @return the path item
     */
    public PathItem readCallbackOperations(AnnotationInstance[] annotations) {
        PathItem pathItem = OASFactory.createPathItem();

        readOperationsInto(pathItem, annotations, callbackOperationIO());

        return pathItem;
    }

    private void readOperationsInto(PathItem pathItem, AnnotationInstance[] annotations,
            OperationIO<V, A, O, AB, OB> operationIO) {
        Arrays.stream(annotations)
                .filter(annotation -> Objects.nonNull(value(annotation, "method")))
                .forEach(annotation -> {
                    String method = value(annotation, "method");
                    Operation operation = operationIO.read(annotation);
                    operation.setExtensions(extensionIO().readExtensible(annotation));
                    pathItem.setOperation(HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)), operation);
                });
    }
}
