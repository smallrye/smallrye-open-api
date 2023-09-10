package io.smallrye.openapi.runtime.scanner.spi;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Factory that allows plugging in more scanners.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationScannerFactory implements Supplier<Iterable<AnnotationScanner>> {

    /**
     * List of AnnotationScanners discovered via the ServiceLoader, ordered by
     * {@linkplain AnnotationScanner#getName() name}
     */
    private final List<AnnotationScanner> loadedScanners;

    public AnnotationScannerFactory(ClassLoader loader) {
        Iterable<AnnotationScanner> scanners = ServiceLoader.load(AnnotationScanner.class, loader);
        loadedScanners = StreamSupport.stream(scanners.spliterator(), false)
                .sorted(comparing(AnnotationScanner::getName, nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public List<AnnotationScanner> getAnnotationScanners() {
        return new ArrayList<>(loadedScanners);
    }

    @Override
    public Iterable<AnnotationScanner> get() {
        return getAnnotationScanners();
    }

}
