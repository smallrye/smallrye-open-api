package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

// Mimic org.jboss.resteasy.reactive.server.spi.ResteasyReactiveExceptionMapper

public interface ResteasyReactiveExceptionMapper<E extends Throwable> extends ExceptionMapper<E> {

    Response toResponse(E exception, Object context);

}
