package io.smallrye.openapi.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Deprecated
class OpenApiProcessorTest {

    ClassLoader loader;

    @BeforeEach
    void setup() {
        this.loader = Thread.currentThread().getContextClassLoader();
    }

    @Test
    void testNewInstanceWithNullClassName() {
        Object instance = OpenApiProcessor.newInstance(null, loader, OpenApiProcessor.EMPTY_INDEX);
        assertNull(instance);
    }

    @Test
    void testNewInstanceWithNotFoundClassName() {
        String invalidClassName = UUID.randomUUID().toString();

        Throwable thrown = assertThrows(OpenApiRuntimeException.class,
                () -> OpenApiProcessor.newInstance(invalidClassName, loader, OpenApiProcessor.EMPTY_INDEX));
        assertEquals(ClassNotFoundException.class, thrown.getCause().getClass());
    }

    @Test
    void testNewInstanceWithEmptyIndex() {
        IndexAwareObject instance = OpenApiProcessor.newInstance(IndexAwareObject.class.getName(), loader,
                OpenApiProcessor.EMPTY_INDEX);
        assertNotNull(instance);
        assertFalse(instance.defaultConstructorUsed);
        assertEquals(0, instance.index.getKnownClasses().size());
    }

    @Test
    void testNewInstanceWithIndexUnsupported() {
        IndexUnawareObject instance = OpenApiProcessor.newInstance(IndexUnawareObject.class.getName(), loader,
                OpenApiProcessor.EMPTY_INDEX);
        assertNotNull(instance);
        assertTrue(instance.defaultConstructorUsed);
    }

    static class IndexAwareObject {
        IndexView index;
        boolean defaultConstructorUsed;

        IndexAwareObject() {
            defaultConstructorUsed = true;
        }

        IndexAwareObject(IndexView index) {
            this.index = index;
            defaultConstructorUsed = false;
        }

        IndexAwareObject(Object other) {
            defaultConstructorUsed = false;
        }
    }

    static class IndexUnawareObject {
        boolean defaultConstructorUsed;

        IndexUnawareObject() {
            defaultConstructorUsed = true;
        }

        IndexUnawareObject(Object other) {
            defaultConstructorUsed = false;
        }
    }
}
