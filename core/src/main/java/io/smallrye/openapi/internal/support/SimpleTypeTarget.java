package io.smallrye.openapi.internal.support;

import java.util.Collection;
import java.util.Collections;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Declaration;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.RecordComponentInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeTarget;

/**
 * Simple wrapper type that may be used to allow a Type to be accessed like
 * an AnnotationTarget.
 */
public final class SimpleTypeTarget implements AnnotationTarget {

    public static final SimpleTypeTarget create(Type type) {
        return new SimpleTypeTarget(type);
    }

    private final Type type;

    private SimpleTypeTarget(Type type) {
        this.type = type;
    }

    @Override
    public Kind kind() {
        return Kind.TYPE;
    }

    @Override
    public boolean isDeclaration() {
        return false;
    }

    @Override
    public Declaration asDeclaration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassInfo asClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldInfo asField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodInfo asMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodParameterInfo asMethodParameter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeTarget asType() {
        throw new UnsupportedOperationException("Not a TypeTarget");
    }

    @Override
    public RecordComponentInfo asRecordComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAnnotation(DotName name) {
        return type.hasAnnotation(name);
    }

    @Override
    public AnnotationInstance annotation(DotName name) {
        return type.annotation(name);
    }

    @Override
    public Collection<AnnotationInstance> annotations(DotName name) {
        return Collections.singletonList(type.annotation(name));
    }

    @Override
    public Collection<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        return type.annotationsWithRepeatable(name, index);
    }

    @Override
    public Collection<AnnotationInstance> annotations() {
        return type.annotations();
    }

    @Override
    public boolean hasDeclaredAnnotation(DotName name) {
        return type.hasAnnotation(name);
    }

    @Override
    public AnnotationInstance declaredAnnotation(DotName name) {
        return type.annotation(name);
    }

    @Override
    public Collection<AnnotationInstance> declaredAnnotationsWithRepeatable(DotName name, IndexView index) {
        return type.annotationsWithRepeatable(name, index);
    }

    @Override
    public Collection<AnnotationInstance> declaredAnnotations() {
        return type.annotations();
    }
}
