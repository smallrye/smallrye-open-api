package io.smallrye.openapi.runtime.io;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Operation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.util.ModelUtil;

public class PathItemOperationIO<V, A extends V, O extends V, AB, OB> extends OperationIO<V, A, O, AB, OB> {

    public PathItemOperationIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.PATH_ITEM_OPERATION);
    }

    @Override
    public Operation read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@PathItemOperation");
        Operation operation = OASFactory.createOperation();

        operation.setTags(processTags(annotation.value(PROP_TAGS)));
        operation.setSummary(value(annotation, PROP_SUMMARY));
        operation.setDescription(value(annotation, PROP_DESCRIPTION));
        operation.setExternalDocs(extDocIO().read(annotation.value(PROP_EXTERNAL_DOCS)));
        operation.setOperationId(value(annotation, PROP_OPERATION_ID));
        operation.setParameters(parameterIO().readList(annotation.value(PROP_PARAMETERS)));
        operation.setRequestBody(requestBodyIO().read(annotation.value(PROP_REQUEST_BODY)));
        operation.setResponses(apiResponsesIO().read(annotation.value(PROP_RESPONSES)));
        operation.setCallbacks(callbackIO().readMap(annotation.value(PROP_CALLBACKS)));
        operation.setDeprecated(value(annotation, PROP_DEPRECATED));
        operation.setSecurity(securityIO().readRequirements(
                annotation.value(PROP_SECURITY),
                annotation.value(PROP_SECURITY_SETS)));
        operation.setServers(serverIO().readList(annotation.value(PROP_SERVERS)));
        operation.setExtensions(extensionIO().readExtensible(annotation));

        return operation;
    }

    private List<String> processTags(AnnotationValue tagAnnotations) {
        return Optional.ofNullable(tagAnnotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::processTags)
                .orElse(null);
    }

    /**
     * Read an array of {@code Tag} annotations, collecting the names and adding any new definitions to the top-level OpenAPI
     * object.
     *
     * @param tagAnnotations the annotations
     * @return the list of tag names
     */
    private List<String> processTags(AnnotationInstance[] tagAnnotations) {
        Set<String> tagNames = new LinkedHashSet<>();

        tagIO().readList(tagAnnotations)
                .stream()
                .filter(tag -> Objects.nonNull(tag.getName()))
                .forEach(tag -> {
                    tagNames.add(tag.getName());
                    ModelUtil.addTag(scannerContext().getOpenApi(), tag);
                });

        tagIO().readReferences(tagAnnotations).forEach(tagNames::add);

        return new ArrayList<>(tagNames);
    }
}
