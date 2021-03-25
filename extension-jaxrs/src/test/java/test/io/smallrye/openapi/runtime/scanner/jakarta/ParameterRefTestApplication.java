package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(info = @Info(title = "title", version = "1"), components = @Components(parameters = {
        @Parameter(name = "queryParam1", in = ParameterIn.QUERY),
        @Parameter(name = "pathParam2", in = ParameterIn.PATH, description = "`pathParam2` with info in components") }))
public class ParameterRefTestApplication extends Application {

}
