package io.smallrye.openapi.runtime.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.OpenApiDataObjectScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class TypeUtilTest extends IndexScannerTestBase {

    private static final Type ITERABLE_TYPE = OpenApiDataObjectScanner.ITERABLE_TYPE;
    private static final Type TYPE_ENUM = OpenApiDataObjectScanner.ENUM_TYPE;
    private static final Type TYPE_MAP = OpenApiDataObjectScanner.MAP_TYPE;

    @Test
    void testIsA_BothIndexed() {
        final Class<?> subjectClass = ArrayCollection.class;
        Index index = indexOf(subjectClass, Collection.class);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertTrue(result);
    }

    @Test
    void testIsA_SubjectIndexed() {
        final Class<?> subjectClass = ArrayCollection.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertTrue(result);
    }

    @Test
    void testIsA_ObjectIndexed() {
        final Class<?> subjectClass = ArrayCollection.class;
        Index index = indexOf(Collection.class);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertTrue(result);
    }

    @Test
    void testIsA_IndexedSubjectImplementsObject() {
        final Class<?> subjectClass = CustomCollection.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertTrue(result);
    }

    @Test
    void testIsA_IndexedSubjectImplementsOther() {
        final Class<?> subjectClass = CustomMap.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertFalse(result);
    }

    @Test
    void testIsA_IndexedSubjectExtendsUnindexedCollection() {
        final Class<?> subjectClass = ChildCollection.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertTrue(result);
    }

    @Test
    void testIsA_IndexedSubjectUnrelatedToObject() {
        final Class<?> subjectClass = UnrelatedType.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertFalse(result);
    }

    @Test
    void testIsA_UnindexedPrimitiveSubjectUnrelatedToObject() {
        final Class<?> subjectClass = int.class;
        Index index = indexOf();
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.PRIMITIVE);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertFalse(result);
    }

    @Test
    void testIsA_UnindexedPrimitiveWrapperSubjectUnrelatedToObject() {
        final Class<?> subjectClass = Integer.class;
        final DotName subjectName = DotName.createSimple(subjectClass.getName());
        Index index = indexOf();
        Type testSubject = Type.create(subjectName, Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertFalse(result);
    }

    @Test
    void testIsA_SubjectIsJavaLangObject() {
        final Class<?> subjectClass = Object.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, ITERABLE_TYPE);
        assertFalse(result);
    }

    @Test
    void testIsA_SubjectSameAsObject() {
        final Class<?> subjectClass = MapContainer.class;
        final DotName subjectName = DotName.createSimple(subjectClass.getName());
        Index index = indexOf(subjectClass);
        ClassInfo subjectInfo = index.getClassByName(subjectName);
        Type testSubject = subjectInfo.field("theMap").type();
        boolean result = isA(index, testSubject, TYPE_MAP);
        assertTrue(result);
    }

    @Test
    void testIsA_SubjectSuperSameAsObject() {
        final Class<?> subjectClass = TestEnum.class;
        Index index = indexOf(subjectClass);
        Type testSubject = Type.create(DotName.createSimple(subjectClass.getName()), Type.Kind.CLASS);
        boolean result = isA(index, testSubject, TYPE_ENUM);
        assertTrue(result);
    }

    private boolean isA(Index index, Type testSubject, Type testObject) {
        AnnotationScannerContext context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                emptyConfig());
        return TypeUtil.isA(context, testSubject, testObject);
    }

    static class ArrayCollection extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    static abstract class CustomCollection implements Collection<String> {
    }

    static abstract class CustomMap implements Map<String, Object> {
    }

    static abstract class ChildCollection extends CustomCollection {
    }

    static class UnrelatedType {

    }

    static class MapContainer {
        @SuppressWarnings("unused")
        private Map<String, String> theMap;
    }

    static enum TestEnum {
        VALUE1,
        VALUE2,
        VALUE3;
    }
}
