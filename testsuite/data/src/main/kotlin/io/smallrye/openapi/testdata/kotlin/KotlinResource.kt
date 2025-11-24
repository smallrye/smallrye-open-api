package io.smallrye.openapi.testdata.kotlin

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jboss.resteasy.reactive.RestStreamElementType

@Path("kotlin")
class KotlinResource {
    @Path("hello")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    fun hello(
      @org.jboss.resteasy.reactive.RestQuery
      p1: String,             // required = true; nullable = false
      @org.jboss.resteasy.reactive.RestQuery
      p2: String = "default", // required = false; nullable = false
      @org.jboss.resteasy.reactive.RestQuery
      p3: String?,            // required = true; nullable = true
      @org.jboss.resteasy.reactive.RestQuery
      p4: String? = null,     // required = false; nullable = true
    ): Flow<String> {
        return flow {
            Foobar("Hello")
        }
    }
}

data class Foobar(val data: String)
