package io.smallrye.openapi.runtime.scanner;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        assertEquals(singletonList(Schema.SchemaType.ARRAY), out.getType());
        List<Schema.SchemaType> expectedTypes = itemType == null ? null : singletonList(itemType);
        assertEquals(expectedTypes, out.getItems().getType());
        assertEquals(itemFormat, out.getItems().getFormat());
    }

    @Test
    void testNoSelfReferencingRegardlessOfScanOrder() throws IOException {
        // one way to trigger issue 1565 (self-referencing) was to have a
        // non-SchemaType.OBJECT type as a field in another type
        @org.eclipse.microprofile.openapi.annotations.media.Schema(description = "Nested class", type = SchemaType.STRING)
        class N {
        }

        @org.eclipse.microprofile.openapi.annotations.media.Schema(description = "Containing class")
        class C {
            N n;
        }

        IndexView index = Index.of(C.class, N.class);
        AnnotationScannerContext contextCFirst = new AnnotationScannerContext(index,
                Thread.currentThread().getContextClassLoader(),
                IndexScannerTestBase.emptyConfig());
        AnnotationScannerContext contextNFirst = new AnnotationScannerContext(index,
                Thread.currentThread().getContextClassLoader(),
                IndexScannerTestBase.emptyConfig());
        Type cType = Type.create(DotName.createSimple(C.class), Kind.CLASS);
        Type nType = Type.create(DotName.createSimple(N.class), Kind.CLASS);

        // io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner.processClassSchemas()
        // calls OpenApiDataObjectScanner.process() for all annotated classes in 'index',
        // and the order of the calls might not be deterministic.
        // Try both orders for the two example classes and expect the same result
        OpenApiDataObjectScanner.process(contextCFirst, cType);
        OpenApiDataObjectScanner.process(contextCFirst, nType);

        OpenApiDataObjectScanner.process(contextNFirst, nType);
        OpenApiDataObjectScanner.process(contextNFirst, cType);

        // the contextCFirst case had "#/components/schemas/1N" from getRef() in smallrye-open-api 3.5.2
        assertAll(
                () -> assertNull(contextCFirst.getOpenApi().getComponents().getSchemas().get("1N").getRef(), "C first"),
                () -> assertNull(contextNFirst.getOpenApi().getComponents().getSchemas().get("1N").getRef(), "N first"));
    }
}
