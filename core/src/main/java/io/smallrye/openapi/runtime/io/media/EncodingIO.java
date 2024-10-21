package io.smallrye.openapi.runtime.io.media;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.Encoding.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class EncodingIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Encoding, V, A, O, AB, OB> {

    private static final String PROP_ALLOW_RESERVED = "allowReserved";
    private static final String PROP_CONTENT_TYPE = "contentType";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_STYLE = "style";

    public EncodingIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.ENCODING, Names.create(Encoding.class));
    }

    @Override
    public Encoding read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Encoding");
        Encoding encoding = OASFactory.createEncoding();
        encoding.setContentType(value(annotation, PROP_CONTENT_TYPE));
        encoding.setStyle(readStyle(annotation));
        encoding.setExplode(value(annotation, PROP_EXPLODE));
        encoding.setAllowReserved(value(annotation, PROP_ALLOW_RESERVED));
        encoding.setHeaders(headerIO().readMap(annotation.value(PROP_HEADERS)));
        encoding.setExtensions(extensionIO().readExtensible(annotation));
        return encoding;
    }

    Style readStyle(AnnotationInstance annotation) {
        return Optional.ofNullable(annotation)
                .map(a -> a.value(PROP_STYLE))
                .map(AnnotationValue::asString)
                .flatMap(this::readStyle)
                .orElse(null);
    }

    private Optional<Style> readStyle(String style) {
        return Arrays.stream(Style.values())
                .filter(value -> style.equals(value.toString()))
                .findFirst();
    }
}
