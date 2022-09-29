package test.io.smallrye.openapi.runtime.scanner.jakarta;

import java.util.List;

import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;

public class ParameterOrderResource {

    public static final Class<?>[] CLASSES = {
            Application1.class,
            Resource1.class
    };

    @OpenAPIDefinition(info = @Info(title = "Parameter Order", version = "1.0.0"), components = @Components(parameters = {
            @Parameter(name = "namespace", schema = @Schema(type = SchemaType.STRING)),
            @Parameter(name = "collection", schema = @Schema(type = SchemaType.STRING)),
            @Parameter(name = "where", schema = @Schema(type = SchemaType.OBJECT)),
            @Parameter(name = "fields", schema = @Schema(implementation = String[].class)),
            @Parameter(name = "page-state", schema = @Schema(type = SchemaType.BOOLEAN)),
            @Parameter(name = "profile", schema = @Schema(type = SchemaType.BOOLEAN)),
            @Parameter(name = "raw", schema = @Schema(type = SchemaType.BOOLEAN))
    }))
    static class Application1 extends Application {
    }

    @Path("/1")
    static class Resource1 {
        @Parameters(value = {
                @Parameter(ref = "namespace"),
                @Parameter(name = "collection", ref = "collection"),
                @Parameter(name = "where", ref = "where"),
                @Parameter(ref = "fields"),
                @Parameter(name = "page-size", in = ParameterIn.QUERY, description = "The max number of results to return.", schema = @Schema(implementation = Integer.class, defaultValue = "3", minimum = "1", maximum = "20")),
                @Parameter(name = "page-state", ref = "page-state"),
                @Parameter(name = "profile", ref = "profile")
                // `raw` parameter omitted, should appear last anyway due to default ordering
        })
        @GET
        public String get(
                // Order purposefully different than parameters listed in `@Parameters` on method
                @Parameter(ref = "raw") @QueryParam("raw") boolean raw,
                @QueryParam("collection") String collection,
                @QueryParam("fields") List<String> fields,
                @QueryParam("namespace") String namespace,
                @QueryParam("page-size") int pageSize,
                @QueryParam("page-state") boolean pageState,
                @QueryParam("profile") boolean profile,
                @QueryParam("where") JsonObject where) {
            return null;
        }
    }
}
