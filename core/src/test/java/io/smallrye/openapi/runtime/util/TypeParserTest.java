package io.smallrye.openapi.runtime.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TypeParserTest {

    @Test
    void testParameterizedArrayComponent() {
        Type result = TypeParser.parse("java.util.List<? extends java.lang.String[]>[][]");
        assertNotNull(result);
        assertEquals(Type.Kind.ARRAY, result.kind());
        assertEquals(2, result.asArrayType().dimensions());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, result.asArrayType().constituent().kind());

        Type typeArg = result.asArrayType().constituent().asParameterizedType().arguments().get(0);

        assertEquals(Type.Kind.WILDCARD_TYPE, typeArg.kind());
        assertEquals(Type.Kind.ARRAY, typeArg.asWildcardType().extendsBound().kind());
        assertEquals(1, typeArg.asWildcardType().extendsBound().asArrayType().dimensions());
        assertEquals(Type.Kind.CLASS, typeArg.asWildcardType().extendsBound().asArrayType().constituent().kind());
        assertEquals(String.class.getName(),
                typeArg.asWildcardType().extendsBound().asArrayType().constituent().asClassType().name().toString());
    }

    @Test
    void testPrimitive() {
        Type result = TypeParser.parse("float");
        assertNotNull(result);
        assertEquals(Type.Kind.PRIMITIVE, result.kind());
        assertEquals("float", result.name().toString());
    }

    @Test
    void testClassType() {
        Type result = TypeParser.parse("java.lang.Object");
        assertNotNull(result);
        assertEquals(Type.Kind.CLASS, result.kind());
        assertEquals("java.lang.Object", result.name().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "java.util.List<? super java.lang.String>",
            "java.util.List<? extends java.lang.String>",
            "java.util.List<?>"
    })
    void testWildcards(String typeSignature) {
        Type result = TypeParser.parse(typeSignature);
        assertNotNull(result);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, result.kind());
        assertEquals("java.util.List", result.name().toString());
        assertEquals(1, result.asParameterizedType().arguments().size());
        assertEquals(Type.Kind.WILDCARD_TYPE, result.asParameterizedType().arguments().get(0).kind());
    }
}
