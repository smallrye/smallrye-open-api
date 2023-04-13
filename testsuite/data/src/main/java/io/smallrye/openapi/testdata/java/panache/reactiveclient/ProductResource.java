package io.smallrye.openapi.testdata.java.panache.reactiveclient;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/product")
@RequestScoped
public class ProductResource {

    @GET
    public Product get() {
        var product = new Product();
        product.setName("Quarkus T-Shirt");

        return product;
    }
}
