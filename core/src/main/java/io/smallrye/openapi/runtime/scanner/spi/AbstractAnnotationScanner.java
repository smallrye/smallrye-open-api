package io.smallrye.openapi.runtime.scanner.spi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Abstract base class for annotation scanners
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractAnnotationScanner implements AnnotationScanner {
    private static final String EMPTY = "";

    protected String currentAppPath = EMPTY;
    private String contextRoot = EMPTY;

    @Override
    public void setContextRoot(String path) {
        this.contextRoot = path;
    }

    protected String makePath(String operationPath) {
        return createPathFromSegments(this.contextRoot, this.currentAppPath, operationPath);
    }

    /**
     * Make a path out of a number of path segments.
     *
     * @param segments String paths
     * @return Path built from the segments
     */
    protected static String createPathFromSegments(String... segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.startsWith("/")) {
                segment = segment.substring(1);
            }
            if (segment.endsWith("/")) {
                segment = segment.substring(0, segment.length() - 1);
            }
            if (segment.isEmpty()) {
                continue;
            }
            builder.append("/");
            builder.append(segment);
        }
        String rval = builder.toString();
        if (rval.isEmpty()) {
            return "/";
        }
        return rval;
    }

    /**
     * Checks if the given extensible contains profiles, and if the extensible should be included in the final openapi document.
     * Any extension containing a profile is removed from the extensible.
     * inclusion is then calculated based on all collected profiles.
     *
     * @param config current config
     * @param extensible the extensible to check for profiles
     * @return true, if the given extensible should be included in the final openapi document, otherwise false
     */
    protected static boolean processProfiles(OpenApiConfig config, Extensible<?> extensible) {

        Set<String> profiles = new HashSet<>();
        Map<String, Object> extensions = extensible.getExtensions();
        if (extensions != null && !extensions.isEmpty()) {
            extensions = new HashMap<>(extensions);

            for (String name : extensions.keySet()) {
                if (!name.startsWith(OpenApiConstants.EXTENSION_PROFILE_PREFIX)) {
                    continue;
                }

                String profile = name.substring(OpenApiConstants.EXTENSION_PROFILE_PREFIX.length());
                profiles.add(profile);
                extensible.removeExtension(name);
            }
        }

        return profileIncluded(config, profiles);
    }

    private static boolean profileIncluded(OpenApiConfig config, Set<String> profiles) {
        if (!config.getScanExcludeProfiles().isEmpty()) {
            return config.getScanExcludeProfiles().stream().noneMatch(profiles::contains);
        }

        if (config.getScanProfiles().isEmpty()) {
            return true;
        }

        return config.getScanProfiles().stream().anyMatch(profiles::contains);
    }

    public String[] getDefaultConsumes(AnnotationScannerContext context, MethodInfo methodInfo) {
        return context.getConfig().getDefaultConsumes().orElseGet(OpenApiConstants.DEFAULT_MEDIA_TYPES);
    }

    public String[] getDefaultProduces(AnnotationScannerContext context, MethodInfo methodInfo) {
        if (isPrimimive(methodInfo.returnType())) {
            return context.getConfig().getDefaultPrimitivesProduces().orElseGet(OpenApiConstants.DEFAULT_MEDIA_TYPES);
        }
        return context.getConfig().getDefaultProduces().orElseGet(OpenApiConstants.DEFAULT_MEDIA_TYPES);
    }

    private boolean isPrimimive(Type type) {
        return type.kind().equals(Type.Kind.PRIMITIVE)
                || type.name().equals(DotName.createSimple(String.class))
                || (TypeUtil.isWrappedType(type) && isPrimimive(TypeUtil.unwrapType(type)));
    }
}
