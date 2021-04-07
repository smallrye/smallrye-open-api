package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

class SchemaPropertyNegativeTest extends IndexScannerTestBase {

    @RegisterExtension
    public LogCapture logs = new LogCapture(ScannerLogging.class.getPackage().getName());

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        System.out.println(testInfo.getDisplayName());
    }

    @Test
    void testClassSchemaPropertyBlankName() throws Exception {
        Index index = indexOf(BlankNameTest.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-blankname.json", result);

    }

    // Blank name is treated as valid
    @Schema(properties = { @SchemaProperty(name = "", type = SchemaType.STRING) })
    static class BlankNameTest {
    }

    @Test
    void testClassSchemaPropertyDuplicateName() throws Exception {
        Index index = indexOf(DuplicateNameTest.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-duplicatename.json", result);
    }

    // Last instance replaces others, instances are not merged
    @Schema(properties = { @SchemaProperty(name = "foo", type = SchemaType.STRING, defaultValue = "5"),
            @SchemaProperty(name = "foo", type = SchemaType.INTEGER) })
    static class DuplicateNameTest {
    }

    @Test
    void testClassSchemaPropertyNegativeMultipleOf() throws Exception {
        Index index = indexOf(NegativeMultipleOf.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-negativemultipleof.json", result);
    }

    @Schema(properties = { @SchemaProperty(name = "test", type = SchemaType.INTEGER, multipleOf = -2) })
    static class NegativeMultipleOf {
    }

    @Test
    void testClassSchemaPropertyMaximumNotNumber() throws Exception {
        Index index = indexOf(MaximumNotNumber.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-maximumnotnumber.json", result);
    }

    // maximum should be ignored
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.INTEGER, maximum = "foo"))
    static class MaximumNotNumber {
    }

    @Test
    void testClassSchemaPropertyMinimumNotNumber() throws Exception {
        Index index = indexOf(MinimumNotNumber.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-minimumnotnumber.json", result);
    }

    // minimum should be ignored
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.INTEGER, minimum = "foo"))
    static class MinimumNotNumber {
    }

    @Test
    void testClassSchemaPropertyMinLengthNegative() throws Exception {
        Index index = indexOf(MinLengthNegative.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-minlengthnegative.json", result);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, minLength = -2))
    static class MinLengthNegative {
    }

    @Test
    void testClassSchemaPropertyMaxLengthNegative() throws Exception {
        Index index = indexOf(MaxLengthNegative.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-maxlengthnegative.json", result);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, maxLength = -2))
    static class MaxLengthNegative {
    }

    @Test
    void testClassSchemaPropertyPatternInvalid() throws Exception {
        Index index = indexOf(PatternInvalid.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-patterninvalid.json", result);
    }

    // Invalid pattern used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, pattern = "(invalid"))
    static class PatternInvalid {
    }

    @Test
    void testClassSchemaPropertyMaxPropertiesNegative() throws Exception {
        Index index = indexOf(MaxPropertiesNegative.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-maxpropertiesnegative.json", result);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.OBJECT, maxProperties = -2))
    static class MaxPropertiesNegative {
    }

    @Test
    void testClassSchemaPropertyMinPropertiesNegative() throws Exception {
        Index index = indexOf(MinPropertiesNegative.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-minpropertiesnegative.json", result);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.OBJECT, minProperties = -2))
    static class MinPropertiesNegative {
    }

    @Test
    void testClassSchemaPropertyRefWithOtherProps() throws Exception {
        Index index = indexOf(RefWithOtherProps.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-refwithotherprops.json", result);
    }

    // Name and ref used in document, other attributes ignored
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, ref = "foobar"))
    static class RefWithOtherProps {
    }

    @Test
    void testClassSchemaPropertyDefaultValueWrongType() throws Exception {
        Index index = indexOf(DefaultValueWrongType.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-defaultvaluewrongtype.json", result);
    }

    // Invalid default value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.INTEGER, defaultValue = "foo"))
    static class DefaultValueWrongType {
    }

    @Test
    void testClassSchemaPropertyMaxItemsNegative() throws Exception {
        Index index = indexOf(MaxItemsNegative.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-maxitemsnegative.json", result);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.ARRAY, maxItems = -2))
    static class MaxItemsNegative {
    }

    @Test
    void testClassSchemaPropertyMinItemsNegative() throws Exception {
        Index index = indexOf(MinItemsNegative.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-minitemsnegative.json", result);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.ARRAY, minItems = -2))
    static class MinItemsNegative {
    }

    static class MissingClass {
        public String example;
    }

    @Test
    void testClassSchemaPropertyImplementationMissing() throws Exception {
        Index index = indexOf(ImplementationMissing.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-implementationmissing.json", result);
    }

    // Implementation attribute not included in document
    @Schema(properties = @SchemaProperty(name = "test", implementation = MissingClass.class))
    static class ImplementationMissing {
    }

    @Test
    void testClassSchemaPropertyNotMissing() throws Exception {
        Index index = indexOf(NotMissing.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-notmissing.json", result);

        String expectedMessage = String.format("Could not find schema class in index: %s", MissingClass.class.getName());
        LogRecord record = logs.assertLogContaining(expectedMessage);
        assertEquals(Level.WARNING, record.getLevel());
    }

    // Not attribute present, pointing to an empty schema
    @Schema(properties = @SchemaProperty(name = "test", not = MissingClass.class))
    static class NotMissing {
    }

    @Test
    void testClassSchemaPropertyOneOfMissing() throws Exception {
        Index index = indexOf(OneOfMissing.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-oneofmissing.json", result);

        String expectedMessage = String.format("Could not find schema class in index: %s", MissingClass.class.getName());
        LogRecord record = logs.assertLogContaining(expectedMessage);
        assertEquals(Level.WARNING, record.getLevel());
    }

    // OneOf attribute present, pointing to an empty schema
    @Schema(properties = @SchemaProperty(name = "test", oneOf = { MissingClass.class }))
    static class OneOfMissing {
    }

    @Test
    void testClassSchemaPropertyAnyOfMissing() throws Exception {
        Index index = indexOf(AnyOfMissing.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-anyofmissing.json", result);

        String expectedMessage = String.format("Could not find schema class in index: %s", MissingClass.class.getName());
        LogRecord record = logs.assertLogContaining(expectedMessage);
        assertEquals(Level.WARNING, record.getLevel());
    }

    // AnyOf attribute present, pointing to an empty schema
    @Schema(properties = @SchemaProperty(name = "test", anyOf = MissingClass.class))
    static class AnyOfMissing {
    }

    @Test
    void testClassSchemaPropertyAllOfMissing() throws Exception {
        Index index = indexOf(AllOfMissing.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.schemaproperty-allofmissing.json", result);

        String expectedMessage = String.format("Could not find schema class in index: %s", MissingClass.class.getName());
        LogRecord record = logs.assertLogContaining(expectedMessage);
        assertEquals(Level.WARNING, record.getLevel());
    }

    @Schema(properties = @SchemaProperty(name = "test", allOf = MissingClass.class))
    static class AllOfMissing {
    }
}
