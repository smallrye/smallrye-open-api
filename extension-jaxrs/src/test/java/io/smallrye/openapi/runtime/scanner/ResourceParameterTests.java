package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.json.bind.annotation.JsonbTransient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.json.JSONException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.smallrye.openapi.api.OpenApiConfig;
import test.io.smallrye.openapi.runtime.scanner.resources.ParameterResource;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ResourceParameterTests extends JaxRsDataObjectScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #25.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/25
     *
     */
    @Test
    public void testParameterResource() throws IOException, JSONException {
        Index i = indexOf(ParameterResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.simpleSchema.json", result);
    }

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #165.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/165
     *
     */
    @Test
    public void testPrimitiveArraySchema() throws IOException, JSONException {
        Index i = indexOf(PrimitiveArraySchemaTestResource.class,
                PrimitiveArraySchemaTestResource.PrimitiveArrayTestObject.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-schema.json", result);
    }

    @Path("/v1")
    static class PrimitiveArraySchemaTestResource {
        @Schema(name = "PrimitiveArrayTestObject", description = "the REST response class")
        static class PrimitiveArrayTestObject {
            @Schema(required = true, description = "a packed data array")
            private double[] data;

            @Schema(implementation = double.class, type = SchemaType.ARRAY)
            // Type is intentionally different than annotation implementation type
            private float[] data2;
        }

        @GET
        @Operation(summary = "Get an object containing a primitive array")
        @APIResponses({
                @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrimitiveArrayTestObject.class))) })
        public PrimitiveArrayTestObject getResponse() {
            return new PrimitiveArrayTestObject();
        }
    }

    /*************************************************************************/

    @Test
    public void testPrimitiveArrayParameter() throws IOException, JSONException {
        Index i = indexOf(PrimitiveArrayParameterTestResource.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-param.json", result);
    }

    @Path("/v1")
    static class PrimitiveArrayParameterTestResource {
        @POST
        @Consumes("application/json")
        @Produces("application/json")
        @Operation(summary = "Convert an array of doubles to an array of floats")
        @APIResponses({
                @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = float[].class))) })
        public float[] doubleToFloat(@SuppressWarnings("unused") double[] input) {
            return new float[0];
        }
    }

    /*************************************************************************/

    @Test
    public void testPrimitiveArrayPolymorphism() throws IOException, JSONException {
        Index i = indexOf(PrimitiveArrayPolymorphismTestResource.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-polymorphism.json", result);
    }

    @Path("/v1")
    static class PrimitiveArrayPolymorphismTestResource {
        @POST
        @Consumes("application/json")
        @Produces("application/json")
        @Operation(summary = "Convert an array of integer types to an array of floating point types")
        @RequestBody(content = @Content(schema = @Schema(anyOf = { int[].class, long[].class })))
        @APIResponses({
                @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                        float[].class, double[].class }))) })
        public Object intToFloat(@SuppressWarnings("unused") Object input) {
            return null;
        }
    }

    /*************************************************************************/

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #201.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/201
     *
     */
    @Test
    public void testSchemaImplementationType() throws IOException, JSONException {
        Index i = indexOf(SchemaImplementationTypeResource.class,
                SchemaImplementationTypeResource.GreetingMessage.class,
                SchemaImplementationTypeResource.SimpleString.class);

        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.string-implementation-wrapped.json", result);
    }

    @Path("/hello")
    static class SchemaImplementationTypeResource {
        static class GreetingMessage {
            @Schema(description = "Used to send a message")
            private final SimpleString message;

            @Schema(implementation = String.class, description = "Simply a string", required = false)
            private SimpleString optionalMessage;

            public GreetingMessage(@JsonProperty SimpleString message) {
                this.message = message;
            }

            public SimpleString getMessage() {
                return message;
            }

            public SimpleString getOptionalMessage() {
                return optionalMessage;
            }
        }

        @Schema(implementation = String.class, title = "A Simple String")
        static class SimpleString {
            @Schema(hidden = true)
            private final String value;

            public SimpleString(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }

        @SuppressWarnings("unused")
        @POST
        @Consumes("application/json")
        @Produces("application/json")
        public Response doPost(GreetingMessage message) {
            return Response.created(URI.create("http://example.com")).build();
        }
    }

    /*************************************************************************/

    /*
     * Test case derived for Smallrye OpenAPI issue #233.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/233
     *
     */
    @Test
    public void testTimeResource() throws IOException, JSONException {
        Index i = indexOf(TimeTestResource.class, TimeTestResource.UTC.class, LocalTime.class, OffsetTime.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.time.json", result);
    }

    @Path("/times")
    @Produces(MediaType.TEXT_PLAIN)
    static class TimeTestResource {

        static class UTC {
            @Schema(description = "Current time at offset '00:00'")
            OffsetTime utc = OffsetTime.now(ZoneId.of("UTC"));
        }

        @Path("local")
        @GET
        public LocalTime getLocalTime() {
            return LocalTime.now();
        }

        @Path("zoned")
        @GET
        public OffsetTime getZonedTime(@QueryParam("zoneId") String zoneId) {
            return OffsetTime.now(ZoneId.of(zoneId));
        }

        @Path("utc")
        @GET
        public UTC getUTC() {
            return new UTC();
        }

        @Path("utc")
        @POST
        public OffsetTime toUTC(@QueryParam("local") LocalTime local, @QueryParam("offsetId") String offsetId) {
            return OffsetTime.of(local, ZoneOffset.of(offsetId));
        }
    }

    /*************************************************************************/

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #237.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/237
     *
     */
    @Test
    public void testTypeVariableResponse() throws IOException, JSONException {
        Index i = indexOf(TypeVariableResponseTestResource.class,
                TypeVariableResponseTestResource.Dto.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.type-variable.json", result);
    }

    @Path("/variable-types")
    @SuppressWarnings("unused")
    static class TypeVariableResponseTestResource<TEST extends TypeVariableResponseTestResource.Dto> {
        static class Dto {
            String id;
        }

        @GET
        public List<TEST> getAll() {
            return null;
        }

        @GET
        @Path("{id}")
        public TEST getOne(@PathParam("id") String id) {
            return null;
        }
    }

    /*************************************************************************/

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #248.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/248
     *
     */
    @Test
    public void testResponseTypeUnindexed() throws IOException, JSONException {
        // Index is intentionally missing ResponseTypeUnindexedTestResource$ThirdPartyType
        Index i = indexOf(ResponseTypeUnindexedTestResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.unknown-type.empty-schema.json", result);
    }

    @Path("/unindexed")
    static class ResponseTypeUnindexedTestResource {
        // This type will not be in the Jandex index, nor does it implement Map or List.
        static class ThirdPartyType {
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public ThirdPartyType hello() {
            return null;
        }
    }

    /*************************************************************************/

    /*
     * Test cases derived from original example in SmallRye OpenAPI issue #260.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/260
     *
     */
    @Test
    public void testGenericSetResponseWithSetIndexed() throws IOException, JSONException {
        Index i = indexOf(FruitResource.class, Fruit.class, Seed.class, Set.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.generic-collection.set-indexed.json", result);
    }

    @Test
    public void testGenericSetResponseWithSetUnindexed() throws IOException, JSONException {
        Index i = indexOf(FruitResource.class, Fruit.class, Seed.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.generic-collection.set-unindexed.json", result);
    }

    @Path("/fruits")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unused")
    static class FruitResource {
        @GET
        public Set<Fruit> list() {
            return null;
        }

        @POST
        public Set<Fruit> add(Fruit fruit) {
            return null;
        }

        @DELETE
        public Set<Fruit> delete(Fruit fruit) {
            return null;
        }
    }

    static class Fruit {
        String description;
        String name;
        List<Seed> seeds;
    }

    static class Seed {

    }

    /*************************************************************************/

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #239.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/239
     *
     */
    @Test
    public void testBeanParamMultipartFormInheritance() throws IOException, JSONException {
        Index i = indexOf(BeanParamMultipartFormInheritanceResource.class,
                MultipartFormVerify.class,
                MultipartFormUploadIconForm.class,
                BeanParamBase.class,
                BeanParamImpl.class,
                BeanParamAddon.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("params.beanparam-multipartform-inherited.json", result);
    }

    static class MultipartFormVerify {
        @FormParam("token")
        public String token;
        @FormParam("os")
        public String os;
    }

    static class MultipartFormUploadIconForm extends MultipartFormVerify {
        @FormParam("icon")
        public byte[] icon;
    }

    static class BeanParamBase implements BeanParamAddon {
        @QueryParam("qc1")
        String qc1;

        @Override
        public void setHeaderParam1(String value) {
        }
    }

    static interface BeanParamAddon {
        @HeaderParam("hi1")
        void setHeaderParam1(String value);
    }

    static class BeanParamImpl extends BeanParamBase implements BeanParamAddon {
        @CookieParam("cc1")
        String cc1;
    }

    @Path("/")
    static class BeanParamMultipartFormInheritanceResource {
        @POST
        @Path("/uploadIcon")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response uploadUserAvatar(@MultipartForm MultipartFormUploadIconForm form) {
            return null;
        }

        @GET
        @Path("/beanparambase")
        public Response getWithBeanParams(@BeanParam BeanParamBase params) {
            return null;
        }

        @GET
        @Path("/beanparamimpl")
        public Response getWithBeanParams(@BeanParam BeanParamImpl params) {
            return null;
        }
    }

    /*************************************************************************/
    /*
     * Test case derived from original example in SmallRye OpenAPI issue #330.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/330
     *
     */

    @Test
    public void testMethodTargetParametersWithoutJAXRS() throws IOException, JSONException {
        Index i = indexOf(MethodTargetParametersResource.class,
                MethodTargetParametersResource.PagedResponse.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("params.method-target-nojaxrs.json", result);
    }

    @Path("/policies")
    @Produces("application/json")
    @Consumes("application/json")
    @RequestScoped
    static class MethodTargetParametersResource {
        static class PagedResponse<T> {
            public Map<String, Long> meta = new HashMap<>(1);
            public Map<String, String> links = new HashMap<>(3);
            public List<T> data = new ArrayList<>();
        }

        @Operation(summary = "Return all policies for a given account")
        @GET
        @Path("/")
        @Parameters({
                @Parameter(name = "offset", in = ParameterIn.QUERY, description = "Page number, starts 0, if not specified uses 0.", schema = @Schema(type = SchemaType.INTEGER)),
                @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Number of items per page, if not specified uses 10. "
                        + "NO_LIMIT can be used to specify an unlimited page, when specified it ignores the offset", schema = @Schema(type = SchemaType.INTEGER)),
                @Parameter(name = "sortColumn", in = ParameterIn.QUERY, description = "Column to sort the results by", schema = @Schema(type = SchemaType.STRING, enumeration = {
                        "name",
                        "description",
                        "is_enabled",
                        "mtime"
                })),
                @Parameter(name = "sortDirection", in = ParameterIn.QUERY, description = "Sort direction used", schema = @Schema(type = SchemaType.STRING, enumeration = {
                        "asc",
                        "desc"
                })),
                @Parameter(name = "filter[name]", in = ParameterIn.QUERY, description = "Filtering policies by the name depending on the Filter operator used.", schema = @Schema(type = SchemaType.STRING)),
                @Parameter(name = "filter:op[name]", in = ParameterIn.QUERY, description = "Operations used with the filter", schema = @Schema(type = SchemaType.STRING, enumeration = {
                        "equal",
                        "like",
                        "ilike",
                        "not_equal"
                }, defaultValue = "equal")),
                @Parameter(name = "filter[description]", in = ParameterIn.QUERY, description = "Filtering policies by the description depending on the Filter operator used.", schema = @Schema(type = SchemaType.STRING)),
                @Parameter(name = "filter:op[description]", in = ParameterIn.QUERY, description = "Operations used with the filter", schema = @Schema(type = SchemaType.STRING, enumeration = {
                        "equal",
                        "like",
                        "ilike",
                        "not_equal"
                }, defaultValue = "equal")),
                @Parameter(name = "filter[is_enabled]", in = ParameterIn.QUERY, description = "Filtering policies by the is_enabled field."
                        +
                        "Defaults to true if no operand is given.", schema = @Schema(type = SchemaType.STRING, defaultValue = "true", enumeration = {
                                "true", "false" })),
        })
        @APIResponse(responseCode = "400", description = "Bad parameter for sorting was passed")
        @APIResponse(responseCode = "404", description = "No policies found for customer")
        @APIResponse(responseCode = "403", description = "Individual permissions missing to complete action")
        @APIResponse(responseCode = "200", description = "Policies found", content = @Content(schema = @Schema(implementation = PagedResponse.class, name = "ignored")), headers = @Header(name = "TotalCount", description = "Total number of items found", schema = @Schema(type = SchemaType.INTEGER)))
        public Response getPoliciesForCustomer() {
            return null;
        }
    }

    /*************************************************************************/
    /*
     * Test case derived from original example in SmallRye OpenAPI issue #437.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/437
     *
     */

    @Test
    public void testJsonbTransientOnSetterGeneratesReadOnly() throws IOException, JSONException {
        Index i = indexOf(Policy437Resource.class, Policy437.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.hidden-setter-readonly-props.json", result);
    }

    @Path("/")
    static class Policy437Resource {
        @GET
        @Path("/beanparamimpl")
        public Policy437 getWithBeanParams() {
            return null;
        }
    }

    static class Policy437 {

        // The ID will be created by code.
        public UUID id;

        @JsonbTransient
        public String customerid;

        @NotNull
        @NotEmpty
        @Schema(description = "Name of the rule. Must be unique per customer account.")
        @Size(max = 150)
        public String name;

        @Schema(description = "A short description of the policy.")
        public String description;

        public boolean isEnabled;

        @Schema(description = "Condition string.", example = "arch = \"x86_64\"")
        @NotEmpty
        @NotNull
        public String conditions;

        @Schema(description = "String describing actions separated by ';' when the policy is evaluated to true." +
                "Allowed values are 'email' and 'webhook'")
        public String actions;

        @Schema(type = SchemaType.STRING, description = "Last update time in a form like '2020-01-24 12:19:56.718', output only", readOnly = true, format = "yyyy-MM-dd hh:mm:ss.ddd", implementation = String.class)
        private Timestamp mtime = new Timestamp(System.currentTimeMillis());

        @Schema(type = SchemaType.STRING, description = "Create time in a form like '2020-01-24 12:19:56.718', output only", readOnly = true, format = "yyyy-MM-dd hh:mm:ss.ddd", implementation = String.class)
        private Timestamp ctime = new Timestamp(System.currentTimeMillis());

        private long lastTriggered;

        @JsonbTransient
        public void setMtime(String mtime) {
            this.mtime = Timestamp.valueOf(mtime);
        }

        public void setMtimeToNow() {
            this.mtime = new Timestamp(System.currentTimeMillis());
        }

        public String getMtime() {
            return mtime.toString();
        }

        @JsonbTransient
        public void setLastTriggered(long tTime) {
            lastTriggered = tTime;
        }

        public long getLastTriggered() {
            return lastTriggered;
        }

        @JsonbTransient
        public void setCtime(String ctime) {
            this.ctime = Timestamp.valueOf(ctime);
        }

        public String getCtime() {
            return ctime.toString();
        }

        public UUID store(String customer, Policy437 policy) {
            if (!customer.equals(policy.customerid)) {
                throw new IllegalArgumentException("Store: customer id do not match");
            }
            return id;
        }

        public void delete(Policy437 policy) {
        }

        public void populateFrom(Policy437 policy) {
            this.id = policy.id;
            this.name = policy.name;
            this.description = policy.description;
            this.actions = policy.actions;
            this.conditions = policy.conditions;
            this.isEnabled = policy.isEnabled;
            this.customerid = policy.customerid;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Policy{");
            sb.append("id=").append(id);
            sb.append(", customerid='").append(customerid).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append(", mtime=").append(mtime);
            sb.append('}');
            return sb.toString();
        }

        enum SortableColumn {
            NAME("name"),
            DESCRIPTION("description"),
            IS_ENABLED("is_enabled"),
            MTIME("mtime");

            private final String name;

            SortableColumn(final String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public static SortableColumn fromName(String columnName) {
                for (SortableColumn column : SortableColumn.values()) {
                    if (column.getName().equals(columnName)) {
                        return column;
                    }
                }
                throw new IllegalArgumentException("Unknown Policy.SortableColumn requested: [" + columnName + "]");
            }

        }

        enum FilterableColumn {
            NAME("name"),
            DESCRIPTION("description"),
            IS_ENABLED("is_enabled");

            private final String name;

            FilterableColumn(final String name) {
                this.name = name;
            }

            public static FilterableColumn fromName(String columnName) {
                Optional<FilterableColumn> result = Arrays.stream(FilterableColumn.values())
                        .filter(val -> val.name.equals(columnName))
                        .findAny();
                if (result.isPresent()) {
                    return result.get();
                }
                throw new IllegalArgumentException("Unknown Policy.FilterableColumn requested: [" + columnName + "]");
            }
        }
    }

}
