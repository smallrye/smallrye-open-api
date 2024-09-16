package io.smallrye.openapi.api.models;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MapBasedModelmplTest {

    public static class TestMapModel extends MapBasedModelImpl {
    };

    @Test
    public void getWrongType() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");

        assertEquals("Hello", test.getProperty("test", String.class));
        assertEquals("Hello", test.getProperty("test", Object.class));
        assertNull(test.getProperty("test", Integer.class));
    }

    @Test
    public void addToNonList() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.addToListProperty("test", 4);

        assertEquals(asList(4), test.getProperty("test", Object.class));
    }

    @Test
    public void removeFromNonList() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.removeFromListProperty("test", 4);

        assertEquals("Hello", test.getProperty("test", Object.class));
    }

    @Test
    public void addToNonMap() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.addToMapProperty("test", "foo", 4);

        Map<String, Integer> expected = new HashMap<>();
        expected.put("foo", 4);
        assertEquals(expected, test.getProperty("test", Object.class));
    }

    @Test
    public void removeFromNonMap() {
        TestMapModel test = new TestMapModel();
        test.setProperty("test", "Hello");
        test.removeFromMapProperty("test", "foo");

        assertEquals("Hello", test.getProperty("test", Object.class));
    }
}
