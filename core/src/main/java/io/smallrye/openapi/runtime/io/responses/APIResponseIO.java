package io.smallrye.openapi.runtime.io.responses;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

public class APIResponseIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<APIResponse, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_RESPONSE_CODE = "responseCode";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_LINKS = "links";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_RESPONSE_DESCRIPTION = "responseDescription";
    private static final String PROP_VALUE = "value";

    public APIResponseIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.API_RESPONSE, DotName.createSimple(APIResponse.class));
    }

    @Override
    public APIResponse read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@APIResponse");
        APIResponse response = OASFactory.createAPIResponse();
        response.setDescription(value(annotation, PROP_DESCRIPTION));
        response.setHeaders(headerIO().readMap(annotation.value(PROP_HEADERS)));
        response.setLinks(linkIO().readMap(annotation.value(PROP_LINKS)));
        response.setContent(contentIO().read(annotation.value(PROP_CONTENT), ContentIO.Direction.OUTPUT));
        response.setExtensions(extensionIO().readExtensible(annotation));
        response.setRef(ReferenceType.RESPONSE.refValue(annotation));
        Extensions.setResponseCode(response, responseCode(annotation).orElse(null));
        return response;
    }

    Map<String, APIResponse> readResponseSchema(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@APIResponseSchema");

        String responseCode = value(annotation, PROP_RESPONSE_CODE);
        APIResponse response = OASFactory.createAPIResponse();
        response.setDescription(value(annotation, PROP_RESPONSE_DESCRIPTION));
        Extensions.setResponseCode(response, responseCode);

        Optional.ofNullable(scannerContext().getCurrentProduces()).ifPresent(mediaTypes -> {
            Type responseType = value(annotation, PROP_VALUE);

            if (!TypeUtil.isVoid(responseType)) {
                // Only generate the content if the endpoint declares an @Produces media type
                Content content = OASFactory.createContent();
                Schema responseSchema = SchemaFactory.typeToSchema(scannerContext(),
                        responseType,
                        null,
                        scannerContext().getExtensions());

                for (String mediaType : mediaTypes) {
                    content.addMediaType(mediaType, OASFactory.createMediaType().schema(responseSchema));
                }

                response.setContent(content);
            }
        });

        return Collections.singletonMap(responseCode, response);
    }

    public Optional<String> responseCode(AnnotationInstance annotation) {
        String responseCode = value(annotation, PROP_RESPONSE_CODE);
        String ref = ReferenceType.RESPONSE.refValue(annotation);

        if (responseCode != null) {
            return Optional.of(responseCode);
        } else if (ref != null) {
            return Optional.ofNullable(ModelUtil.getComponent(scannerContext().getOpenApi(), ref))
                    .filter(APIResponse.class::isInstance)
                    .map(APIResponse.class::cast)
                    .map(Extensions::getResponseCode);
        } else {
            return Optional.of(APIResponses.DEFAULT);
        }
    }
}
