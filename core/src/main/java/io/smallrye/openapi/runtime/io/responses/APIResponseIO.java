package io.smallrye.openapi.runtime.io.responses;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.headers.HeaderIO;
import io.smallrye.openapi.runtime.io.links.LinkIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

public class APIResponseIO extends MapModelIO<APIResponse> implements ReferenceIO {

    private static final String PROP_RESPONSE_CODE = "responseCode";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_LINKS = "links";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_RESPONSE_DESCRIPTION = "responseDescription";
    private static final String PROP_VALUE = "value";

    private final LinkIO linkIO;
    private final HeaderIO headerIO;
    private final ContentIO contentIO;
    private final ExtensionIO extensionIO;

    public APIResponseIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.API_RESPONSE, DotName.createSimple(APIResponse.class));
        linkIO = new LinkIO(context);
        this.contentIO = contentIO;
        headerIO = new HeaderIO(context, contentIO);
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public APIResponse read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@APIResponse");
        APIResponseImpl response = new APIResponseImpl();
        response.setDescription(value(annotation, PROP_DESCRIPTION));
        response.setHeaders(headerIO.readMap(annotation.value(PROP_HEADERS)));
        response.setLinks(linkIO.readMap(annotation.value(PROP_LINKS)));
        response.setContent(contentIO.read(annotation.value(PROP_CONTENT), ContentDirection.OUTPUT));
        response.setExtensions(extensionIO.readExtensible(annotation));
        response.setRef(ReferenceType.RESPONSE.refValue(annotation));
        response.setResponseCode(responseCode(annotation).orElse(null));

        return response;
    }

    Map<String, APIResponse> readResponseSchema(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@APIResponseSchema");

        String responseCode = value(annotation, PROP_RESPONSE_CODE);
        APIResponseImpl response = new APIResponseImpl();
        response.setDescription(value(annotation, PROP_RESPONSE_DESCRIPTION));
        response.setResponseCode(responseCode);

        Optional.ofNullable(context.getCurrentProduces()).ifPresent(mediaTypes -> {
            Type responseType = value(annotation, PROP_VALUE);

            if (!TypeUtil.isVoid(responseType)) {
                // Only generate the content if the endpoint declares an @Produces media type
                Content content = new ContentImpl();
                Schema responseSchema = SchemaFactory.typeToSchema(context,
                        responseType,
                        null,
                        context.getExtensions());

                for (String mediaType : mediaTypes) {
                    content.addMediaType(mediaType, new MediaTypeImpl().schema(responseSchema));
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
            return Optional.ofNullable(ModelUtil.getComponent(context.getOpenApi(), ref))
                    .filter(APIResponseImpl.class::isInstance)
                    .map(APIResponseImpl.class::cast)
                    .map(APIResponseImpl::getResponseCode);
        } else {
            return Optional.of(APIResponses.DEFAULT);
        }
    }

    @Override
    public APIResponse read(ObjectNode node) {
        IoLogging.logger.singleJsonObject("Response");
        APIResponse model = new APIResponseImpl();
        model.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        model.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        model.setHeaders(headerIO.readMap(node.get(PROP_HEADERS)));
        model.setContent(contentIO.read(node.get(PROP_CONTENT)));
        model.setLinks(linkIO.readMap(node.get(PROP_LINKS)));
        extensionIO.readMap(node).forEach(model::addExtension);
        return model;
    }

    public Optional<ObjectNode> write(APIResponse model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                setIfPresent(node, PROP_HEADERS, headerIO.write(model.getHeaders()));
                setIfPresent(node, PROP_CONTENT, contentIO.write(model.getContent()));
                setIfPresent(node, PROP_LINKS, linkIO.write(model.getLinks()));
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        });
    }
}
