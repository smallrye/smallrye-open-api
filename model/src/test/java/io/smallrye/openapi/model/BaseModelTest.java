package io.smallrye.openapi.model;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
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
}
