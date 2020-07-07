package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

public class GenericModelTypesResourceTest extends IndexScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #25.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/25
     *
     */
    @Test
    public void testGenericsApplication() throws IOException, JSONException {
        Index i = indexOf(BaseModel.class,
                BaseResource.class,
                KingCrimson.class,
                KingCrimsonResource.class,
                Magma.class,
                MagmaResource.class,
                Message.class,
                OpenAPIConfig.class,
                Residents.class,
                ResidentsResource.class,
                Result.class,
                ResultList.class,
                List.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.generic-model-types.json", result);
    }

    @OpenAPIDefinition(tags = {
            @Tag(name = "Test", description = "Cristian") }, info = @Info(title = "API - Service", version = "V1"), security = @SecurityRequirement(name = "API-Key"), components = @Components(securitySchemes = {
                    @SecurityScheme(securitySchemeName = "API-Key", type = SecuritySchemeType.APIKEY, apiKeyName = "API-Key", in = SecuritySchemeIn.HEADER) }))
    static class OpenAPIConfig extends Application {
    }

    static abstract class BaseModel {

        protected UUID id;
        @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[.SSS]X")
        @Schema(implementation = String.class, format = "date-time")
        protected Date lastUpdate;

        public BaseModel() {
        }

        public BaseModel(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID uuid) {
            this.id = uuid;
        }

        public void setId(String uuid) {
            this.id = UUID.fromString(uuid);
        }

        public Date getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(Date lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " [id=" + id + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BaseModel other = (BaseModel) obj;
            if (id == null && other.id != null) {
                return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }
    }

    static class KingCrimson extends BaseModel {
        public enum Status {
            unknown,
            success,
            failure
        }

        @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[.SSS]X")
        @Schema(implementation = String.class, format = "date-time")
        private Date timestamp;
        private Magma environment;
        private Status status;

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public Magma getEnvironment() {
            return environment;
        }

        public void setEnvironment(Magma environment) {
            this.environment = environment;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

    }

    static class Magma extends BaseModel {

        private String codename;
        private String tier;
        private KingCrimson deployment;

        public Magma() {
        }

        public Magma(UUID id) {
            super(id);
        }

        public String getCodename() {
            return codename;
        }

        public void setCodename(String codename) {
            this.codename = codename;
        }

        public String getTier() {
            return tier;
        }

        public void setTier(String tier) {
            this.tier = tier;
        }

        public KingCrimson getDeployment() {
            return deployment;
        }

        public void setDeployment(KingCrimson deployment) {
            this.deployment = deployment;
        }

    }

    static class Residents extends BaseModel {

        private String foo;
        private String bar;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }
    }

    static class Message {
        private String message;
        private String description;

        public Message() {
        }

        public Message(String message) {
            this.message = message;
        }

        public Message(String message, String description) {
            this.message = message;
            this.description = description;
        }

        public String getMessage() {
            return message;
        }

        public String getDescription() {
            return description;
        }

    }

    static class Result<T extends BaseModel> {
        private T result;
        private Message error;
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public Message getError() {
            return error;
        }

        public T getResult() {
            return result;
        }

        public static class ResultBuilder<T extends BaseModel> {
            private Integer status;
            private Message error = new Message();
            private T result;

            public ResultBuilder<T> status(Integer status) {
                this.status = status;
                return this;
            }

            public ResultBuilder<T> error(String message) {
                this.error = new Message(message);
                return this;
            }

            public ResultBuilder<T> error(String message, String description) {
                this.error = new Message(message, description);
                return this;
            }

            public ResultBuilder<T> result(T result) {
                this.result = result;
                return this;
            }

            public Result<T> build() {
                Result<T> response = new Result<T>();
                response.status = this.status;
                response.error = this.error;
                response.result = this.result;
                return response;
            }
        }
    }

    static class ResultList<T extends BaseModel> {
        private List<T> result;
        private Message error;
        private Integer status;

        public List<T> getResult() {
            return result;
        }

        public Message getError() {
            return error;
        }

        public Integer getStatus() {
            return status;
        }

        public static class ResultBuilder<T extends BaseModel> {
            private Integer status;
            private Message error = new Message();
            private List<T> result;

            public ResultList.ResultBuilder<T> status(Integer status) {
                this.status = status;
                return this;
            }

            public ResultList.ResultBuilder<T> error(String message) {
                this.error = new Message(message);
                return this;
            }

            public ResultList.ResultBuilder<T> error(String message, String description) {
                this.error = new Message(message, description);
                return this;
            }

            public ResultList.ResultBuilder<T> result(List<T> result) {
                this.result = result;
                return this;
            }

            public ResultList<T> build() {
                ResultList<T> response = new ResultList<T>();
                response.status = this.status;
                response.error = this.error;
                response.result = this.result;
                return response;
            }
        }
    }

    static abstract class BaseResource<T extends BaseModel> {

        protected ResultList<T> getAll1() {
            return new ResultList.ResultBuilder<T>()
                    .status(200).build();
        }

        protected Result<T> post1(T t) {
            return new Result.ResultBuilder<T>()
                    .status(200).build();
        }

        protected Result<T> put1(T e) {
            return new Result.ResultBuilder<T>()
                    .status(200).build();
        }

        protected Response delete1(T t) {
            return Response.status(Response.Status.NO_CONTENT)
                    .build();
        }
    }

    @Path("/v1/kingcrimson")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    static class KingCrimsonResource extends BaseResource<KingCrimson> {

        @GET
        public ResultList<KingCrimson> getAll(@QueryParam("id") String id,
                @QueryParam("limit") int limit,
                @QueryParam("offset") int offset,
                @QueryParam("orderby") List<String> orderBy) {
            return super.getAll1();
        }

        @POST
        @RequestBody(content = @Content(schema = @Schema(implementation = KingCrimson.class)))
        public Result<KingCrimson> post(KingCrimson deployment) {
            return super.post1(deployment);
        }

        @PUT
        @RequestBody(content = @Content(schema = @Schema(implementation = KingCrimson.class)))
        public Result<KingCrimson> put(KingCrimson deployment) {
            return super.put1(deployment);
        }

        @DELETE
        @RequestBody(content = @Content(schema = @Schema(implementation = KingCrimson.class)))
        public Response delete(KingCrimson deployment) {
            return super.delete1(deployment);
        }
    }

    @Path("/v1/magma")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    static class MagmaResource extends BaseResource<Magma> {

        @GET
        public ResultList<Magma> getAll(@QueryParam("id") String id,
                @QueryParam("limit") int limit,
                @QueryParam("offset") int offset,
                @QueryParam("orderby") List<String> orderBy) {

            return super.getAll1();
        }

        @POST
        @RequestBody(content = @Content(schema = @Schema(implementation = Magma.class)))
        public Result<Magma> post(Magma environment) {
            return super.post1(environment);
        }

        @PUT
        @RequestBody(content = @Content(schema = @Schema(implementation = Magma.class)))
        public Result<Magma> put(Magma environment) {
            return super.put1(environment);
        }

        @DELETE
        @RequestBody(content = @Content(schema = @Schema(implementation = Magma.class)))
        public Response delete(Magma environment) {
            return super.delete1(environment);
        }
    }

    @Path("/v1/residents")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    static class ResidentsResource extends BaseResource<Residents> {

        @GET
        public ResultList<Residents> getAll(@QueryParam("id") String id,
                @QueryParam("limit") int limit,
                @QueryParam("offset") int offset,
                @QueryParam("orderby") List<String> orderBy) {

            return super.getAll1();
        }

        @POST
        @RequestBody(content = @Content(schema = @Schema(implementation = Residents.class)))
        @APIResponse(responseCode = "200", description = "Creates a new Chart")
        public Result<Residents> post(Residents chart) {
            return super.post1(chart);
        }

        @PUT
        @RequestBody(content = @Content(schema = @Schema(implementation = Residents.class)))
        @APIResponse(responseCode = "200", description = "Creates a new Chart")
        public Result<Residents> put(Residents chart) {
            return super.put1(chart);
        }

        @DELETE
        @RequestBody(content = @Content(schema = @Schema(implementation = Residents.class)))
        public Response delete(Residents chart) {
            return super.delete1(chart);
        }
    }

}
