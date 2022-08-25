package io.smallrye.openapi.runtime.scanner;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ModuleInfo;

import io.smallrye.openapi.api.OpenApiConfig;

/**
 * Wraps an {@link IndexView} instance and filters the contents based on the
 * settings provided via {@link OpenApiConfig}.
 * 
 * @author eric.wittmann@gmail.com
 */
public class FilteredIndexView implements IndexView {

    private final IndexView delegate;

    private final Pattern scanClasses;
    private final Pattern scanPackages;
    private final Pattern scanExcludeClasses;
    private final Pattern scanExcludePackages;

    /**
     * Constructor.
     * 
     * @param delegate the original (to be wrapped) index
     * @param config the config
     */
    public FilteredIndexView(IndexView delegate, OpenApiConfig config) {
        this.delegate = delegate;

        scanClasses = config.scanClasses();
        scanPackages = config.scanPackages();
        scanExcludeClasses = config.scanExcludeClasses();
        scanExcludePackages = config.scanExcludePackages();

    }

    /**
     * Returns true if the class name should be included in the index (is either included or
     * not excluded).
     * 
     * @param className the name of the class
     * @return true if the inclusion/exclusion configuration allows scanning of the class name
     */
    public boolean accepts(DotName className) {
        return accepts(className, true);
    }

    /**
     * Returns true if the class name should be included in the index, only when explicitly
     * included (and not excluded) via configuration.
     * 
     * @param className the name of the class
     * @return true if the inclusion/exclusion configuration allows scanning of the class name
     */
    public boolean explicitlyAccepts(DotName className) {
        return accepts(className, false);
    }

    /**
     * Returns true if the class name should be included in the index (is either included or
     * not excluded).
     * 
     * @param className the name of the class
     * @param allowImpliedInclusion whether the class may be implied for inclusion
     * @return true if the inclusion/exclusion configuration allows scanning of the class name
     */
    public boolean accepts(DotName className, boolean allowImpliedInclusion) {
        final boolean accept;
        final MatchHandler match = new MatchHandler(className);

        if (match.isQualifiedNameExcluded()) {
            /*
             * A FQCN or pattern that *fully* matched the FQCN was given in
             * `mp.openapi.scan.exclude.classes`.
             */
            accept = false;
        } else if (match.isQualifiedNameIncluded()) {
            /*
             * A FQCN or pattern that *fully* matched the FQCN was given in
             * `mp.openapi.scan.classes`.
             */
            accept = true;
        } else if (match.isSimpleNameExcluded()) {
            /*
             * A pattern or partial class name was given in `mp.openapi.scan.exclude.classes`
             * where the matching part of the configuration ends with the simple class name
             * *AND* no match exists for the simple class name in `mp.openapi.scan.classes`
             * with a more complete package specified.
             */
            accept = false;
        } else if (match.isSimpleNameIncluded()) {
            /*
             * A pattern or partial class name was given in `mp.openapi.scan.classes`
             * where the matching part of the configuration ends with the simple class name
             */
            accept = true;
        } else if (match.isPackageExcluded()) {
            /*
             * A package or package pattern given in `mp.openapi.scan.exclude.packages`
             * matches the start of the FQCN's package and a more complete match in
             * `mp.openapi.scan.packages` was not given.
             */
            accept = false;
        } else if (match.isPackageIncluded()) {
            /*
             * A package or package pattern given in `mp.openapi.scan.packages`
             * matches the start of the FQCN's package.
             */
            accept = true;
        } else if (allowImpliedInclusion && match.isImpliedInclusion()) {
            /*
             * No value has been specified for either `mp.openapi.scan.classes`
             * or `mp.openapi.scan.packages`.
             */
            accept = true;
        } else {
            /*
             * A value is specified for `mp.openapi.scan.classes` or `mp.openapi.scan.packages`
             * which does not match this FQCN in any way.
             */
            accept = false;
        }

        return accept;
    }

    class MatchHandler {
        final DotName className;
        final String fqcn;
        final String simpleName;
        final String packageName;

        final String classExclGroup;
        final String classInclGroup;
        final String pkgExclGroup;
        final String pkgInclGroup;

        public MatchHandler(DotName className) {
            this.className = className;
            this.fqcn = className.toString();
            this.simpleName = className.withoutPackagePrefix();
            final int index = fqcn.lastIndexOf('.');
            this.packageName = index > -1 ? fqcn.substring(0, index) : "";

            this.classExclGroup = matchingGroup(fqcn, scanExcludeClasses);
            this.classInclGroup = matchingGroup(fqcn, scanClasses);
            this.pkgExclGroup = matchingGroup(packageName, scanExcludePackages);
            this.pkgInclGroup = matchingGroup(packageName, scanPackages);
        }

        public boolean isQualifiedNameExcluded() {
            return fqcn.equals(classExclGroup);
        }

        public boolean isQualifiedNameIncluded() {
            return fqcn.equals(classInclGroup);
        }

