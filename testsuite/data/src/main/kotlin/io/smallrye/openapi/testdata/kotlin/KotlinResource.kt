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
    fun hello(): Flow<String> {
        return flow {
            Foobar("Hello")
        }
    }
}

data class Foobar(val data: String)