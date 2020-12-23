package io.smallrye.openapi.runtime.io.schema;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.junit.Test;

import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class SchemaFactoryTest extends IndexScannerTestBase {

    @Test
    public void testResolveAsyncType() {
        Index index = indexOf();
        Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
        Type target = ParameterizedType.create(DotName.createSimple(CompletableFuture.class.getName()),
                new Type[] { STRING_TYPE },
                null);
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        Type result = SchemaFactory.resolveAsyncType(context, target, Collections.emptyList());
        assertEquals(STRING_TYPE, result);
    }

}
