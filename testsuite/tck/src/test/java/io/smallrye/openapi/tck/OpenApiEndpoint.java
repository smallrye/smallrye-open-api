package io.smallrye.openapi.tck;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.stream.Stream;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

import io.smallrye.openapi.runtime.io.Format;

@Path("/openapi")
public class OpenApiEndpoint {

    @Context
    ServletContext servletContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(hidden = true)
    public Response openApi(@QueryParam("format") final String formatParam) throws Exception {
        final Format format = getOpenApiFormat(formatParam);

        return Response.ok(servletContext.getAttribute("OpenAPI." + format.name()))
                .type(format.getMimeType())
                .build();
    }

    private Format getOpenApiFormat(String format) {
        return Stream.of(Format.values())
                .filter(f -> format != null && f.name().compareToIgnoreCase(format) == 0)
                .findFirst()
                .orElse(httpHeaders.getAcceptableMediaTypes().contains(APPLICATION_JSON_TYPE) ? Format.JSON : Format.YAML);
    }

}
