package io.smallrye.openapi.model;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.junit.jupiter.api.Test;

class BaseModelTest {

    static class TestMapModel extends BaseModel<Constructible> {
    }

    @Test
    void getWrongType() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");

        assertEquals("Hello", test.getProperty("test", String.class));
        assertEquals("Hello", test.getProperty("test", Object.class));
        assertNull(test.getProperty("test", Integer.class));
    }

    @Test
    void addToNonList() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.addListPropertyEntry("test", 4);

        assertEquals(asList(4), test.getProperty("test", Object.class));
    }

    @Test
    void removeFromNonList() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.removeListPropertyEntry("test", 4);

        assertEquals("Hello", test.getProperty("test", Object.class));
    }

    @Test
    void addToNonMap() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.putMapPropertyEntry("test", "foo", 4);

        Map<String, Integer> expected = new HashMap<>();
        expected.put("foo", 4);
        assertEquals(expected, test.getProperty("test", Object.class));
    }

    @Test
    void removeFromNonMap() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.removeMapPropertyEntry("test", "foo");

        assertEquals("Hello", test.getProperty("test", Object.class));
    }

    @Test
    void testHashCodeEquality() {
        TestMapModel test1 = new TestMapModel();
        test1.setMapProperty("p1", Map.of("k1", List.of("value1")));
        test1.setListProperty("p2", List.of(new TestMapModel()));
        test1.setProperty("p3", test1);

        TestMapModel test2 = new TestMapModel();
        test2.setMapProperty("p1", Map.of("k1", List.of("value1")));
        test2.setListProperty("p2", List.of(new TestMapModel()));
        test2.setProperty("p3", test2);

        assertEquals(test1.hashCode(), test2.hashCode());
    }

    @Test
    void testHashCodeInequality() {
        TestMapModel test1 = new TestMapModel();
        test1.setMapProperty("p1", Map.of("k1", List.of("value1")));
        test1.setListProperty("p2", List.of(new TestMapModel()));
        test1.setProperty("p3", test1);

        TestMapModel test2 = new TestMapModel();
        test2.setMapProperty("p1", Map.of("k1", List.of("value2")));
        test2.setListProperty("p2", List.of(new TestMapModel()));
        test2.setProperty("p3", test2);

        assertNotEquals(test1.hashCode(), test2.hashCode());
    }
}
