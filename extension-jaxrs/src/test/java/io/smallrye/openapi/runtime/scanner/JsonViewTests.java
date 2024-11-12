package io.smallrye.openapi.runtime.scanner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

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

    @Test
    void testJsonViewsWithIgnoredProperties() throws Exception {
        class Views {
            class Max extends Full {
            }

            class Full extends Ingest {
            }

            class Ingest extends Abridged {
            }

            class Abridged {
            }
        }

        @Schema(name = "Role")
        class Role {
            @JsonView(Views.Full.class)
            private UUID id;
            @JsonView(Views.Ingest.class)
            private String name;
            @Schema(title = "Title of description")
            @JsonView(Views.Full.class)
            private String description;
        }

        @Schema(name = "Group")
        class Group {
            @JsonView(Views.Full.class)
            private UUID id;
            @JsonView(Views.Abridged.class)
            private String name;
            @JsonView(Views.Full.class)
            private String description;
            @JsonView(Views.Ingest.class)
            private String roleId;
            @JsonView(Views.Abridged.class)
            @JsonIgnoreProperties("description")
            private List<Role> roles;
        }

        @Schema(name = "User")
        class User {
            @JsonView(Views.Full.class)
            private UUID id;

            @JsonView(Views.Ingest.class)
            private String name;

            @JsonView(Views.Ingest.class)
            private String groupId;

            @JsonView(Views.Abridged.class)
            @Schema(type = SchemaType.STRING, format = "date-time", description = "test date-time field")
            private LocalDateTime birthday;

            @JsonView(Views.Full.class)
            @Schema
            private Group group;
        }

        @Path("/user")
        class UserResource {
            @GET
            @Produces(MediaType.APPLICATION_JSON)
            @JsonView(Views.Full.class)
            @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
            public Response get() {
                return null;
            }

            @POST
            public Response post(@RequestBody @JsonView(Views.Ingest.class) User group) {
                return null;
            }
        }

        @Path("/role")
        class RoleResource {
            @GET
            @Produces(MediaType.APPLICATION_JSON)
            @JsonView(Views.Full.class)
            @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Role.class)))
            public Response getRole() {
                return null;
            }

            @POST
            public Response post(@RequestBody @JsonView(Views.Ingest.class) Role role) {
                return null;
            }
        }

        @Path("/group")
        class GroupResource {
            @GET
            @Produces(MediaType.APPLICATION_JSON)
            @JsonView(Views.Full.class)
            @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Group.class)))
            public Response get() {
                return null;
            }

            @POST
            public Response post(@RequestBody @JsonView(Views.Ingest.class) Group group) {
                return null;
            }
        }

        Index index = Index.of(Views.class, Views.Max.class, Views.Full.class, Views.Ingest.class, Views.Abridged.class,
                User.class, Group.class, Role.class, UserResource.class, LocalDateTime.class, RoleResource.class,
                GroupResource.class);
        OpenApiConfig config = dynamicConfig(SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_SCHEMAS, Boolean.TRUE);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        OpenApiDocument document = OpenApiDocument.newInstance();
        document.reset();
        document.config(config);
        document.modelFromAnnotations(scanner.scan());
        document.initialize();

        OpenAPI result = document.get();
        printToConsole(result);
        assertJsonEquals("special.jsonview-with-ignored.json", result);
    }
}
