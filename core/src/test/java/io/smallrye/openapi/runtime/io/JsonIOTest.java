package io.smallrye.openapi.runtime.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

abstract class JsonIOTest<V, A extends V, O extends V, AB, OB> {

    protected JsonIO<V, A, O, AB, OB> target;

    @ParameterizedTest
    @CsvSource({
            "{ \"key\": 3.1415 }, 3.1415",
            "{ \"key\": \"3.1415\" }, ",
            "{ \"key\": [ 3.1415 ] }, ",
    })
    void testGetJsonBigDecimal(String input, BigDecimal expected) {
        @SuppressWarnings("unchecked")
        O value = (O) target.fromString(input, Format.JSON);
        assertEquals(expected, target.getJsonBigDecimal(value, "key"));
    }

    @ParameterizedTest
    @CsvSource({
            "{ \"key\": 3.141592653589793238462643383279 }, 3.141592653589793238462643383279",
    })
    void testFromJsonBigDecimal(String input, String expected) {
        @SuppressWarnings("unchecked")
        O value = (O) target.fromString(input, Format.JSON);
        V jsonValue = target.getValue(value, "key");
        Object javaValue = target.fromJson(jsonValue);
        assertEquals(new BigDecimal(expected), javaValue);
    }
}
