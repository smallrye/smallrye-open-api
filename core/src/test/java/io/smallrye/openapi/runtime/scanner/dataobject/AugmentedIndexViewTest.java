package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

class AugmentedIndexViewTest {

    interface IFace0 {
        void method1();
    }

    static class Impl0 implements IFace0 {
        @Override
        public void method1() {
            // test
        }
    }

    @Test
    void testAncestryWithMissingInterface() throws IOException {
        IndexView index = Index.of(Impl0.class);
        AugmentedIndexView augmented = AugmentedIndexView.augment(index);
        ClassInfo impl0 = index.getClassByName(Impl0.class);
        MethodInfo method1 = impl0.method("method1");
        Map<ClassInfo, MethodInfo> ancestors = augmented.ancestry(method1);
        assertEquals(1, ancestors.size());
        assertTrue(ancestors.containsKey(impl0));
    }

    @Test
    void testAncestryWithInterfacePresent() throws IOException {
        IndexView index = Index.of(IFace0.class, Impl0.class);
        AugmentedIndexView augmented = AugmentedIndexView.augment(index);
        ClassInfo iface0 = index.getClassByName(IFace0.class);
        ClassInfo impl0 = index.getClassByName(Impl0.class);
        MethodInfo method1 = impl0.method("method1");
        Map<ClassInfo, MethodInfo> ancestors = augmented.ancestry(method1);
        assertEquals(2, ancestors.size());
        assertTrue(ancestors.containsKey(iface0));
        assertTrue(ancestors.containsKey(impl0));
    }
}
