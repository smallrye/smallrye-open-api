package io.smallrye.openapi.runtime.scanner.dataobject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScannerTest.BVTestContainer;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class BeanValidationResourceTest extends IndexScannerTestBase {

    @Test
    void testBeanValidationDocument() throws IOException, JSONException {
        Index index = indexOf(BVTestResource.class, BVTestResourceEntity.class, BeanValidationScannerTest.BVTestContainer.class,
                TestEnum.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("dataobject/resource.testBeanValidationDocument.json", result);
    }

    @Test
    void testInheritedBVConstraints() throws IOException, JSONException {
        Index index = indexOf(User.class, BaseUser.class, UserImpl.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("dataobject/schema.inherited-bv-constraints.json", result);
    }

    /**********************************************************************/

    @Path("/bv")
    static class BVTestResource {
        @SuppressWarnings("unused")
        @Path("/test-container")
        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        @Tag(name = "Test", description = "Testing the container")
        public BVTestContainer getTestContainer(BVTestResourceEntity parameter) {
            return new BVTestContainer();
        }
    }

    static class BVTestResourceEntity {
        @Size(min = 5, max = 100)
        @NotNull
        @Schema(minLength = 10, maxLength = 101, nullable = true, name = "string_no_bean_constraints", required = false)
        @JsonbProperty("string_no_bean_constraints")
        private String stringIgnoreBvContraints;

        @Size(min = 1, max = 2000)
        @Digits(integer = 100, fraction = 100)
        @NotNull
        @Schema(minimum = "101", maximum = "101.999", nullable = true, pattern = "^\\d{1,3}([.]\\d{1,3})?$", name = "big_int_no_bean_constraints", required = false)
        @JsonbProperty("big_int_no_bean_constraints")
        private BigInteger bIntegerIgnoreBvContraints;

        @NotNull
        @NotEmpty
        @Size(max = 200)
        @Schema(minItems = 0, maxItems = 100, nullable = true, name = "list_no_bean_constraints", required = false)
        @JsonbProperty("list_no_bean_constraints")
        private List<String> listIgnoreBvContraints;

        @NotNull
        @NotEmpty
        @Size(max = 200)
        @Schema(minProperties = 0, maxProperties = 100, nullable = true, name = "map_no_bean_constraints", required = false)
        @JsonbProperty("map_no_bean_constraints")
        private Map<String, String> mapIgnoreBvContraints;

        @NotNull
        TestEnum enumValue;

    }

    @Schema
    enum TestEnum {
        ABC,
        DEF
    }

    interface User {
        @Positive
        @Max(9999)
        Integer getId();
    }

    static abstract class BaseUser {
        @Min(10)
        @NotNull
        protected Integer id;
    }

    @Schema
    static class UserImpl extends BaseUser implements User {
        @Schema(description = "The user identifier", minimum = "15")
        public Integer getId() {
            return 0;
        }
    }
}
