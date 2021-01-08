package io.smallrye.openapi.runtime.scanner.dataobject;

import java.util.Collection;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * IndexView augmented with additional methods for common operations
 * used throughout the data object scanning code.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class AugmentedIndexView implements IndexView {

    private final IndexView index;

    public static AugmentedIndexView augment(IndexView index) {
        if (index instanceof AugmentedIndexView) {
            return (AugmentedIndexView) index;
        }
        return new AugmentedIndexView(index);
    }

    private AugmentedIndexView(IndexView index) {
        validateInput(index);
        this.index = index;
    }

    public ClassInfo getClass(Type type) {
        validateInput(type);
        return index.getClassByName(TypeUtil.getName(type));
    }

    public boolean containsClass(Type type) {
        validateInput(type);
        return getClass(type) != null;
    }

    public ClassInfo getClass(Class<?> klazz) {
        validateInput(klazz);
        return index.getClassByName(DotName.createSimple(klazz.getName()));
    }

    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return index.getKnownClasses();
    }

    @Override
    public ClassInfo getClassByName(DotName className) {
        validateInput(className);
        return index.getClassByName(className);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        validateInput(className);
        return index.getKnownDirectSubclasses(className);
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        validateInput(className);
        return index.getAllKnownSubclasses(className);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
        validateInput(className);
        return index.getKnownDirectImplementors(className);
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        validateInput(interfaceName);
        return index.getAllKnownImplementors(interfaceName);
    }

    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        validateInput(annotationName);
        return index.getAnnotations(annotationName);
    }

    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView annotationIndex) {
        validateInput(annotationName, annotationIndex);
        return index.getAnnotationsWithRepeatable(annotationName, annotationIndex);
    }

    private void validateInput(Object... inputs) {
        for (Object input : inputs) {
            if (input == null)
                throw DataObjectMessages.msg.notNull();
        }
    }
}
