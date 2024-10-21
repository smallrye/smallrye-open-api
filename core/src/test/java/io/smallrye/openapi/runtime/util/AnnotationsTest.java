package io.smallrye.openapi.runtime.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.OASFactory;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class AnnotationsTest extends IndexScannerTestBase {

    enum AnnotationEnum {
        VAL1,
        VAL2
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Nested {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(AllKinds.List.class)
    @interface AllKinds {

        @Retention(RetentionPolicy.RUNTIME)
        @interface List {
            AllKinds[] value();
        }

        boolean boolValue() default true;

        boolean[] boolArray() default {};

        byte byteValue() default 0;

        byte[] byteArray() default {};

        char charValue() default 0;

        char[] charArray() default {};

        double doubleValue() default 0;

        double[] doubleArray() default {};

        float floatValue() default 0;

        float[] floatArray() default {};

        int intValue() default 0;

        int[] intArray() default {};

        long longValue() default 0;

        long[] longArray() default {};

        short shortValue() default 0;

        short[] shortArray() default {};

        String stringValue() default "";

        String[] stringArray() default {};

        Class<?> classValue() default Object.class;

        Class<?>[] classArray() default {};

        AnnotationEnum enumValue() default AnnotationEnum.VAL1;

        AnnotationEnum[] enumArray() default {};

        Nested nestedValue() default @Nested(value = "");

        Nested[] nestedArray() default {};

    }

    @ParameterizedTest
    @CsvSource({
            "boolArray, boolean[]",
            "byteArray, byte[]",
            "charArray, char[]",
            "doubleArray, double[]",
            "floatArray, float[]",
            "intArray, int[]",
            "longArray, long[]",
            "shortArray, short[]",
            "stringArray, java.lang.String[]",
            "classArray, org.jboss.jandex.Type[]",
            "enumArray, java.lang.String[]",
            "nestedArray, org.jboss.jandex.AnnotationInstance[]",
    })
    void testGetAnnotationValuesWithEmptyArrays(String valueName, Class<?> expectedType) throws IOException {
        class Bean {
            @AllKinds(boolArray = {}, byteArray = {}, charArray = {}, doubleArray = {}, floatArray = {}, intArray = {}, longArray = {}, shortArray = {}, stringArray = {}, classArray = {}, enumArray = {}, nestedArray = {})
            String target;
        }

        FilteredIndexView index = new FilteredIndexView(
                Index.of(Bean.class, AllKinds.class, Nested.class, AnnotationEnum.class), emptyConfig());
        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                Collections.emptyList(),
                emptyConfig(), OASFactory.createOpenAPI());
        Object value = context.annotations().getAnnotationValue(index.getClassByName(Bean.class).field("target"),
                DotName.createSimple(AllKinds.class), valueName);
        assertTrue(expectedType.isInstance(value));
    }

    @ParameterizedTest
    @CsvSource({
            "boolValue, java.lang.Boolean",
            "byteValue, java.lang.Byte",
            "charValue, java.lang.Character",
            "doubleValue, java.lang.Double",
            "floatValue, java.lang.Float",
            "intValue, java.lang.Integer",
            "longValue, java.lang.Long",
            "shortValue, java.lang.Short",
            "stringValue, java.lang.String",
            "classValue, org.jboss.jandex.Type",
            "enumValue, java.lang.String",
            "nestedValue, org.jboss.jandex.AnnotationInstance",
    })
    void testGetAnnotationValuesWithOverrides(String valueName, Class<?> expectedType) throws IOException {
        class Bean {
            @AllKinds(boolValue = false, byteValue = 1, charValue = 1, doubleValue = 1, floatValue = 1, intValue = 1, longValue = 1, shortValue = 1, stringValue = "Hello", classValue = java.lang.reflect.Type.class, enumValue = AnnotationEnum.VAL2, nestedValue = @Nested(value = "World"))
            String target;
        }

        FilteredIndexView index = new FilteredIndexView(
                Index.of(Bean.class, AllKinds.class, Nested.class, AnnotationEnum.class), emptyConfig());
        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                Collections.emptyList(),
                emptyConfig(), OASFactory.createOpenAPI());
        Object value = context.annotations().getAnnotationValue(index.getClassByName(Bean.class).field("target"),
                DotName.createSimple(AllKinds.class), valueName);
        assertTrue(expectedType.isInstance(value));
    }

    @AllKinds(boolArray = {}, byteArray = {}, charArray = {}, doubleArray = {}, floatArray = {}, intArray = {}, longArray = {}, shortArray = {}, stringArray = {}, classArray = {}, enumArray = {}, nestedArray = {})
    @AllKinds(boolValue = false, byteValue = 1, charValue = 1, doubleValue = 1, floatValue = 1, intValue = 1, longValue = 1, shortValue = 1, stringValue = "Hello", classValue = java.lang.reflect.Type.class, enumValue = AnnotationEnum.VAL2, nestedValue = @Nested(value = "World"))
    @Retention(RetentionPolicy.RUNTIME)
    @interface AllKindsComposed {
    }

    @Test
    void testComposedAnnotation() throws IOException {
        class Bean {
            @AllKindsComposed
            String target;
        }

        FilteredIndexView index = new FilteredIndexView(Index.of(Bean.class, AllKinds.class, AllKinds.List.class, Nested.class,
                AnnotationEnum.class, AllKindsComposed.class, Retention.class, Repeatable.class), emptyConfig());
        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                Collections.emptyList(),
                emptyConfig(), OASFactory.createOpenAPI());

        List<AnnotationInstance> annotations = context.annotations().getRepeatableAnnotation(
                index.getClassByName(Bean.class).field("target"),
                DotName.createSimple(AllKinds.class),
                DotName.createSimple(AllKinds.List.class));

        assertEquals(2, annotations.size());
    }
}
