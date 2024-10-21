package io.smallrye.openapi.runtime.scanner.dataobject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ModuleInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.io.Names;
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
        IndexView indexedNames;

        try {
            indexedNames = Index.of(Names.getIndexable());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        this.index = CompositeIndex.create(Objects.requireNonNull(index), indexedNames);
    }

    public ClassInfo getClass(Type type) {
        return index.getClassByName(TypeUtil.getName(Objects.requireNonNull(type)));
    }

    public boolean containsClass(Type type) {
        return getClass(Objects.requireNonNull(type)) != null;
    }

    public ClassInfo getClass(Class<?> klazz) {
        return index.getClassByName(DotName.createSimple(Objects.requireNonNull(klazz).getName()));
    }

    /**
     * Retrieve the unique <code>Type</code>s that the given <code>ClassInfo</code>
     * implements.
     *
     * @param klass
     * @return the <code>Set</code> of interfaces
     *
     */
    public Set<Type> interfaces(ClassInfo klass) {
        Set<Type> interfaces = new LinkedHashSet<>();

        for (Type type : klass.interfaceTypes()) {
            interfaces.add(type);

            if (containsClass(type)) {
                interfaces.addAll(interfaces(getClass(type)));
            }
        }

        return interfaces;
    }

    /**
     * Builds an insertion-order map of a class's inheritance chain, starting
     * with the klazz argument.
     *
     * @param klazz the class to retrieve inheritance
     * @param type type of the klazz
     * @return map of a class's inheritance chain/ancestry
     */
    public Map<ClassInfo, Type> inheritanceChain(ClassInfo klazz, Type type) {
        Map<ClassInfo, Type> chain = new LinkedHashMap<>();

        do {
            chain.put(klazz, type);
        } while ((type = klazz.superClassType()) != null &&
                (klazz = index.getClassByName(TypeUtil.getName(type))) != null);

        return chain;
    }

    public Map<ClassInfo, MethodInfo> ancestry(MethodInfo method) {
        ClassInfo declaringClass = method.declaringClass();
        Type resourceType = Type.create(declaringClass.name(), Type.Kind.CLASS);
        Map<ClassInfo, Type> chain = inheritanceChain(declaringClass, resourceType);
        Map<ClassInfo, MethodInfo> ancestry = new LinkedHashMap<>();

        for (ClassInfo classInfo : chain.keySet()) {
            ancestry.put(classInfo, null);

            classInfo.methods()
                    .stream()
                    .filter(m -> !m.isSynthetic())
                    .filter(m -> isSameSignature(method, m))
                    .findFirst()
                    .ifPresent(m -> ancestry.put(classInfo, m));

            interfaces(classInfo)
                    .stream()
                    .filter(type -> !TypeUtil.knownJavaType(type.name()))
                    .map(this::getClass)
                    .filter(Objects::nonNull)
                    .map(iface -> {
                        ancestry.put(iface, null);
                        return iface;
                    })
                    .flatMap(iface -> iface.methods().stream())
                    .filter(m -> isSameSignature(method, m))
                    .forEach(m -> ancestry.put(m.declaringClass(), m));
        }

        return ancestry;
    }

    private static boolean isSameSignature(MethodInfo m1, MethodInfo m2) {
        return Objects.equals(m1.name(), m2.name())
                && m1.parametersCount() == m2.parametersCount()
                && Objects.equals(m1.parameterTypes(), m2.parameterTypes());
    }

    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return index.getKnownClasses();
    }

    @Override
    public ClassInfo getClassByName(DotName className) {
        return index.getClassByName(Objects.requireNonNull(className));
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        return index.getKnownDirectSubclasses(Objects.requireNonNull(className));
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        return index.getAllKnownSubclasses(Objects.requireNonNull(className));
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
        return index.getKnownDirectSubinterfaces(Objects.requireNonNull(interfaceName));
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
        return index.getAllKnownSubinterfaces(Objects.requireNonNull(interfaceName));
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
        return index.getKnownDirectImplementors(Objects.requireNonNull(className));
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        return index.getAllKnownImplementors(Objects.requireNonNull(interfaceName));
    }

    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        return index.getAnnotations(Objects.requireNonNull(annotationName));
    }

    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView annotationIndex) {
        Objects.requireNonNull(annotationName);
        Objects.requireNonNull(annotationIndex);
        return index.getAnnotationsWithRepeatable(annotationName, annotationIndex);
    }

    @Override
    public Collection<ModuleInfo> getKnownModules() {
        return index.getKnownModules();
    }

    @Override
    public ModuleInfo getModuleByName(DotName moduleName) {
        return index.getModuleByName(Objects.requireNonNull(moduleName));
    }

    @Override
    public Collection<ClassInfo> getKnownUsers(DotName className) {
        return index.getKnownUsers(Objects.requireNonNull(className));
    }

    @Override
    public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
        return index.getClassesInPackage(Objects.requireNonNull(packageName));
    }

    @Override
    public Set<DotName> getSubpackages(DotName packageName) {
        return index.getSubpackages(Objects.requireNonNull(packageName));
    }
}
