package io.smallrye.openapi.runtime;

/**
 * <code>RuntimeException</code> to be thrown when fatal exceptions are detected
 * in the Open API read/scan processes.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 *
 */
public class OpenApiRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 8472532911884999427L;

    public OpenApiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenApiRuntimeException(String message) {
        super(message);
    }

    public OpenApiRuntimeException(Throwable cause) {
        super(cause);
    }
}
