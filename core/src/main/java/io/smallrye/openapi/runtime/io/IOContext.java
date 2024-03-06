package io.smallrye.openapi.runtime.io;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class IOContext<V, A extends V, O extends V, AB, OB> {

    private AnnotationScannerContext scannerContext;
    private final JsonIO<V, A, O, AB, OB> jsonIO;

    public static <V, A extends V, O extends V, AB, OB> IOContext<V, A, O, AB, OB> forJson(JsonIO<V, A, O, AB, OB> jsonIO) {
        return new IOContext<>(null, jsonIO);
    }

    public static IOContext<?, ?, ?, ?, ?> forScanning(AnnotationScannerContext context) { // NOSONAR
        return new IOContext<>(context, null);
    }

    public IOContext(AnnotationScannerContext context, JsonIO<V, A, O, AB, OB> jsonIO) {
        this.scannerContext = context;
        this.jsonIO = jsonIO;
    }

    public AnnotationScannerContext scannerContext() {
        return scannerContext;
    }

    public void scannerContext(AnnotationScannerContext scannerContext) {
        this.scannerContext = scannerContext;
    }

    public JsonIO<V, A, O, AB, OB> jsonIO() {
        return jsonIO;
    }
}
