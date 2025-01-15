package io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.PathItem;
import org.eclipse.microprofile.openapi.annotations.PathItemOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.util.UnusedSchemaFilter;
import io.smallrye.openapi.runtime.OpenApiProcessor;

class CallbackScanTests extends IndexScannerTestBase {

    @Test
    void testCallbackReference() throws Exception {
        @OpenAPIDefinition(info = @Info(title = "Greetings", version = "0.0.1"), components = @Components(callbacks = @Callback(name = "myEvent", callbackUrlExpression = "{$request.body#/callbackUrl}", operations = @CallbackOperation(method = "POST", responses = {
                @APIResponse(responseCode = "2XX", description = "Ok"),
                @APIResponse(responseCode = "5XX", description = "Failed"),
        }))))
        class App extends Application {

        }

        @Path("/")
        class Resource {
            @GET
            @Produces(MediaType.TEXT_PLAIN)
            @Callback(ref = "#/components/callbacks/myEvent")
            public String hello() {
                return "goodbye";
            }
        }

        Index index = indexOf(App.class, Resource.class);
        OpenAPI result = OpenApiProcessor.bootstrap(dynamicConfig(OASConfig.FILTER, UnusedSchemaFilter.class.getName()), index);
        printToConsole(result);
        assertJsonEquals("callback.reference-component-callback.json", result);
    }

    @Test
    void testCallbackPathItemReference() throws Exception {
        @OpenAPIDefinition(info = @Info(title = "Greetings", version = "0.0.1"), components = @Components(pathItems = @PathItem(name = "myPathItem", operations = @PathItemOperation(method = "POST", responses = {
                @APIResponse(responseCode = "2XX", description = "Ok"),
                @APIResponse(responseCode = "5XX", description = "Failed"),
        }))))
        class App extends Application {
        }

        @Path("/")
        class Resource {
            @GET
            @Produces(MediaType.TEXT_PLAIN)
            @Callback(name = "cb1", callbackUrlExpression = "/test1", pathItemRef = "myPathItem")
            @Callback(name = "cb2", callbackUrlExpression = "/test2", pathItemRef = "#/components/pathItems/myPathItem")
            public String hello() {
                return "goodbye";
            }
        }

        Index index = indexOf(App.class, Resource.class);
        OpenAPI result = OpenApiProcessor.bootstrap(dynamicConfig(OASConfig.FILTER, UnusedSchemaFilter.class.getName()), index);
        printToConsole(result);
        assertJsonEquals("callback.reference-component-pathitem.json", result);
    }
}
