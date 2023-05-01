package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import test.io.smallrye.openapi.runtime.scanner.jakarta.Message;
import test.io.smallrye.openapi.runtime.scanner.jakarta.Result;

class ArrayItemRefTest extends IndexScannerTestBase {

    @Test
    @SuppressWarnings("unused")
    void testArrayItemsReferenceRegisteredTypes() throws IOException, JSONException {
        class Foo {
        }

        class Bar {
        }

        @Schema(type = SchemaType.STRING, format = "entity-id", implementation = String.class)
        class ID<T> {
            Long id;
            Class<T> entityClazz;
        }

        class Generics {
            Set<ID<Foo>> fooSet;
            List<ID<Foo>> fooList;
            Set<ID<Bar>> barSet;
            List<ID<Bar>> barList;
        }

        @Path(value = "/v1/generics")
        @Produces(value = MediaType.APPLICATION_JSON)
        @Consumes(value = MediaType.APPLICATION_JSON)
        class GenericsResource {

            @POST
            @RequestBody(content = @Content(schema = @Schema(implementation = Generics.class)))
            public Result<Generics> post(Generics deployment) {
                return null;
            }
        }

        Index i = indexOf(
                Foo.class,
                ID.class,
                Generics.class,
                GenericsResource.class,
                Result.class,
                Message.class,
                Set.class,
                List.class);

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.array-item-ref.json", result);
    }
}
