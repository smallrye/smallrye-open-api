package io.smallrye.openapi.runtime.io.responses;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.responses.APIResponsesImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class APIResponsesIO extends ModelIO<APIResponses> {

    private static final String PROP_DEFAULT = "default";

    private final APIResponseIO responseIO;
    private final ExtensionIO extensionIO;

    public APIResponsesIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.API_RESPONSES, Names.create(APIResponses.class));
        responseIO = new APIResponseIO(context, contentIO);
        extensionIO = new ExtensionIO(context);
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
        return Optional.ofNullable(annotation)
                .map(AnnotationInstance::value)
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
        return Optional.ofNullable(context.annotations().getAnnotation(target, Names.API_RESPONSE_SCHEMA))
                .map(responseIO::readResponseSchema);
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     *
     * @param context the scanning context
     * @param annotationValue {@literal @}APIResponse annotation
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
    public APIResponses read(ObjectNode node) {
        IoLogging.logger.jsonList("APIResponse");

        APIResponses model = new APIResponsesImpl();
        model.setDefaultValue(responseIO.read(node.get(PROP_DEFAULT)));
        extensionIO.readMap(node).forEach(model::addExtension);

        node.properties()
                .stream()
                .filter(not(property -> PROP_DEFAULT.equals(property.getKey())))
                .forEach(property -> model.addAPIResponse(property.getKey(), responseIO.read(property.getValue())));

        return model;
    }

    public Optional<ObjectNode> write(APIResponses model) {
        return optionalJsonObject(model)
                .map(node -> {
                    setAllIfPresent(node, extensionIO.write(model));
                    setIfPresent(node, PROP_DEFAULT, responseIO.write(model.getDefaultValue()));
                    setAllIfPresent(node, responseIO.write(model.getAPIResponses()));
                    return node;
                });
    }
}
