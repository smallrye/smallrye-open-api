package io.smallrye.openapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.spi.OASFactoryResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BaseExtensibleModelDeepCopyTest {
    private static final String PRIVATE_EXTENSION = Extensions.PRIVATE_EXT_PREFIX + "private";

    interface TestExtensible extends Extensible<TestExtensible>, Constructible {
    }

    static class TestExtensibleModel extends BaseExtensibleModel<TestExtensible> implements TestExtensible {

        @Override
        public Class<TestExtensible> getConstructibleClass() {
            return TestExtensible.class;
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
                return (T) new TestExtensibleModel();
            }
        });
    }

    @AfterAll
    static void teardown() {
        OASFactoryResolver.setInstance(null);
    }

    private static TestExtensibleModel createModel() {
        TestExtensibleModel child = new TestExtensibleModel();
        child.setProperty("string", "string-value");
        child.addExtension("x-string", "string-value");

        TestExtensibleModel parent = new TestExtensibleModel();
        parent.setProperty("string", "string-value");
        parent.setProperty("model", child);

        parent.addExtension("x-string", "string-value");
        parent.addExtension("x-list", List.of("string-value1", "string-value2", "string-value3"));
        parent.addExtension("x-map",
                Map.of("string-key1", "string-value1", "string-key2", "string-value2", "string-key3", "string-value3"));
        parent.addExtension(PRIVATE_EXTENSION, "private-value");

        return parent;
    }

    @Test
    void testCopyEqualsSource() {
        TestExtensibleModel source = createModel();
        TestExtensibleModel copy = (TestExtensibleModel) BaseModel.deepCopy(source, TestExtensible.class);

        assertNotSame(source, copy);
        assertEquals(source, copy);
        assertEquals(copy, source);
        assertEquals(source.hashCode(), copy.hashCode());
    }

    @Test
    void testExtensionsCopied() {
        TestExtensibleModel source = createModel();
        TestExtensibleModel copy = (TestExtensibleModel) BaseModel.deepCopy(source, TestExtensible.class);

        assertEquals(source.getExtensions(), copy.getExtensions());
        assertEquals(source.getAllExtensions(), copy.getAllExtensions());
        assertEquals(List.of("x-string", "x-list", "x-map", PRIVATE_EXTENSION),
                List.copyOf(copy.getAllExtensions().keySet()));
        assertFalse(copy.getExtensions().containsKey(PRIVATE_EXTENSION));
        assertFalse(copy.hasExtension("string"));

        assertEquals("string-value", copy.<TestExtensibleModel> getProperty("model").getExtension("x-string"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCopyIndependentOfSource() {
        TestExtensibleModel source = createModel();
        TestExtensibleModel copy = (TestExtensibleModel) BaseModel.deepCopy(source, TestExtensible.class);

        copy.addExtension("x-new", "new-value");
        copy.removeExtension("x-string");
        ((Map<String, Object>) copy.getExtension("x-map")).put("new-key", "new-value");
        copy.<TestExtensibleModel> getProperty("model").addExtension("x-new", "new-value");

        assertFalse(source.hasExtension("x-new"));
        assertTrue(source.hasExtension("x-string"));
        assertEquals("string-value", source.getExtension("x-string"));
        assertFalse(((Map<String, Object>) source.getExtension("x-map")).containsKey("new-key"));
        assertFalse(source.<TestExtensibleModel> getProperty("model").hasExtension("x-new"));
    }

    @Test
    void testCopyWithoutExtensions() {
        TestExtensibleModel source = new TestExtensibleModel();
        source.setProperty("string", "string-value");
        TestExtensibleModel copy = (TestExtensibleModel) BaseModel.deepCopy(source, TestExtensible.class);

        assertNull(copy.getExtensions());
        assertNull(copy.getAllExtensions());

        copy.addExtension("x-new", "new-value");

        assertEquals("new-value", copy.getExtension("x-new"));
        assertNull(source.getExtensions());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUnmodifiableCopy() {
        TestExtensibleModel source = createModel();
        TestExtensibleModel copy = (TestExtensibleModel) BaseModel.deepCopy(source, TestExtensible.class, true);

        assertEquals(source, copy);
        assertThrows(UnsupportedOperationException.class, () -> copy.addExtension("x-new", "new-value"));
        assertThrows(UnsupportedOperationException.class, () -> copy.removeExtension("x-string"));
        assertThrows(UnsupportedOperationException.class, () -> copy.setExtensions(Map.of("x-new", "new-value")));
        assertThrows(UnsupportedOperationException.class,
                () -> ((List<String>) copy.getExtension("x-list")).add("new-value"));
        assertThrows(UnsupportedOperationException.class,
                () -> ((Map<String, Object>) copy.getExtension("x-map")).put("new-key", "new-value"));

        assertThrows(UnsupportedOperationException.class,
                () -> copy.<TestExtensibleModel> getProperty("model").addExtension("x-new", "new-value"));
    }

    @Test
    void testUnmodifiableCopyWithoutExtensions() {
        TestExtensibleModel source = new TestExtensibleModel();
        source.setProperty("string", "string-value");
        TestExtensibleModel copy = (TestExtensibleModel) BaseModel.deepCopy(source, TestExtensible.class, true);

        assertThrows(UnsupportedOperationException.class, () -> copy.addExtension("x-new", "new-value"));
    }

}
