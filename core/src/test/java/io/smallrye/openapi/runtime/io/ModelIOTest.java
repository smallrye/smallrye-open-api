package io.smallrye.openapi.runtime.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ModelIOTest {

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testToLinkedMapDuplicateKeyThrowsException() {
        Stream<Map.Entry<String, String>> stream = Stream.of("k1", "k1").map(k -> new SimpleEntry(k, ""));
        Collector<Map.Entry<String, String>, ?, Map<String, String>> collector = ModelIO.toLinkedMap();
        assertThrows(IllegalStateException.class, () -> stream.collect(collector));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testToLinkedMapParallelCombined() {
        Stream<Map.Entry<String, String>> stream = IntStream
                .range(0, 100).<Map.Entry<String, String>> mapToObj(
                        i -> new SimpleEntry(Integer.toString(i), i % 2 == 0 ? "" : null));
        Collector<Map.Entry<String, String>, ?, Map<String, String>> collector = ModelIO.toLinkedMap();
        Map<String, String> result = stream.parallel().collect(collector);
        assertEquals(100, result.size());
        assertEquals(50, result.values().stream().filter(Objects::isNull).count());
        assertEquals(50, result.values().stream().filter(Objects::nonNull).count());
    }
}
