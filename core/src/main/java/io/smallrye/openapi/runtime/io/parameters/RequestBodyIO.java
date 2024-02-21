package io.smallrye.openapi.runtime.io.parameters;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class RequestBodyIO extends MapModelIO<RequestBody> implements ReferenceIO {

    private static final String PROP_REQUIRED = "required";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_VALUE = "value";
    private static final String PROP_REF = "$ref";

    private final ContentIO contentIO;
    private final ExtensionIO extensionIO;

    public RequestBodyIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.REQUEST_BODY, Names.create(RequestBody.class));
        this.contentIO = contentIO;
        extensionIO = new ExtensionIO(context);
    }

    Stream<AnnotationInstance> getAnnotations(MethodInfo method, DotName annotation) {
        Stream<AnnotationInstance> methodAnnos = Stream.of(context.annotations().getAnnotation(method, annotation));
        Stream<AnnotationInstance> paramAnnos = method.parameterTypes()
                .stream()
                .map(p -> context.annotations().getMethodParameterAnnotation(method, p, annotation));

        return Stream.concat(methodAnnos, paramAnnos)
                .filter(Objects::nonNull);
    }

    @Override
    public List<AnnotationInstance> getRepeatableAnnotations(AnnotationTarget target) {
        if (target.kind() == Kind.METHOD) {
            return getAnnotations(target.asMethod(), Names.REQUEST_BODY).collect(Collectors.toList());
        }
        return Collections.singletonList(context.annotations().getAnnotation(target, Names.REQUEST_BODY));
    }

    @Override
    public RequestBody read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@RequestBody");
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(value(annotation, PROP_DESCRIPTION));
        requestBody.setContent(contentIO.read(annotation.value(PROP_CONTENT), ContentDirection.INPUT));
        requestBody.setRequired(value(annotation, PROP_REQUIRED));
        requestBody.setRef(ReferenceType.REQUEST_BODY.refValue(annotation));
        requestBody.setExtensions(extensionIO.readExtensible(annotation));
        return requestBody;
    }

    public RequestBody readRequestSchema(MethodInfo target) {
        if (context.getCurrentConsumes() == null) {
            // Only generate the RequestBody if the endpoint declares an @Consumes media type
            return null;
        }

        return getAnnotations(target, Names.REQUEST_BODY_SCHEMA)
                .map(this::readRequestSchema)
                .findFirst()
                .orElse(null);
    }

    private RequestBody readRequestSchema(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@RequestBodySchema");
        Content content = new ContentImpl();

        for (String mediaType : context.getCurrentConsumes()) {
            MediaType type = new MediaTypeImpl();
            type.setSchema(SchemaFactory.typeToSchema(context,
                    value(annotation, PROP_VALUE),
                    null,
                    context.getExtensions()));
            content.addMediaType(mediaType, type);
        }

        return new RequestBodyImpl().content(content);
    }

    @Override
    public RequestBody read(ObjectNode node) {
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        requestBody.setContent(contentIO.read(node.get(PROP_CONTENT)));
        requestBody.setRequired(JsonUtil.booleanProperty(node, PROP_REQUIRED).orElse(null));
        requestBody.setRef(JsonUtil.stringProperty(node, PROP_REF));
        requestBody.setExtensions(extensionIO.readMap(node));
        return requestBody;
    }


    public Optional<ObjectNode> write(RequestBody model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, PROP_REF, model.getRef());
            } else {
                JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                setIfPresent(node, PROP_CONTENT, contentIO.write(model.getContent()));
                JsonUtil.booleanProperty(node, PROP_REQUIRED, model.getRequired());
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        });
    }
}
