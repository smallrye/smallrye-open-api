package io.smallrye.openapi.runtime.io.responses;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.models.responses.APIResponsesImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;

public class APIResponsesIO<V, A extends V, O extends V, AB, OB> extends ModelIO<APIResponses, V, A, O, AB, OB> {

    private static final String PROP_DEFAULT = "default";

    private final APIResponseIO<V, A, O, AB, OB> responseIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public APIResponsesIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO,
            ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.API_RESPONSES, Names.create(APIResponses.class));
        responseIO = new APIResponseIO<>(context, contentIO, extensionIO);
        this.extensionIO = extensionIO;
    }

    public Map<String, APIResponse> readSingle(AnnotationTarget target) {
        return Optional.ofNullable(responseIO.getAnnotation(target))
                .map(Collections::singleton)
                .map(annotations -> responseIO.readMap(annotations, responseIO::responseCode))
                .orElse(null);
    }

    public Map<String, APIResponse> readAll(AnnotationTarget target) {
        return responseIO.readMap(target, responseIO::responseCode);
    }

    @Override
    public APIResponses read(AnnotationInstance annotation) {
        AnnotationTarget target = annotation.target();

        return Optional.ofNullable(annotation)
                .map(AnnotationInstance::value)
                /*
                 * Begin - copy target to clones of nested annotations to support @Extension on
                 * method being applied to @APIReponse. Remove when no longer supporting TCK
                 * 3.1.1 and earlier.
                 */
                .map(AnnotationValue::asNestedArray)
                .map(annotations -> Arrays.stream(annotations)
                        .map(a -> AnnotationInstance.create(a.name(), target, a.values()))
                        .toArray(AnnotationInstance[]::new))
                // End
                .map(this::read)
                .map(responses -> responses.extensions(extensionIO.readExtensible(annotation)))
                .orElse(null);
    }

    @Override
    public APIResponses read(AnnotationValue annotation) {
        return Optional.ofNullable(annotation)
                .map(AnnotationValue::asNestedArray)
                .map(this::read)
                .orElse(null);
    }

    public Optional<Map<String, APIResponse>> readResponseSchema(AnnotationTarget target) {
        return Optional.ofNullable(scannerContext().annotations().getAnnotation(target, Names.API_RESPONSE_SCHEMA))
                .map(responseIO::readResponseSchema);
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     *
     * @param annotations {@literal @}APIResponse annotations
     * @return APIResponses model
     */
    public APIResponses read(AnnotationInstance[] annotations) {
        IoLogging.logger.annotationsListInto("@APIResponse", "APIResponses model");
        APIResponses responses = new APIResponsesImpl();

        for (AnnotationInstance nested : annotations) {
            responseIO.responseCode(nested)
                    .ifPresent(responseCode -> responses.addAPIResponse(responseCode, responseIO.read(nested)));
        }

        return responses;
    }

    @Override
    public APIResponses readObject(O node) {
        IoLogging.logger.jsonList("APIResponse");

        APIResponses model = new APIResponsesImpl();
        model.setDefaultValue(responseIO.readValue(jsonIO().getValue(node, PROP_DEFAULT)));
        model.setExtensions(extensionIO.readMap(node));

        jsonIO().properties(node)
                .stream()
                .filter(not(property -> PROP_DEFAULT.equals(property.getKey())))
                .forEach(property -> model.addAPIResponse(property.getKey(), responseIO.readValue(property.getValue())));

        return model;
    }

    public Optional<O> write(APIResponses model) {
        return optionalJsonObject(model).map(node -> {
            setAllIfPresent(node, extensionIO.write(model));
            setIfPresent(node, PROP_DEFAULT, responseIO.write(model.getDefaultValue()));
            setAllIfPresent(node, responseIO.write(model.getAPIResponses()));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
