package test.io.smallrye.openapi.runtime.scanner.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

/**
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@OpenAPIDefinition(info = @Info(title = "Title from the application", version = "1.0"))
public class PackageInfoTestApplication extends Application {
    @Path("package-info-test")
    public static class PackageInfoTestResource {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getValue() {
            return "";
        }
    }
}
