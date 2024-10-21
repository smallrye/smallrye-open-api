package io.smallrye.openapi.runtime.io.parameters;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;

public class RequestBodyIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<RequestBody, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_REQUIRED = "required";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_VALUE = "value";

    public RequestBodyIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.REQUEST_BODY, Names.create(RequestBody.class));
    }

    Stream<AnnotationInstance> getAnnotations(MethodInfo method, DotName annotation) {
        Stream<AnnotationInstance> methodAnnos = Stream.of(scannerContext().annotations().getAnnotation(method, annotation));
        Stream<AnnotationInstance> paramAnnos = IntStream.range(0, method.parametersCount())
                .mapToObj(p -> scannerContext().annotations().getMethodParameterAnnotation(method, p, annotation));

        return Stream.concat(methodAnnos, paramAnnos)
                .filter(Objects::nonNull);
    }

    @Override
    public List<AnnotationInstance> getRepeatableAnnotations(AnnotationTarget target) {
        if (target.kind() == Kind.METHOD) {
            return getAnnotations(target.asMethod(), Names.REQUEST_BODY).collect(Collectors.toList());
        }
        return Collections.singletonList(scannerContext().annotations().getAnnotation(target, Names.REQUEST_BODY));
    }

    @Override
    public RequestBody read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@RequestBody");
        RequestBody requestBody = OASFactory.createRequestBody();
        requestBody.setDescription(value(annotation, PROP_DESCRIPTION));
        requestBody.setContent(contentIO().read(annotation.value(PROP_CONTENT), ContentIO.Direction.INPUT));
        requestBody.setRef(ReferenceType.REQUEST_BODY.refValue(annotation));
        requestBody.setExtensions(extensionIO().readExtensible(annotation));
        Boolean required = value(annotation, PROP_REQUIRED);
        if (required != null) {
            requestBody.setRequired(required);
        } else {
            Extensions.setRequiredDefault(requestBody, Boolean.TRUE);
        }
        return requestBody;
    }

    public RequestBody readRequestSchema(MethodInfo target) {
        if (scannerContext().getCurrentConsumes() == null) {
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
        Content content = OASFactory.createContent();

        for (String mediaType : scannerContext().getCurrentConsumes()) {
            MediaType type = OASFactory.createMediaType();
            type.setSchema(SchemaFactory.typeToSchema(scannerContext(),
                    value(annotation, PROP_VALUE),
                    null,
                    scannerContext().getExtensions()));
            content.addMediaType(mediaType, type);
        }

        return OASFactory.createRequestBody().content(content);
    }
}
