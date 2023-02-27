package io.smallrye.openapi.runtime.scanner;

import java.util.Collection;
import java.util.HashSet;
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

    private final Set<String> scanClasses;
    private final Set<Pattern> scanClassesPatterns;
    private final Set<String> scanPackages;
    private final Set<Pattern> scanPackagesPatterns;
    private final Set<String> scanExcludeClasses;
    private final Set<Pattern> scanExcludeClassesPatterns;
    private final Set<String> scanExcludePackages;
    private final Set<Pattern> scanExcludePackagesPatterns;
    private boolean anyIncludesConfigured;

    /**
     * Constructor.
     *
     * @param delegate the original (to be wrapped) index
     * @param config the config
     */
    public FilteredIndexView(IndexView delegate, OpenApiConfig config) {
        this.delegate = delegate;

        scanClasses = new HashSet<>();
        scanClassesPatterns = new HashSet<>();
        processConfigStrings(config.scanClasses(), scanClasses, scanClassesPatterns);

        scanPackages = new HashSet<>();
        scanPackagesPatterns = new HashSet<>();
        processConfigStrings(config.scanPackages(), scanPackages, scanPackagesPatterns);

        scanExcludeClasses = new HashSet<>();
        scanExcludeClassesPatterns = new HashSet<>();
        processConfigStrings(config.scanExcludeClasses(), scanExcludeClasses, scanExcludeClassesPatterns);

        scanExcludePackages = new HashSet<>();
        scanExcludePackagesPatterns = new HashSet<>();
        processConfigStrings(config.scanExcludePackages(), scanExcludePackages, scanExcludePackagesPatterns);

        anyIncludesConfigured = !scanClasses.isEmpty() || !scanClassesPatterns.isEmpty() || !scanPackages.isEmpty()
                || !scanPackagesPatterns.isEmpty();
    }

    private static void processConfigStrings(Set<String> inputs, Set<String> strings, Set<Pattern> patterns) {
        for (String input : inputs) {
            if (input.startsWith("^") || input.endsWith("$")) {
                patterns.add(Pattern.compile(input));
            } else {
                strings.add(input);
            }
        }
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

        String fqcn = className.toString();
        String simpleName = className.withoutPackagePrefix();
        int index = fqcn.lastIndexOf('.');
        String packageName = index > -1 ? fqcn.substring(0, index) : "";

        // Check for an exact class name match in the exclude list
        if (scanExcludeClasses.contains(fqcn)) {
            return false;
        }

        // Check for an exact class name match in the include list
        if (scanClasses.contains(fqcn)) {
            return true;
        }

        // Find the longest entry from the class exclude list which is a suffix of the fqcn and includes the full simple class name
        String simpleNameExcludeMatch = longestSuffixMatch(fqcn, scanExcludeClasses);
        if (!simpleNameExcludeMatch.endsWith(simpleName)) {
            simpleNameExcludeMatch = "";
        }
        // Find the longest regex match from the class exclude list
        simpleNameExcludeMatch = longest(simpleNameExcludeMatch, longestRegexMatch(fqcn, scanExcludeClassesPatterns));

        // Find the longest entry from the class include list which is a suffix of the fqcn and includes the full simple class name
        String simpleNameIncludeMatch = longestSuffixMatch(fqcn, scanClasses);
        if (!simpleNameIncludeMatch.endsWith(simpleName)) {
            simpleNameIncludeMatch = "";
        }
        // Find the longest regex match from the class include list
        simpleNameIncludeMatch = longest(simpleNameIncludeMatch, longestRegexMatch(fqcn, scanClassesPatterns));

        if (simpleNameExcludeMatch.length() > 0 && simpleNameExcludeMatch.length() >= simpleNameIncludeMatch.length()) {
            // There is an exclude match and it's more complete than any include match
            return false;
        }

        if (simpleNameIncludeMatch.length() > 0) {
            // There is an include match
            return true;
        }

        // Find the longest string prefix match or regex match from the include package list
        String packageIncludeMatch = longest(longestPrefixMatch(packageName, scanPackages),
                longestRegexMatch(packageName, scanPackagesPatterns));

        // Find the longest string prefix match or regex match from the exclude package list
        String packageExcludeMatch = longest(longestPrefixMatch(packageName, scanExcludePackages),
                longestRegexMatch(packageName, scanExcludePackagesPatterns));

        if (packageExcludeMatch.length() > 0 && packageExcludeMatch.length() >= packageIncludeMatch.length()) {
            // There is a package exclude match and it's more complete than any include match
            return false;
        }

        if (packageIncludeMatch.length() > 0) {
            // There is a package include match
            return true;
        }

        if (allowImpliedInclusion && !anyIncludesConfigured) {
            return true;
        }

        return false;
    }

    /**
     * Find the longest string from {@code prefixes} which is a prefix of {@code name}
     *
     * @param name the name
     * @param prefixes a set of potential prefixes of {@code name}
     * @return the longest element of {@code prefixes} which is a prefix of {@code name}, or the empty string if there are none
     */
    private static String longestPrefixMatch(String name, Set<String> prefixes) {
        String longestPrefix = "";
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                if (prefix.length() > longestPrefix.length()) {
                    longestPrefix = prefix;
                }
            }
        }
        return longestPrefix;
    }

    /**
     * Find the longest string from {@code suffixes} which is a suffix of {@code name}
     *
     * @param name the name
     * @param suffixes a set of potential suffixes of {@code name}
     * @return the longest element of {@code suffixes} which is a suffix of {@code name}, or the empty string if there are none
     */
    private static String longestSuffixMatch(String name, Set<String> suffixes) {
        String longestSuffix = "";
        for (String suffix : suffixes) {
            if (name.endsWith(suffix)) {
                if (suffix.length() > longestSuffix.length()) {
                    longestSuffix = suffix;
                }
            }
        }
        return longestSuffix;
    }

    /**
     * Attempts to find each element of {@code patterns} in {@code name} and returns the longest match
     *
     * @param name the name to match against
     * @param patterns the patterns to try
     * @return the longest result returned by {@link Matcher#group()} after successfully finding a pattern in {@code name}, or
     *         the empty string if no patterns matched
     */
    private static String longestRegexMatch(String name, Set<Pattern> patterns) {
        String longestMatch = "";
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                String match = m.group();
                if (match.length() > longestMatch.length()) {
                    longestMatch = match;
                }
            }
        }
        return longestMatch;
    }

    /**
     * Returns the longest argument
     *
     * @param strings an array of strings
     * @return the longest element of {@code strings}
     */
    private static String longest(String... strings) {
        String longest = "";
        for (String string : strings) {
            if (string.length() > longest.length()) {
                longest = string;
            }
        }
        return longest;
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
