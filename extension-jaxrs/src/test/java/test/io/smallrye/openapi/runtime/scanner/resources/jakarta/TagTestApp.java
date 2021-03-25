package test.io.smallrye.openapi.runtime.scanner.resources.jakarta;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/tags")
@OpenAPIDefinition(info = @Info(title = "Testing user-specified tag order", version = "1.0.0"), tags = {
        @Tag(name = "tag3"),
        @Tag(name = "tag1") })
public class TagTestApp extends Application {

}
