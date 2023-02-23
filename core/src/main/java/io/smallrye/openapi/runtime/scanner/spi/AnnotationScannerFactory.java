package io.smallrye.openapi.runtime.scanner.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Factory that allows plugging in more scanners.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationScannerFactory {
    private final Map<String, AnnotationScanner> loadedScanners = new HashMap<>();

    public AnnotationScannerFactory(ClassLoader cl) {
        ServiceLoader<AnnotationScanner> loader = ServiceLoader.load(AnnotationScanner.class, cl);
        Iterator<AnnotationScanner> scannerIterator = loader.iterator();
        while (scannerIterator.hasNext()) {
            AnnotationScanner scanner = scannerIterator.next();
            loadedScanners.put(scanner.getName(), scanner);
        }
    }

    public List<AnnotationScanner> getAnnotationScanners() {
        return new ArrayList<>(loadedScanners.values());
    }
}
