package io.smallrye.openapi.runtime.io;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;

/**
 * A simple registry to hold the current scanner info
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CurrentScannerInfo {
    private static final ThreadLocal<CurrentScannerInfo> current = new ThreadLocal<>();

    public static void register(AnnotationScanner annotationScanner, final String[] currentConsumes,
            final String[] currentProduces) {
        CurrentScannerInfo registry = new CurrentScannerInfo(annotationScanner, currentConsumes, currentProduces);
        current.set(registry);
    }

    public static AnnotationScanner getCurrentAnnotationScanner() {
        return current.get().annotationScanner;
    }

    public static String[] getCurrentConsumes() {
        return current.get().currentConsumes;
    }

    public static String[] getCurrentProduces() {
        return current.get().currentProduces;
    }

    public static void remove() {
        current.remove();
    }

    private final String[] currentConsumes;
    private final String[] currentProduces;
    private final AnnotationScanner annotationScanner;

    private CurrentScannerInfo(final AnnotationScanner annotationScanner, final String[] currentConsumes,
            final String[] currentProduces) {
        this.annotationScanner = annotationScanner;
        this.currentConsumes = currentConsumes;
        this.currentProduces = currentProduces;
    }
}