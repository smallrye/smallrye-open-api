package io.smallrye.openapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.junit.jupiter.api.Test;

class SchemaTest {

    /**
     * Test setting fields to the wrong types
     */
    @Test
    void testInvalidTypes() {
        Schema schema = OASFactory.createSchema();

        schema.set("maxLength", "four");
        assertEquals("four", schema.get("maxLength"));
        assertNull(schema.getMaxLength());

        schema.set("properties", "foo");
        assertEquals("foo", schema.get("properties"));
        assertNull(schema.getProperties());
    }
}
