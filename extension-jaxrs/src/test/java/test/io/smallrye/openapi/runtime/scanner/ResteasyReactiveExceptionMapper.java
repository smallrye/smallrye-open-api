package test.io.smallrye.openapi.runtime.scanner;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

// Mimic org.jboss.resteasy.reactive.server.spi.ResteasyReactiveExceptionMapper

public interface ResteasyReactiveExceptionMapper<E extends Throwable> extends ExceptionMapper<E> {

    Response toResponse(E exception, Object context);

}
