package io.smallrye.openapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.spi.OASFactoryResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BaseModelDeepCopyTest {
    static class TestMapModel extends BaseModel<Constructible> implements Constructible {

        @Override
        public Class<Constructible> getConstructibleClass() {
            return Constructible.class;
        }

        @Override
        public PropertyMetadata getPropertyMetadata() {
            return null;
        }
    }

    @BeforeAll
    static void setup() {
        OASFactoryResolver.setInstance(new OASFactoryResolver() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends Constructible> T createObject(Class<T> clazz) {
                return (T) new TestMapModel();
            }
        });
    }

    @AfterAll
    static void teardown() {
        OASFactoryResolver.setInstance(null);
    }

    private static TestMapModel createModel() {
        TestMapModel child = new TestMapModel();
        child.setProperty("string", "string-value");
        child.setProperty("list", List.of("string-value1", "string-value2", "string-value3"));
        child.setProperty("map",
                Map.of("string-key1", "string-value1", "string-key2", "string-value2", "string-key3", "string-value3"));

        TestMapModel parent = new TestMapModel();
        parent.setProperty("string", "string-value");
        parent.setProperty("list", List.of("string-value1", "string-value2", "string-value3"));
        parent.setProperty("map",
                Map.of("string-key1", "string-value1", "string-key2", "string-value2", "string-key3", "string-value3"));

        parent.setProperty("model", child);

        return parent;
    }

    @Test
    void testCopyEqualsSource() {
        TestMapModel source = createModel();
        TestMapModel copy = (TestMapModel) BaseModel.deepCopy(source, Constructible.class);

        assertNotSame(source, copy);
        assertEquals(source, copy);
        assertEquals(copy, source);
        assertEquals(source.hashCode(), copy.hashCode());
    }

    @Test
    void testCopyIndependentOfSource() {
        TestMapModel source = createModel();
        TestMapModel copy = (TestMapModel) BaseModel.deepCopy(source, Constructible.class);

        copy.setProperty("new-property", "new-value");
        TestMapModel child = copy.getProperty("model");
        child.setProperty("new-property", "new-value");

        assertNull(source.getProperty("new-property"));
        assertNull((source.<TestMapModel> getProperty("model").getProperty("new-property")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUnmodifiableCopy() {
        TestMapModel source = createModel();
        TestMapModel copy = (TestMapModel) BaseModel.deepCopy(source, Constructible.class, true);

        assertEquals(source, copy);
        assertThrows(UnsupportedOperationException.class, () -> copy.setProperty("new-property", "new-value"));
        assertThrows(UnsupportedOperationException.class, () -> copy.<List<String>> getProperty("list").add("new-value"));
        assertThrows(UnsupportedOperationException.class,
                () -> copy.<Map<String, Object>> getProperty("map").put("new-key", "new-value"));

        assertThrows(UnsupportedOperationException.class,
                () -> copy.<TestMapModel> getProperty("model").setProperty("new-property", "new-value"));
        assertThrows(UnsupportedOperationException.class,
                () -> copy.<TestMapModel> getProperty("model").<List<String>> getProperty("list").add("new-value"));
        assertThrows(UnsupportedOperationException.class,
                () -> copy.<TestMapModel> getProperty("model").<Map<String, Object>> getProperty("map").put("new-key",
                        "new-value"));
    }

}
