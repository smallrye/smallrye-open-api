package io.smallrye.openapi.runtime.io.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.WildcardType;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class SchemaFactoryTest extends IndexScannerTestBase {

    @Test
    void testResolveAsyncType() {
        Index index = indexOf(new Class[0]);
        Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
        Type target = ParameterizedType.create(DotName.createSimple(CompletableFuture.class.getName()),
                new Type[] { STRING_TYPE },
                null);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Type result = SchemaFactory.resolveAsyncType(context, target, Collections.emptyList());
        assertEquals(STRING_TYPE, result);
    }

    @Test
    void testWildcardSchemaIsEmpty() {
        Index index = indexOf(new Class[0]);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Type type = WildcardType.create(null, false);
        Schema result = SchemaFactory.typeToSchema(context, type, null, Collections.emptyList());
        assertNull(result.getType());
    }

    @org.eclipse.microprofile.openapi.annotations.media.Schema(description = "An example enum with a value-driven schema type")
    public static enum ExampleEnum1 {
        ONE(1),
        TWO(2);

        private final Integer value;

        ExampleEnum1(final Integer value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        // type derived as int with format int32 due to JsonValue return type
        public Integer getValue() {
            return value;
        }
    }

    @Test
    void testEnumToSchemaTypeUsesValueWithAnnotation() {
        Index index = indexOf(ExampleEnum1.class);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Schema result = SchemaFactory.enumToSchema(context, Type.create(ExampleEnum1.class));
        assertEquals(Schema.SchemaType.INTEGER, result.getType());
        assertEquals("int32", result.getFormat());
        assertEquals("An example enum with a value-driven schema type", result.getDescription());
    }

    public static enum ExampleEnum2 {
        ONE(1L),
        TWO(2L);

        private final Long value;

        ExampleEnum2(final Long value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        // type derived as int with format int64 due to JsonValue return type
        public Long getValue() {
            return value;
        }
    }

    @Test
    void testEnumToSchemaTypeUsesValueWithoutAnnotation() {
        Index index = indexOf(ExampleEnum2.class);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Schema result = SchemaFactory.enumToSchema(context, Type.create(ExampleEnum2.class));
        assertEquals(Schema.SchemaType.INTEGER, result.getType());
        assertEquals("int64", result.getFormat());
        assertNull(result.getDescription());
    }

    @org.eclipse.microprofile.openapi.annotations.media.Schema(description = "An example enum with no values", enumeration = {
            "VAL1", "VAL2" })
    public static enum ExampleEnum3 {
    }

    @Test
    void testEnumToSchemaTypeWithEmptyEnum() {
        Index index = indexOf(ExampleEnum3.class);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Schema result = SchemaFactory.enumToSchema(context, Type.create(ExampleEnum3.class));
        assertEquals(Schema.SchemaType.STRING, result.getType());
        assertEquals(Arrays.asList("VAL1", "VAL2"), result.getEnumeration());
    }

    interface EnumValue4 {
        @com.fasterxml.jackson.annotation.JsonValue
        // type derived as int with format int64 due to JsonValue return type
        boolean getValue();
    }

    @org.eclipse.microprofile.openapi.annotations.media.Schema
    public static enum ExampleEnum4 implements EnumValue4 {
        TRUE(true),
        FALSE(false);

        final boolean value;

        ExampleEnum4(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return value;
        }
    }

    @Test
    void testEnumToSchemaTypeWithInheritance() {
        Index index = indexOf(ExampleEnum4.class, EnumValue4.class);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Schema result = SchemaFactory.enumToSchema(context, Type.create(ExampleEnum4.class));
        assertEquals(Arrays.asList(Schema.SchemaType.BOOLEAN), result.getType());
        assertEquals(Arrays.asList(true, false), result.getEnumeration());
    }

    @Test
    void testParseSchemaType() {
        for (SchemaType type : SchemaType.values()) {
            if (type == SchemaType.DEFAULT) {
                assertNull(SchemaFactory.parseSchemaType(type.name()));
            } else {
                assertEquals(Schema.SchemaType.valueOf(type.name()), SchemaFactory.parseSchemaType(type.name()));
            }
        }
    }
}
