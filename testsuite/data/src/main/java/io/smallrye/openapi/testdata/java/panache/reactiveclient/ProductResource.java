package io.smallrye.openapi.testdata.java.panache.reactiveclient;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
