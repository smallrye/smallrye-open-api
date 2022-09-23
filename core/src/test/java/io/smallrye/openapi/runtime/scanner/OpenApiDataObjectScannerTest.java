package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class OpenApiDataObjectScannerTest {

    OpenApiDataObjectScanner target;

    @BeforeEach
    void setUp() throws Exception {
    }

    @ParameterizedTest
    @CsvSource({
            "java.util.stream.DoubleStream, NUMBER,  double",
            "java.util.stream.IntStream   , INTEGER, int32",
            "java.util.stream.LongStream  , INTEGER, int64",
            "java.util.stream.Stream      ,        , ",
    })
    void testStreams(String type, Schema.SchemaType itemType, String itemFormat) throws IOException {
        IndexView index = Index.of(new Class<?>[0]);
        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                IndexScannerTestBase.emptyConfig());
        Schema out = OpenApiDataObjectScanner.process(context, Type.create(DotName.createSimple(type), Kind.CLASS));
        assertEquals(Schema.SchemaType.ARRAY, out.getType());
        assertEquals(itemType, out.getItems().getType());
        assertEquals(itemFormat, out.getItems().getFormat());
    }

}
