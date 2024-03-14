package io.smallrye.openapi.runtime.io.media;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class EncodingIOTest extends IndexScannerTestBase {

    @ParameterizedTest
    @CsvSource({
            "io.smallrye.openapi.runtime.io.media.EncodingIOTest$Endpoint1, PIPE_DELIMITED",
            "io.smallrye.openapi.runtime.io.media.EncodingIOTest$Endpoint2, "
    })
    void testReadEncodingStyle(Class<?> endpointClass, Encoding.Style expectedStyle) {
        FilteredIndexView index = new FilteredIndexView(IndexScannerTestBase.indexOf(endpointClass), emptyConfig());
        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                Collections.emptyList(),
                emptyConfig(), new OpenAPIImpl());
        ClassInfo clazz = index.getClassByName(DotName.createSimple(endpointClass.getName()));
        AnnotationInstance annotation = clazz.method("getData")
                .annotation(DotName.createSimple(APIResponse.class.getName()))
                .value("content")
                .asNestedArray()[0]
                .value("encoding")
                .asNestedArray()[0];

        IOContext<?, ?, ?, ?, ?> ioContext = IOContext.forScanning(context);
        ExtensionIO<?, ?, ?, ?, ?> extensionIO = new ExtensionIO<>(ioContext);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ContentIO<?, ?, ?, ?, ?> contentIO = new ContentIO(ioContext, extensionIO);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        EncodingIO<?, ?, ?, ?, ?> encodingIO = new EncodingIO(ioContext, contentIO, extensionIO);
        Encoding.Style style = encodingIO.readStyle(annotation);
        assertEquals(expectedStyle, style);
    }

    static class Endpoint1 {
        @APIResponse(content = @Content(encoding = @org.eclipse.microprofile.openapi.annotations.media.Encoding(style = "pipeDelimited")))
        public String getData() {
            return null;
        }
    }

    static class Endpoint2 {
        @APIResponse(content = @Content(encoding = @org.eclipse.microprofile.openapi.annotations.media.Encoding(style = "invalid")))
        public String getData() {
            return null;
        }
    }

}
