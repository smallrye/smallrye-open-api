package io.smallrye.openapi.runtime.io.responses;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class APIResponsesIO<V, A extends V, O extends V, AB, OB> extends ModelIO<APIResponses, V, A, O, AB, OB> {

    public APIResponsesIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.API_RESPONSES, Names.create(APIResponses.class));
    }

    public Map<String, APIResponse> readSingle(AnnotationTarget target) {
        return Optional.ofNullable(apiResponseIO().getAnnotation(target))
                .map(Collections::singleton)
                .map(annotations -> apiResponseIO().readMap(annotations, apiResponseIO()::responseCode))
                .orElse(null);
    }

    public Map<String, APIResponse> readAll(AnnotationTarget target) {
        return apiResponseIO().readMap(target, apiResponseIO()::responseCode);
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
                .map(responses -> responses.extensions(extensionIO().readExtensible(annotation)))
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
                .map(apiResponseIO()::readResponseSchema);
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     *
     * @param annotations {@literal @}APIResponse annotations
     * @return APIResponses model
     */
    public APIResponses read(AnnotationInstance[] annotations) {
        IoLogging.logger.annotationsListInto("@APIResponse", "APIResponses model");
        APIResponses responses = OASFactory.createAPIResponses();

        for (AnnotationInstance nested : annotations) {
            apiResponseIO().responseCode(nested)
                    .ifPresent(responseCode -> responses.addAPIResponse(responseCode, apiResponseIO().read(nested)));
        }

        return responses;
    }
}
