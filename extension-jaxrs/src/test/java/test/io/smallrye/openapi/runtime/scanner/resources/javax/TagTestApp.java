package test.io.smallrye.openapi.runtime.scanner.resources.javax;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@ApplicationPath("/tags")
@OpenAPIDefinition(info = @Info(title = "Testing user-specified tag order", version = "1.0.0"), tags = {
        @Tag(name = "tag3"),
        @Tag(name = "tag1") })
public class TagTestApp extends Application {

}
