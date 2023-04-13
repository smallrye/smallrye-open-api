package io.smallrye.openapi.testdata.java.panache.reactiveclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/get")
public interface VisibleHttpClient {

    @GET
    String get();
}
