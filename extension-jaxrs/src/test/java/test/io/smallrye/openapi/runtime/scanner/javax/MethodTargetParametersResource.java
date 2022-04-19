package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(value = "/policies")
@Produces(value = "application/json")
@Consumes(value = "application/json")
@RequestScoped
public class MethodTargetParametersResource {

    public static class PagedResponse<T> {

        public Map<String, Long> meta = new HashMap<>(1);
        public Map<String, String> links = new HashMap<>(3);
        public List<T> data = new ArrayList<>();
    }

    @Operation(summary = "Return all policies for a given account")
    @GET
    @Path(value = "/")
    @Parameters(value = {
            @Parameter(name = "offset", in = ParameterIn.QUERY, description = "Page number, starts 0, if not specified uses 0.", schema = @Schema(type = SchemaType.INTEGER)),
            @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Number of items per page, if not specified uses 10. "
                    + "NO_LIMIT can be used to specify an unlimited page, when specified it ignores the offset", schema = @Schema(type = SchemaType.INTEGER)),
            @Parameter(name = "sortColumn", in = ParameterIn.QUERY, description = "Column to sort the results by", schema = @Schema(type = SchemaType.STRING, enumeration = {
                    "name", "description", "is_enabled", "mtime" })),
            @Parameter(name = "sortDirection", in = ParameterIn.QUERY, description = "Sort direction used", schema = @Schema(type = SchemaType.STRING, enumeration = {
                    "asc", "desc" })),
            @Parameter(name = "filter[name]", in = ParameterIn.QUERY, description = "Filtering policies by the name depending on the Filter operator used.", schema = @Schema(type = SchemaType.STRING)),
            @Parameter(name = "filter:op[name]", in = ParameterIn.QUERY, description = "Operations used with the filter", schema = @Schema(type = SchemaType.STRING, enumeration = {
                    "equal", "like", "ilike", "not_equal" }, defaultValue = "equal")),
            @Parameter(name = "filter[description]", in = ParameterIn.QUERY, description = "Filtering policies by the description depending on the Filter operator used.", schema = @Schema(type = SchemaType.STRING)),
            @Parameter(name = "filter:op[description]", in = ParameterIn.QUERY, description = "Operations used with the filter", schema = @Schema(type = SchemaType.STRING, enumeration = {
                    "equal", "like", "ilike", "not_equal" }, defaultValue = "equal")),
            @Parameter(name = "filter[is_enabled]", in = ParameterIn.QUERY, description = "Filtering policies by the is_enabled field."
                    + "Defaults to true if no operand is given.", schema = @Schema(type = SchemaType.STRING, defaultValue = "true", enumeration = {
                            "true", "false" })) })
    @APIResponse(responseCode = "400", description = "Bad parameter for sorting was passed")
    @APIResponse(responseCode = "404", description = "No policies found for customer")
    @APIResponse(responseCode = "403", description = "Individual permissions missing to complete action")
    @APIResponse(responseCode = "200", description = "Policies found", content = @Content(schema = @Schema(implementation = PagedResponse.class, name = "ignored")), headers = @Header(name = "TotalCount", description = "Total number of items found", schema = @Schema(type = SchemaType.INTEGER)))
    public Response getPoliciesForCustomer() {
        return null;
    }

}
