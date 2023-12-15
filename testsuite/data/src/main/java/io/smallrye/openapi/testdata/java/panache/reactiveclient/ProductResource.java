package io.smallrye.openapi.testdata.java.panache.reactiveclient;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/product")
@RequestScoped
public class ProductResource {

    @APIResponse(responseCode = "200", description = "A-OK", content = @Content(schema = @Schema(implementation = Product.class)))
    @APIResponse(responseCode = "410", description = "All Gone")
    @Produces(MediaType.APPLICATION_JSON)
    @Retention(RetentionPolicy.RUNTIME)
    @interface GetProductOperation {

    }

    @GET
    @GetProductOperation
    @Operation(description = "Get a t-shirt :-) ")
    public Response get() {
        var product = new Product();
        product.setName("Quarkus T-Shirt");

        return Response.ok(product).build();
    }
}
