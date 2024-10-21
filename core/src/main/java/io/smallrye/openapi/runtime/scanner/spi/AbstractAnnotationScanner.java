package io.smallrye.openapi.runtime.scanner.spi;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Abstract base class for annotation scanners
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractAnnotationScanner implements AnnotationScanner {
    private static final String EMPTY = "";

    private static final Set<DotName> PRIMITIVE_OBJECTS = new HashSet<>();
    private static final Set<DotName> STREAM_OBJECTS = new HashSet<>();

    static {
        PRIMITIVE_OBJECTS.add(DotName.createSimple(String.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Integer.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Short.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Long.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Float.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Double.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Boolean.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(Character.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(BigDecimal.class));
        PRIMITIVE_OBJECTS.add(DotName.createSimple(BigInteger.class));

        STREAM_OBJECTS.add(DotName.createSimple(File.class));
        STREAM_OBJECTS.add(DotName.createSimple(Path.class));
        STREAM_OBJECTS.add(DotName.createSimple(InputStream.class));
        STREAM_OBJECTS.add(DotName.createSimple(Reader.class));
        STREAM_OBJECTS.add(DotName.createSimple(Byte.class));
        STREAM_OBJECTS.add(DotName.createSimple(byte[].class));
        STREAM_OBJECTS.add(DotName.createSimple("io.vertx.core.file.AsyncFile"));
        STREAM_OBJECTS.add(DotName.createSimple("io.vertx.core.buffer.Buffer"));

    }

    protected AnnotationScannerContext context;
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
        Set<String> profiles = Extensions.getProfiles(extensible);
        Extensions.removeProfiles(extensible);
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

    @Override
    public String[] getDefaultConsumes(AnnotationScannerContext context, MethodInfo methodInfo,
            final ResourceParameters params) {
        Type requestBodyType = getRequestBodyParameterClassType(context, methodInfo, params);

        if (requestBodyType != null) {
            if (isStreaming(requestBodyType)) {
                return context.getConfig().getDefaultStreamingConsumes()
                        .orElseGet(ContentIO::defaultMediaTypes);
            } else if (isPrimimive(requestBodyType)) {
                return context.getConfig().getDefaultPrimitivesConsumes()
                        .orElseGet(ContentIO::defaultMediaTypes);
            }
            return context.getConfig().getDefaultConsumes().orElseGet(ContentIO::defaultMediaTypes);
        }
        return new String[] {};
    }

    @Override
    public String[] getDefaultProduces(AnnotationScannerContext context, MethodInfo methodInfo) {
        if (isStreaming(methodInfo.returnType())) {
            return context.getConfig().getDefaultStreamingProduces().orElseGet(ContentIO::defaultMediaTypes);
        } else if (isPrimimive(methodInfo.returnType())) {
            return context.getConfig().getDefaultPrimitivesProduces().orElseGet(ContentIO::defaultMediaTypes);
        }
        return context.getConfig().getDefaultProduces().orElseGet(ContentIO::defaultMediaTypes);
    }

    private boolean isPrimimive(Type type) {
        if (type != null) {
            return type.kind().equals(Type.Kind.PRIMITIVE)
                    || PRIMITIVE_OBJECTS.contains(type.name())
                    || (isWrapperType(type) && isPrimimive(unwrapType(type)))
                    || (TypeUtil.isWrappedType(type) && isPrimimive(TypeUtil.unwrapType(type)));
        }
        return false;
    }

    private boolean isStreaming(Type type) {
        if (type != null) {
            return (type.kind().equals(Type.Kind.PRIMITIVE) && type.name().equals(DotName.createSimple(byte.class)))
                    || STREAM_OBJECTS.contains(type.name())
                    || (isWrapperType(type) && isStreaming(unwrapType(type)))
                    || (TypeUtil.isWrappedType(type) && isStreaming(TypeUtil.unwrapType(type)));
        }
        return false;
    }
}
