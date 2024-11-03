package io.smallrye.openapi.runtime.scanner;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.SmallRyeOASConfig;

class JsonViewTests extends IndexScannerTestBase {

    @Test
    void testJsonViewSchemasPresent() throws Exception {
        class Views {
            class Public {
            }

            class Internal extends Public {
            }

            class WriteOnly extends Public {
            }
        }

        @Schema(name = "Inner2")
        class InnerBean2 {
            @Schema
            String value;
        }

        @Schema(name = "Inner1")
        class InnerBean1 {
            @Schema
            String value;
            @Schema(ref = "Inner2")
            InnerBean2 inner2;
        }

        @Schema(name = "BeanName")
        @com.fasterxml.jackson.annotation.JsonView(Views.Internal.class) // class default is internal
        class Bean {
            String id;
            @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
            String name;
            @com.fasterxml.jackson.annotation.JsonView(Views.WriteOnly.class)
            String secret;
            @Schema
            @com.fasterxml.jackson.annotation.JsonView()
            InnerBean1 inner1;
        }

        @jakarta.ws.rs.Path("/item/{id}")
        class TestResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("internal")
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @com.fasterxml.jackson.annotation.JsonView(Views.Internal.class)
            public Bean getInternal() {
                return null;
            }

            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("public")
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
            public java.util.concurrent.CompletionStage<Bean> getPublic() {
                return null;
            }

            @jakarta.ws.rs.POST
            @jakarta.ws.rs.Path("public")
            @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
            @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
            public java.util.concurrent.CompletionStage<Bean> updatePublic(
                    @com.fasterxml.jackson.annotation.JsonView(Views.WriteOnly.class) Bean modified) {
                return null;
            }
        }

        Index index = Index.of(Views.Public.class, Views.WriteOnly.class, Views.Internal.class, Bean.class, InnerBean1.class,
                InnerBean2.class, TestResource.class);
        OpenApiConfig config = dynamicConfig(SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_SCHEMAS, Boolean.TRUE);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        OpenApiDocument document = OpenApiDocument.newInstance();
        document.reset();
        document.config(config);
        document.modelFromAnnotations(scanner.scan());
        document.initialize();

        OpenAPI result = document.get();
        printToConsole(result);
        assertJsonEquals("special.jsonview-schemas-basic.json", result);
    }
}
