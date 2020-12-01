package io.smallrye.openapi.runtime.io;

import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;

/**
 * A simple registry to hold the current scanner info
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CurrentScannerInfo {
    private static final ThreadLocal<CurrentScannerInfo> current = new ThreadLocal<>();

    public static void register(AnnotationScanner annotationScanner) {
        CurrentScannerInfo registry = new CurrentScannerInfo(annotationScanner);
        current.set(registry);
    }

    public static AnnotationScanner getCurrentAnnotationScanner() {
        CurrentScannerInfo info = current.get();
        return info != null ? info.annotationScanner : null;
    }

    public static void setCurrentConsumes(final String[] currentConsumes) {
        current.get().currentConsumes = currentConsumes;
    }

    public static String[] getCurrentConsumes() {
        return current.get().currentConsumes;
    }

    public static void setCurrentProduces(final String[] currentProduces) {
        current.get().currentProduces = currentProduces;
    }

    public static String[] getCurrentProduces() {
        return current.get().currentProduces;
    }

    public static void remove() {
        current.remove();
    }

    public static boolean isWrapperType(Type type) {
        AnnotationScanner scanner = getCurrentAnnotationScanner();
        return scanner != null && scanner.isWrapperType(type);
    }

    public static boolean isScannerInternalResponse(Type type) {
        AnnotationScanner scanner = getCurrentAnnotationScanner();
        return scanner != null && scanner.isScannerInternalResponse(type);
    }

    private String[] currentConsumes;
    private String[] currentProduces;
    private final AnnotationScanner annotationScanner;

    private CurrentScannerInfo(final AnnotationScanner annotationScanner) {
        this.annotationScanner = annotationScanner;
        this.currentConsumes = null;
        this.currentProduces = null;
    }
}