        public boolean isSimpleNameExcluded() {
            if (classExclGroup.endsWith(simpleName)) {
                if (isSimpleNameIncluded()) {
                    return classExclGroup.length() >= classInclGroup.length();
                }
                return true;
            }
            return false;
        }

        public boolean isSimpleNameIncluded() {
            return classInclGroup.endsWith(simpleName);
        }

        public boolean isPackageExcluded() {
            if (pkgExclGroup.isEmpty()) {
                return false;
            }
            if (packageName.equals(pkgExclGroup)) {
                return true;
            }
            if (packageName.startsWith(pkgExclGroup)) {
                if (isPackageIncluded()) {
                    return (pkgExclGroup.length() >= pkgInclGroup.length());
                }
                return true;
            }
            return false;
        }

        public boolean isPackageIncluded() {
            if (pkgInclGroup.isEmpty()) {
                return false;
            }
            if (packageName.equals(pkgInclGroup)) {
                return true;
            }
            return packageName.startsWith(pkgInclGroup);
        }

        public boolean isImpliedInclusion() {
            return (scanClasses == null || scanClasses.pattern().isEmpty())
                    && (scanPackages == null || scanPackages.pattern().isEmpty());
        }
    }

    String matchingGroup(String value, Pattern pattern) {
        if (pattern == null || pattern.pattern().isEmpty() || value.isEmpty()) {
            return "";
        }
        Matcher m = pattern.matcher(value);
        return m.find() ? m.group() : "";
    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownClasses()
     */
    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return filterClasses(this.delegate.getKnownClasses());
    }

    /**
     * @see org.jboss.jandex.IndexView#getClassByName(org.jboss.jandex.DotName)
     */
    @Override
    public ClassInfo getClassByName(DotName className) {
        if (this.accepts(className)) {
            return this.delegate.getClassByName(className);
        } else {
            return null;
        }
    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownDirectSubclasses(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        return filterClasses(this.delegate.getKnownDirectSubclasses(className));
    }

    /**
     * @see org.jboss.jandex.IndexView#getAllKnownSubclasses(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        return filterClasses(this.delegate.getAllKnownSubclasses(className));
    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownDirectSubinterfaces(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
        return filterClasses(this.delegate.getKnownDirectSubinterfaces(interfaceName));
    }

    /**
     * @see org.jboss.jandex.IndexView#getAllKnownSubinterfaces(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
        return filterClasses(this.delegate.getAllKnownSubinterfaces(interfaceName));
    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownDirectImplementors(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
        return filterClasses(this.delegate.getKnownDirectImplementors(className));
    }

    /**
     * @see org.jboss.jandex.IndexView#getAllKnownImplementors(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        return filterClasses(this.delegate.getAllKnownImplementors(interfaceName));
    }

    /**
     * @see org.jboss.jandex.IndexView#getAnnotations(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        return filterInstances(this.delegate.getAnnotations(annotationName));
    }

    /**
     * @see org.jboss.jandex.IndexView#getAnnotationsWithRepeatable(org.jboss.jandex.DotName, org.jboss.jandex.IndexView)
     */
    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView annotationIndex) {
        return filterInstances(this.delegate.getAnnotationsWithRepeatable(annotationName, annotationIndex));
    }

    @Override
    public Collection<ModuleInfo> getKnownModules() {
        return delegate.getKnownModules();
    }

    @Override
    public ModuleInfo getModuleByName(DotName moduleName) {
        return delegate.getModuleByName(moduleName);
    }

    @Override
    public Collection<ClassInfo> getKnownUsers(DotName className) {
        return filterClasses(this.delegate.getKnownUsers(className));
    }

    @Override
    public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
        return filterClasses(this.delegate.getClassesInPackage(packageName));
    }

    @Override
    public Set<DotName> getSubpackages(DotName packageName) {
        return delegate.getSubpackages(packageName);
    }

    private Collection<AnnotationInstance> filterInstances(Collection<AnnotationInstance> annotations) {
        if (annotations != null && !annotations.isEmpty()) {
            return annotations.stream().filter(ai -> {
                AnnotationTarget target = ai.target();
                switch (target.kind()) {
                    case CLASS:
                        return accepts(target.asClass().name());
                    case FIELD:
                        return accepts(target.asField().declaringClass().name());
                    case METHOD:
                        return accepts(target.asMethod().declaringClass().name());
                    case METHOD_PARAMETER:
                        return accepts(target.asMethodParameter().method().declaringClass().name());
                    case TYPE:
                        // TODO properly handle filtering of "type" annotation targets
                        return true;
                    default:
                        return false;
                }
            }).collect(Collectors.toList());
        } else {
            return annotations;
        }
    }

    private Collection<ClassInfo> filterClasses(Collection<ClassInfo> classes) {
        return classes.stream()
                .filter(classInfo -> accepts(classInfo.name()))
                .collect(Collectors.toList());
    }
}
