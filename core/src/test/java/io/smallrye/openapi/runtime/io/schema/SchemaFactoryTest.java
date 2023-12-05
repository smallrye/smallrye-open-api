package io.smallrye.openapi.runtime.io.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.WildcardType;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class SchemaFactoryTest extends IndexScannerTestBase {

    @Test
    void testResolveAsyncType() {
        Index index = indexOf(new Class[0]);
        Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
        Type target = ParameterizedType.create(DotName.createSimple(CompletableFuture.class.getName()),
                new Type[] { STRING_TYPE },
                null);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Type result = SchemaFactory.resolveAsyncType(context, target, Collections.emptyList());
        assertEquals(STRING_TYPE, result);
    }

    @Test
    void testWildcardSchemaIsEmpty() {
        Index index = indexOf(new Class[0]);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Type type = WildcardType.create(null, false);
        Schema result = SchemaFactory.typeToSchema(context, type, null, Collections.emptyList());
        assertNull(result.getType());
    }

    @Test
    void testParseSchemaType() {
        for (SchemaType type : SchemaType.values()) {
            if (type == SchemaType.DEFAULT) {
                assertNull(SchemaFactory.parseSchemaType(type.name()));
            } else {
                assertEquals(Schema.SchemaType.valueOf(type.name()), SchemaFactory.parseSchemaType(type.name()));
            }
        }
    }
}
