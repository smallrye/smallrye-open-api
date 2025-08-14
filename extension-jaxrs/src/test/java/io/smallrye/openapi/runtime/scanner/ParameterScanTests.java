package io.smallrye.openapi.runtime.scanner;

import static io.smallrye.openapi.api.constants.OpenApiConstants.DUPLICATE_OPERATION_ID_BEHAVIOR;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.model.Extensions;
import test.io.smallrye.openapi.runtime.scanner.Widget;
import test.io.smallrye.openapi.runtime.scanner.jakarta.MultipleContentTypesWithFormParamsTestResource;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class ParameterScanTests extends IndexScannerTestBase {

    public static void verifyMethodAndParamRefsPresent(OpenAPI oai) {
        if (oai.getPaths() != null && oai.getPaths().getPathItems() != null) {
            for (Map.Entry<String, PathItem> pathItemEntry : oai.getPaths().getPathItems().entrySet()) {
                final PathItem pathItem = pathItemEntry.getValue();
                if (pathItem.getOperations() != null) {
                    for (var operationEntry : pathItem.getOperations().entrySet()) {
                        var operation = operationEntry.getValue();
                        String opRef = operationEntry.getKey() + " " + pathItemEntry.getKey();
                        Assertions.assertNotNull(Extensions.getMethodRef(operation), "methodRef: " + opRef);
                        if (operation.getParameters() != null) {
                            for (var parameter : operation.getParameters()) {
                                /*
                                 * if @Parameter style=matrix was not specified at the same @Path segment
                                 * a synthetic parameter is created which cannot be mapped to a field or method parameter
                                 */
                                if (!isPathMatrixObject(parameter)) {
                                    // in all other cases paramRef should be set
                                    String pRef = opRef + ", " + parameter.getIn() + ": " + parameter.getName();
                                    Assertions.assertNotNull(Extensions.getParamRef(parameter), "paramRef: " + pRef);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isPathMatrixObject(org.eclipse.microprofile.openapi.models.parameters.Parameter parameter) {
        return parameter.getIn() == org.eclipse.microprofile.openapi.models.parameters.Parameter.In.PATH
                && parameter.getStyle() == org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.MATRIX
                && parameter.getSchema() != null && parameter.getSchema().getType() != null
                && parameter.getSchema().getType().equals(
                        Collections.singletonList(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.OBJECT));
    }

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
        verifyMethodAndParamRefsPresent(result);
    }

    private static void test(OpenApiConfig config, String expectedResource, Class<?>... classes)
            throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
        verifyMethodAndParamRefsPresent(result);
    }

    @Test
    void testJavaxIgnoredMpOpenApiHeaders() throws IOException, JSONException {
        test("params.ignored-mp-openapi-headers.json",
                test.io.smallrye.openapi.runtime.scanner.javax.IgnoredMpOpenApiHeaderArgsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaIgnoredMpOpenApiHeaders() throws IOException, JSONException {
        test("params.ignored-mp-openapi-headers.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.IgnoredMpOpenApiHeaderArgsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxParameterOnMethod() throws IOException, JSONException {
        test("params.parameter-on-method.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterOnMethodTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaParameterOnMethod() throws IOException, JSONException {
        test("params.parameter-on-method.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterOnMethodTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxParameterOnField() throws IOException, JSONException {
        test("params.parameter-on-field.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ResourcePathParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaParameterOnField() throws IOException, JSONException {
        test("params.parameter-on-field.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResourcePathParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxParameterInBeanFromField() throws IOException, JSONException {
        test("params.parameter-in-bean-from-field.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterInBeanFromFieldTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterInBeanFromFieldTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaParameterInBeanFromField() throws IOException, JSONException {
        test("params.parameter-in-bean-from-field.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterInBeanFromFieldTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterInBeanFromFieldTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxParameterInBeanFromSetter() throws IOException, JSONException {
        test("params.parameter-in-bean-from-setter.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterInBeanFromSetterTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterInBeanFromSetterTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaParameterInBeanFromSetter() throws IOException, JSONException {
        test("params.parameter-in-bean-from-setter.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterInBeanFromSetterTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterInBeanFromSetterTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxPathParamWithFormParams() throws IOException, JSONException {
        test("params.path-param-with-form-params.json",
                test.io.smallrye.openapi.runtime.scanner.javax.PathParamWithFormParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaPathParamWithFormParams() throws IOException, JSONException {
        test("params.path-param-with-form-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.PathParamWithFormParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxMultipleContentTypesWithFormParams() throws IOException, JSONException {
        test("params.multiple-content-types-with-form-params.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MultipleContentTypesWithFormParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaMultipleContentTypesWithFormParams() throws IOException, JSONException {
        test("params.multiple-content-types-with-form-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipleContentTypesWithFormParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testFailOnDuplicateOperationIds() {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> test(
                        dynamicConfig(DUPLICATE_OPERATION_ID_BEHAVIOR, OpenApiConfig.DuplicateOperationIdBehavior.FAIL.name()),
                        "params.multiple-content-types-with-form-params.json",
                        MultipleContentTypesWithFormParamsTestResource.class,
                        Widget.class));
        assertStartsWith(exception.getMessage(), "SROAP07950: Duplicate operationId:", "Exception message");
    }

    private static void assertStartsWith(String actual, String expectedStart, String description) {
        final boolean condition = actual != null && actual.startsWith(expectedStart);
        if (!condition) {
            Assertions
                    .fail(String.format("%s is expected to start with: <%s> but was <%s>", description, expectedStart, actual));
        }
    }

    @Test
    void testJavaxParametersInConstructor() throws IOException, JSONException {
        test("params.parameters-in-constructor.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ParametersInConstructorTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ParametersInConstructorTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaParametersInConstructor() throws IOException, JSONException {
        test("params.parameters-in-constructor.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParametersInConstructorTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParametersInConstructorTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxMatrixParamsOnResourceMethodArgs() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-args.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MatrixParamsOnResourceMethodArgsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaMatrixParamsOnResourceMethodArgs() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-args.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MatrixParamsOnResourceMethodArgsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxMatrixParamsOnResourceMethodCustomName() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-custom-name.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MatrixParamsOnResourceMethodCustomNameTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaMatrixParamsOnResourceMethodCustomName() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-custom-name.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MatrixParamsOnResourceMethodCustomNameTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxMatrixParamsOnMethodAndFieldArgs() throws IOException, JSONException {
        test("params.matrix-params-on-method-and-field-args.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MatrixParamsOnMethodAndFieldArgsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaMatrixParamsOnMethodAndFieldArgs() throws IOException, JSONException {
        test("params.matrix-params-on-method-and-field-args.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MatrixParamsOnMethodAndFieldArgsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxAllTheParams() throws IOException, JSONException {
        test("params.all-the-params.json", test.io.smallrye.openapi.runtime.scanner.javax.AllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
        test("params.all-the-params.json",
                test.io.smallrye.openapi.runtime.scanner.javax.RestEasyReactiveAllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.RestEasyReactiveAllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaAllTheParams() throws IOException, JSONException {
        test("params.all-the-params.json", test.io.smallrye.openapi.runtime.scanner.jakarta.AllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
        test("params.all-the-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactiveAllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactiveAllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxMultipartForm() throws IOException, JSONException {
        test("params.multipart-form.json", test.io.smallrye.openapi.runtime.scanner.javax.MultipartFormTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.MultipartFormTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class, InputStream.class);
    }

    @Test
    void testJakartaMultipartForm() throws IOException, JSONException {
        test("params.multipart-form.json", test.io.smallrye.openapi.runtime.scanner.jakarta.MultipartFormTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipartFormTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class, InputStream.class);
    }

    @Test
    void testJavaxEnumQueryParam() throws IOException, JSONException {
        test("params.enum-form-param.json", test.io.smallrye.openapi.runtime.scanner.javax.EnumQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.EnumQueryParamTestResource.TestEnum.class,
                test.io.smallrye.openapi.runtime.scanner.javax.EnumQueryParamTestResource.TestEnumWithSchema.class);
    }

    @Test
    void testJakartaEnumQueryParam() throws IOException, JSONException {
        test("params.enum-form-param.json", test.io.smallrye.openapi.runtime.scanner.jakarta.EnumQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.EnumQueryParamTestResource.TestEnum.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.EnumQueryParamTestResource.TestEnumWithSchema.class);
    }

    @Test
    void testJavaxUUIDQueryParam() throws IOException, JSONException {
        test("params.uuid-params-responses.json",
                test.io.smallrye.openapi.runtime.scanner.javax.UUIDQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.UUIDQueryParamTestResource.WrappedUUID.class);
    }

    @Test
    void testJakartaUUIDQueryParam() throws IOException, JSONException {
        test("params.uuid-params-responses.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.UUIDQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.UUIDQueryParamTestResource.WrappedUUID.class);
    }

    @Test
    void testJavaxRestEasyFieldsAndSetters() throws IOException, JSONException {
        test("params.resteasy-fields-and-setters.json",
                test.io.smallrye.openapi.runtime.scanner.javax.RestEasyFieldsAndSettersTestResource.class);
    }

    @Test
    void testJakartaRestEasyFieldsAndSetters() throws IOException, JSONException {
        test("params.resteasy-fields-and-setters.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyFieldsAndSettersTestResource.class);
    }

    @Test
    void testJavaxCharSequenceArrayParam() throws IOException, JSONException {
        test("params.char-sequence-arrays.json",
                test.io.smallrye.openapi.runtime.scanner.javax.CharSequenceArrayParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.CharSequenceArrayParamTestResource.EchoResult.class);
    }

    @Test
    void testJakartaCharSequenceArrayParam() throws IOException, JSONException {
        test("params.char-sequence-arrays.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.CharSequenceArrayParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.CharSequenceArrayParamTestResource.EchoResult.class);
    }

    @Test
    void testJavaxOptionalParam() throws IOException, JSONException {
        test("params.optional-types.json", test.io.smallrye.openapi.runtime.scanner.javax.OptionalParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.OptionalParamTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.javax.OptionalParamTestResource.NestedBean.class,
                test.io.smallrye.openapi.runtime.scanner.javax.OptionalParamTestResource.OptionalWrapper.class, Optional.class,
                OptionalDouble.class, OptionalLong.class);
    }

    @Test
    void testJakartaOptionalParam() throws IOException, JSONException {
        test("params.optional-types.json", test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.NestedBean.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.OptionalWrapper.class,
                Optional.class, OptionalDouble.class, OptionalLong.class);
    }

    @Test
    void testJavaxPathParamTemplateRegex() throws IOException, JSONException {
        test("params.path-param-templates.json",
                test.io.smallrye.openapi.runtime.scanner.javax.PathParamTemplateRegexTestResource.class);
    }

    @Test
    void testJakartaPathParamTemplateRegex() throws IOException, JSONException {
        test("params.path-param-templates.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.PathParamTemplateRegexTestResource.class);
    }

    @Test
    void testJavaxPathSegmentMatrix() throws IOException, JSONException {
        test("params.path-segment-param.json",
                test.io.smallrye.openapi.runtime.scanner.javax.PathSegmentMatrixTestResource.class);
    }

    @Test
    void testJakartaPathSegmentMatrix() throws IOException, JSONException {
        test("params.path-segment-param.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.PathSegmentMatrixTestResource.class);
    }

    @Test
    void testJavaxParamNameOverride() throws IOException, JSONException {
        test("params.param-name-override.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ParamNameOverrideTestResource.class);
    }

    @Test
    void testJakartaParamNameOverride() throws IOException, JSONException {
        test("params.param-name-override.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParamNameOverrideTestResource.class);
    }

    @Test
    void testJavaxCommonTargetMethodParameter() throws IOException, JSONException {
        test("params.common-annotation-target-method.json",
                test.io.smallrye.openapi.runtime.scanner.javax.CommonTargetMethodParameterResource.class);
    }

    @Test
    void testJakartaCommonTargetMethodParameter() throws IOException, JSONException {
        test("params.common-annotation-target-method.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.CommonTargetMethodParameterResource.class);
    }

    @Test
    void testJavaxRestEasyReactivePathParamOmitted() throws IOException, JSONException {
        test("params.resteasy-reactive-missing-restpath.json",
                test.io.smallrye.openapi.runtime.scanner.javax.RestEasyReactivePathParamOmittedTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.RestEasyReactivePathParamOmittedTestResource2.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaRestEasyReactivePathParamOmitted() throws IOException, JSONException {
        test("params.resteasy-reactive-missing-restpath.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactivePathParamOmittedTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactivePathParamOmittedTestResource2.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxSerializedIndexParameterAnnotations() throws IOException, JSONException {
        Index i1 = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.GreetResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.GreetResource.GreetingMessage.class);
        testSerializedIndexParameterAnnotations(i1);
    }

    @Test
    void testJakartaSerializedIndexParameterAnnotations() throws IOException, JSONException {
        Index i1 = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.GreetResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.GreetResource.GreetingMessage.class);
        testSerializedIndexParameterAnnotations(i1);
    }

    void testSerializedIndexParameterAnnotations(Index i) throws IOException, JSONException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IndexWriter writer = new IndexWriter(out);
        writer.write(i);

        Index index = new IndexReader(new ByteArrayInputStream(out.toByteArray())).read();
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("params.serialized-annotation-index.json", result);
    }

    @Test
    void testJavaxParameterRefOnly() throws IOException, JSONException {
        test("params.parameter-ref-property.json",
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterRefTestApplication.class,
                test.io.smallrye.openapi.runtime.scanner.javax.ParameterRefTestResource.class);
    }

    @Test
    void testJakartaParameterRefOnly() throws IOException, JSONException {
        test("params.parameter-ref-property.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterRefTestApplication.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterRefTestResource.class);
    }

    @Test
    void testJavaxDefaultEnumValue() throws IOException, JSONException {
        test("params.local-schema-attributes.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DefaultEnumTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.DefaultEnumTestResource.MyEnum.class);
    }

    @Test
    void testJakartaDefaultEnumValue() throws IOException, JSONException {
        test("params.local-schema-attributes.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DefaultEnumTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.DefaultEnumTestResource.MyEnum.class);
    }

    @Test
    void testJavaxGenericTypeVariableResource() throws IOException, JSONException {
        test("params.generic-type-variables.json", test.io.smallrye.openapi.runtime.scanner.javax.BaseGenericResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.BaseGenericResource.GenericBean.class,
                test.io.smallrye.openapi.runtime.scanner.javax.IntegerStringUUIDResource.class);
    }

    @Test
    void testJakartaGenericTypeVariableResource() throws IOException, JSONException {
        test("params.generic-type-variables.json", test.io.smallrye.openapi.runtime.scanner.jakarta.BaseGenericResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BaseGenericResource.GenericBean.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.IntegerStringUUIDResource.class);
    }

    @Test
    void testPreferredParameterOrderWithAnnotation() throws IOException, JSONException {
        test("params.annotation-preferred-order.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterOrderResource.CLASSES);
    }

    static class Issue1256 {
        static final Class<?>[] CLASSES = {
                BeanParamBean.class,
                Filter.class,
                FilterBean.class,
                JsonBase.class,
                DataJson.class,
                GenericBaseInterface.class,
                RestInterface.class,
                RestImpl.class
        };

        static class BeanParamBean {
            @jakarta.ws.rs.QueryParam("param")
            @Parameter(description = "A parameter")
            private String param;
        }

        interface Filter {
        }

        static class FilterBean implements Filter {
        }

        static class JsonBase {
        }

        static class DataJson extends JsonBase {
        }

        interface GenericBaseInterface<T extends JsonBase, F extends Filter> {

            @Operation(summary = "list")
            @APIResponse(responseCode = "200", description = "OK")
            @APIResponse(responseCode = "500", description = "internal server error", content = @Content(schema = @Schema(type = SchemaType.OBJECT)))
            List<T> list(BeanParamBean params, F filter);
        }

        @jakarta.ws.rs.Path("reproducer/reproducers")
        @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
        @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
        @Tag(name = "reproducers", description = "This resource is there for reproducing bug 1256.")
        interface RestInterface extends GenericBaseInterface<DataJson, FilterBean> {

            @jakarta.ws.rs.POST
            @Operation(summary = "create")
            @APIResponse(responseCode = "200", description = "OK")
            @APIResponse(responseCode = "500", description = "internal server error", content = @Content(schema = @Schema(type = SchemaType.OBJECT)))
            DataJson create(DataJson json);

            @jakarta.ws.rs.GET
            @Override
            List<DataJson> list(@jakarta.ws.rs.BeanParam BeanParamBean params, @jakarta.ws.rs.BeanParam FilterBean filter);
        }

        static class RestImpl implements RestInterface {
            @Override
            public DataJson create(DataJson json) {
                return null;
            }

            @Override
            public List<DataJson> list(BeanParamBean params, FilterBean filter) {
                return null;
            }
        }
    }

    @Test
    void testParamsNotDuplicated() throws IOException, JSONException {
        test("params.synthetic-methods-not-included.json", Issue1256.CLASSES);
    }

    static class Issue1466 {
        static final Class<?>[] CLASSES = {
                Fruit.class,
                FruitId.class,
                FruitResource.class
        };

        static class Fruit {
            public String name;
            public String description;

            public Fruit() {
            }

            public Fruit(String name, String description) {
                this.name = name;
                this.description = description;
            }
        }

        @Schema(type = SchemaType.STRING)
        static class FruitId {
            private final String id;

            public FruitId(String id) {
                this.id = id;
            }

            public String getId() {
                return this.id;
            }
        }

        @jakarta.ws.rs.Path("/fruits")
        static class FruitResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("{fruitId}")
            public Fruit get(@jakarta.ws.rs.PathParam("fruitId") FruitId fruitId) {
                return null;
            }
        }
    }

    @Test
    void testPathParamValueType() throws IOException, JSONException {
        test("params.value-class-pathparam.json", Issue1466.CLASSES);
    }

    static class ParameterConstraintComposition {
        static final Class<?>[] CLASSES = {
                Resource.class,
                CustomIntConstraint.class
        };

        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(100000)
        @jakarta.validation.Constraint(validatedBy = {})
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface CustomIntConstraint {
            String message() default "";

            Class<?>[] groups() default {};

            Class<? extends jakarta.validation.Payload>[] payload() default {};
        }

        @jakarta.ws.rs.Path("/custom-resource")
        static class Resource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("{id}")
            public String get(@CustomIntConstraint @jakarta.ws.rs.QueryParam("id") int id) {
                return null;
            }
        }
    }

    @Test
    void testParameterConstraintComposition() throws IOException, JSONException {
        test("params.constraint-composition.json", ParameterConstraintComposition.CLASSES);
    }

    static class NullableRefParamClasses {
        static final Class<?>[] CLASSES = {
                StatusEnum.class,
                FruitResource.class
        };

        enum StatusEnum {
            VAL1,
            VAL2;
        }

        @jakarta.ws.rs.Path("/status")
        static class FruitResource {
            @jakarta.ws.rs.GET
            public String get(@jakarta.ws.rs.QueryParam("status") @org.jetbrains.annotations.Nullable StatusEnum status) {
                return null;
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "3.0.3, params.nullable-ref-param-3.0.json",
            "3.1.0, params.nullable-ref-param-3.1.json",
    })
    void testNullableRefParam(String oasVersion, String expectedResource) throws IOException, JSONException {
        OpenAPI result = scan(config(SmallRyeOASConfig.VERSION, oasVersion), NullableRefParamClasses.CLASSES);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testUnsortedParameters() throws IOException, JSONException {
        @jakarta.ws.rs.Path("/status")
        class Resource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("/{resourceId}")
            public String get(
                    @jakarta.ws.rs.QueryParam("q7") int q7,
                    @jakarta.ws.rs.QueryParam("q9") int q9,
                    @jakarta.ws.rs.HeaderParam("h6") int h8,
                    @jakarta.ws.rs.PathParam("resourceId") String resourceId,
                    @jakarta.ws.rs.QueryParam("q8") int q8) {
                return null;
            }
        }

        test(dynamicConfig(SmallRyeOASConfig.SMALLRYE_SORTED_PARAMETERS_ENABLE, Boolean.FALSE),
                "params.unsorted-scan-order.json", Resource.class);
    }

    @Test
    void testDuplicatedParametersIgnored() throws IOException, JSONException {
        /**
         * This class is the equivalent of record:
         *
         * <code>
         * record Parameters(@QueryParam("qparam") @Parameter(description = "Hi") String qparam) { }
         * </code>
         */
        class Parameters {
            @jakarta.ws.rs.QueryParam("qparam")
            @Parameter(description = "Hi")
            String qparam;

            @SuppressWarnings("unused")
            public Parameters(
                    @jakarta.ws.rs.QueryParam("qparam") @Parameter(description = "Hi") String qparam) {
                this.qparam = qparam;
            }

            @jakarta.ws.rs.QueryParam("qparam")
            @Parameter(description = "Hi")
            public String qparam() {
                return qparam;
            }
        }

        @jakarta.ws.rs.Path("/resource")
        class BeanParamResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.TEXT_PLAIN)
            @APIResponse(responseCode = "200", description = "Get a message")
            public String get(@jakarta.ws.rs.BeanParam Parameters params) {
                return "Hi";
            }
        }

        test(dynamicConfig(SmallRyeOASConfig.SMALLRYE_SORTED_PARAMETERS_ENABLE, Boolean.FALSE),
                "params.ignored-name-type-duplicates.json", Parameters.class, BeanParamResource.class);
    }
}
