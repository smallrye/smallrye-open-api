package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
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
        assertJsonEquals("components.schemas.schemaproperty-blankname.json", BlankNameTest.class);

    }

    // Blank name is treated as valid
    @Schema(properties = { @SchemaProperty(name = "", type = SchemaType.STRING) })
    static class BlankNameTest {
    }

    @Test
    void testClassSchemaPropertyDuplicateName() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-duplicatename.json", DuplicateNameTest.class);
    }

    // Last instance replaces others, instances are not merged
    @Schema(properties = { @SchemaProperty(name = "foo", type = SchemaType.STRING, defaultValue = "5"),
            @SchemaProperty(name = "foo", type = SchemaType.INTEGER) })
    static class DuplicateNameTest {
    }

    @Test
    void testClassSchemaPropertyNegativeMultipleOf() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-negativemultipleof.json", NegativeMultipleOf.class);
    }

    @Schema(properties = { @SchemaProperty(name = "test", type = SchemaType.INTEGER, multipleOf = -2) })
    static class NegativeMultipleOf {
    }

    @Test
    void testClassSchemaPropertyMaximumNotNumber() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-maximumnotnumber.json", MaximumNotNumber.class);
    }

    // maximum should be ignored
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.INTEGER, maximum = "foo"))
    static class MaximumNotNumber {
    }

    @Test
    void testClassSchemaPropertyMinimumNotNumber() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-minimumnotnumber.json", MinimumNotNumber.class);
    }

    // minimum should be ignored
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.INTEGER, minimum = "foo"))
    static class MinimumNotNumber {
    }

    @Test
    void testClassSchemaPropertyMinLengthNegative() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-minlengthnegative.json", MinLengthNegative.class);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, minLength = -2))
    static class MinLengthNegative {
    }

    @Test
    void testClassSchemaPropertyMaxLengthNegative() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-maxlengthnegative.json", MaxLengthNegative.class);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, maxLength = -2))
    static class MaxLengthNegative {
    }

    @Test
    void testClassSchemaPropertyPatternInvalid() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-patterninvalid.json", PatternInvalid.class);
    }

    // Invalid pattern used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, pattern = "(invalid"))
    static class PatternInvalid {
    }

    @Test
    void testClassSchemaPropertyMaxPropertiesNegative() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-maxpropertiesnegative.json", MaxPropertiesNegative.class);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.OBJECT, maxProperties = -2))
    static class MaxPropertiesNegative {
    }

    @Test
    void testClassSchemaPropertyMinPropertiesNegative() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-minpropertiesnegative.json", MinPropertiesNegative.class);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.OBJECT, minProperties = -2))
    static class MinPropertiesNegative {
    }

    @Test
    void testClassSchemaPropertyRefWithOtherProps() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-refwithotherprops.json", RefWithOtherProps.class);
    }

    // Ref is no longer treated as special, all properties are emitted
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.STRING, ref = "foobar"))
    static class RefWithOtherProps {
    }

    @Test
    void testClassSchemaPropertyDefaultValueWrongType() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-defaultvaluewrongtype.json", DefaultValueWrongType.class);
    }

    // Invalid default value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.INTEGER, defaultValue = "foo"))
    static class DefaultValueWrongType {
    }

    @Test
    void testClassSchemaPropertyMaxItemsNegative() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-maxitemsnegative.json", MaxItemsNegative.class);
    }

    // Negative value used in document
    @Schema(properties = @SchemaProperty(name = "test", type = SchemaType.ARRAY, maxItems = -2))
    static class MaxItemsNegative {
    }

    @Test
    void testClassSchemaPropertyMinItemsNegative() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-minitemsnegative.json", MinItemsNegative.class);
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
        assertJsonEquals("components.schemas.schemaproperty-implementationmissing.json", ImplementationMissing.class);
    }

    // Implementation attribute not included in document
    @Schema(properties = @SchemaProperty(name = "test", implementation = MissingClass.class))
    static class ImplementationMissing {
    }

    @Test
    void testClassSchemaPropertyNotMissing() throws Exception {
        assertJsonEquals("components.schemas.schemaproperty-notmissing.json", NotMissing.class);

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
        assertJsonEquals("components.schemas.schemaproperty-oneofmissing.json", OneOfMissing.class);

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
        assertJsonEquals("components.schemas.schemaproperty-anyofmissing.json", AnyOfMissing.class);

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
        assertJsonEquals("components.schemas.schemaproperty-allofmissing.json", AllOfMissing.class);

        String expectedMessage = String.format("Could not find schema class in index: %s", MissingClass.class.getName());
        LogRecord record = logs.assertLogContaining(expectedMessage);
        assertEquals(Level.WARNING, record.getLevel());
    }

    @Schema(properties = @SchemaProperty(name = "test", allOf = MissingClass.class))
    static class AllOfMissing {
    }
}
