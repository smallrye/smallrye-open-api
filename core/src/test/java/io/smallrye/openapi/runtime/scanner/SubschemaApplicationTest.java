package io.smallrye.openapi.runtime.scanner;

import static org.eclipse.microprofile.openapi.annotations.enums.SchemaType.OBJECT;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.DependentRequired;
import org.eclipse.microprofile.openapi.annotations.media.DependentSchema;
import org.eclipse.microprofile.openapi.annotations.media.PatternProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

public class SubschemaApplicationTest extends IndexScannerTestBase {

    @Test
    public void testSubschemaApplication() throws Exception {
        Index index = indexOf(A.class, B.class, C.class, TestOneOf.class, TestAnyOf.class, TestAllOf.class, TestNot.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-application.json", result);
    }

    @Test
    public void testSubschemaApplicationProperty() throws Exception {
        Index index = indexOf(A.class, B.class, C.class, TestOneOfProperty.class, TestAnyOfProperty.class,
                TestAllOfProperty.class, TestNotProperty.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-application-property.json", result);
    }

    @Test
    public void testSubschemaIfThenElse() throws Exception {
        Index index = indexOf(A.class, B.class, C.class, TestIfThenElse.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-ifthenelse.json", result);
    }

    @Test
    public void testSubschemaIfThenElseProperty() throws Exception {
        Index index = indexOf(A.class, B.class, C.class, TestIfThenElseProperty.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-ifthenelse-property.json", result);
    }

    @Test
    public void testDependentSchemas() throws Exception {
        Index index = indexOf(A.class, B.class, TestDepedentSchemas.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-dependent-schemas.json", result);
    }

    @Test
    public void testDependentSchemasProperty() throws Exception {
        Index index = indexOf(A.class, B.class, TestDepedentSchemasProperty.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-dependent-schemas-property.json", result);
    }

    @Test
    public void testDependentRequired() throws Exception {
        Index index = indexOf(TestDependentRequired.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-dependent-required.json", result);
    }

    @Test
    public void testDependentRequiredProperty() throws Exception {
        Index index = indexOf(TestDependentRequiredProperty.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-dependent-required-property.json", result);
    }

    @Test
    public void testSubschemaCollections() throws Exception {
        Index index = indexOf(A.class, JavaTypeString.class, TestCollections.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-collections.json", result);
    }

    @Test
    public void testSubschemaCollectionsProperty() throws Exception {
        Index index = indexOf(A.class, JavaTypeString.class, TestCollectionsProperty.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.subschema-collections-property.json", result);
    }

    @Schema(oneOf = { A.class, B.class, C.class })
    public static class TestOneOf {
    }

    @Schema(anyOf = { A.class, B.class, C.class })
    public static class TestAnyOf {
    }

    @Schema(allOf = { A.class, B.class, C.class })
    public static class TestAllOf {
    }

    @Schema(not = A.class)
    public static class TestNot {
    }

    @Schema(properties = {
            @SchemaProperty(name = "prop", oneOf = { A.class, B.class, C.class })
    })
    public static class TestOneOfProperty {
    }

    @Schema(properties = {
            @SchemaProperty(name = "prop", anyOf = { A.class, B.class, C.class })
    })
    public static class TestAnyOfProperty {
    }

    @Schema(properties = {
            @SchemaProperty(name = "prop", allOf = { A.class, B.class, C.class })
    })
    public static class TestAllOfProperty {
    }

    @Schema(properties = @SchemaProperty(name = "prop", not = A.class))
    public static class TestNotProperty {
    }

    @Schema(ifSchema = A.class, thenSchema = B.class, elseSchema = C.class)
    public static class TestIfThenElse {
    }

    @Schema(properties = @SchemaProperty(name = "prop", ifSchema = A.class, thenSchema = B.class, elseSchema = C.class))
    public static class TestIfThenElseProperty {
    }

    @Schema(dependentSchemas = {
            @DependentSchema(name = "field1", schema = A.class),
            @DependentSchema(name = "field2", schema = B.class)
    })
    public static class TestDepedentSchemas {
        public String field1;
    }

    @Schema(properties = @SchemaProperty(name = "prop", dependentSchemas = {
            @DependentSchema(name = "field1", schema = A.class),
            @DependentSchema(name = "field2", schema = B.class)
    }))
    public static class TestDepedentSchemasProperty {
    }

    @Schema(dependentRequired = {
            @DependentRequired(name = "field1", requires = {
                    "field2",
                    "field3"
            }),
            @DependentRequired(name = "field4", requires = "field5")
    })
    public static class TestDependentRequired {
        public String field1;
        public String field2;
        public String field3;
    }

    @Schema(properties = @SchemaProperty(name = "prop", dependentRequired = {
            @DependentRequired(name = "field1", requires = {
                    "field2",
                    "field3"
            }),
            @DependentRequired(name = "field4", requires = "field5")
    }))
    public static class TestDependentRequiredProperty {
    }

    @Schema
    public static class TestCollections {
        @Schema(prefixItems = { JavaTypeString.class, JavaTypeString.class })
        public List<String> mustStartWithTwoTypeNames;

        @Schema(contains = JavaTypeString.class)
        public List<String> mustContainATypeName;

        @Schema(contains = JavaTypeString.class, minContains = 3, maxContains = 5)
        public List<String> mustContain3To5TypeNames;

        @Schema(propertyNames = JavaTypeString.class)
        public Map<String, String> keysMustBeTypeNames;

        @Schema(patternProperties = {
                @PatternProperty(regex = "^str", schema = String.class),
                @PatternProperty(regex = "^int", schema = Integer.class)
        })
        public Map<String, Object> keysNamedByType;

        @Schema(contentEncoding = "base64", contentMediaType = "application/json", contentSchema = A.class)
        public String encodedJson;
    }

    @Schema(properties = {
            @SchemaProperty(name = "mustStartWithTwoTypeNames", type = SchemaType.ARRAY, implementation = String.class, prefixItems = {
                    JavaTypeString.class,
                    JavaTypeString.class
            }),
            @SchemaProperty(name = "mustContainATypeName", type = SchemaType.ARRAY, implementation = String.class, contains = JavaTypeString.class),
            @SchemaProperty(name = "mustContain3To5TypeNames", type = SchemaType.ARRAY, implementation = String.class, contains = JavaTypeString.class, minContains = 3, maxContains = 5),
            @SchemaProperty(name = "keysMustBeTypeNames", type = OBJECT, propertyNames = JavaTypeString.class, additionalProperties = String.class),
            @SchemaProperty(name = "keysNamedByType", type = OBJECT, implementation = Object.class, patternProperties = {
                    @PatternProperty(regex = "^str", schema = String.class),
                    @PatternProperty(regex = "^int", schema = Integer.class)
            }, additionalProperties = Object.class),
            @SchemaProperty(name = "encodedJson", implementation = String.class, contentEncoding = "base64", contentMediaType = "application/json", contentSchema = A.class)
    })
    public static class TestCollectionsProperty {
    }

    @Schema(type = SchemaType.STRING, pattern = "^[A-Z][a-zA-Z0-9]*$")
    public static class JavaTypeString {
    }

    @Schema(type = OBJECT, description = "A")
    public static class A {
    }

    @Schema(description = "B")
    public static class B {
    }

    @Schema(description = "C")
    public static class C {
    }
}
