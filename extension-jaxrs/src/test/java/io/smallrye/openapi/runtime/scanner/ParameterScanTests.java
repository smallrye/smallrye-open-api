package io.smallrye.openapi.runtime.scanner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class ParameterScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testJavaxIgnoredMpOpenApiHeaders() throws IOException, JSONException {
        test("params.ignored-mp-openapi-headers.json",
                test.io.smallrye.openapi.runtime.scanner.IgnoredMpOpenApiHeaderArgsTestResource.class,
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
                test.io.smallrye.openapi.runtime.scanner.ParameterOnMethodTestResource.class,
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
                test.io.smallrye.openapi.runtime.scanner.ResourcePathParamTestResource.class,
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
                test.io.smallrye.openapi.runtime.scanner.ParameterInBeanFromFieldTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ParameterInBeanFromFieldTestResource.Bean.class,
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
                test.io.smallrye.openapi.runtime.scanner.ParameterInBeanFromSetterTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ParameterInBeanFromSetterTestResource.Bean.class,
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
                test.io.smallrye.openapi.runtime.scanner.PathParamWithFormParamsTestResource.class,
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
                test.io.smallrye.openapi.runtime.scanner.MultipleContentTypesWithFormParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaMultipleContentTypesWithFormParams() throws IOException, JSONException {
        test("params.multiple-content-types-with-form-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipleContentTypesWithFormParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxParametersInConstructor() throws IOException, JSONException {
        test("params.parameters-in-constructor.json",
                test.io.smallrye.openapi.runtime.scanner.ParametersInConstructorTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ParametersInConstructorTestResource.Bean.class,
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
                test.io.smallrye.openapi.runtime.scanner.MatrixParamsOnResourceMethodArgsTestResource.class,
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
                test.io.smallrye.openapi.runtime.scanner.MatrixParamsOnResourceMethodCustomNameTestResource.class,
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
                test.io.smallrye.openapi.runtime.scanner.MatrixParamsOnMethodAndFieldArgsTestResource.class,
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
        test("params.all-the-params.json",
                test.io.smallrye.openapi.runtime.scanner.AllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.AllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
        test("params.all-the-params.json",
                test.io.smallrye.openapi.runtime.scanner.RestEasyReactiveAllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.RestEasyReactiveAllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaAllTheParams() throws IOException, JSONException {
        test("params.all-the-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.AllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
        test("params.all-the-params.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactiveAllTheParamsTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactiveAllTheParamsTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxMultipartForm() throws IOException, JSONException {
        test("params.multipart-form.json",
                test.io.smallrye.openapi.runtime.scanner.MultipartFormTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.MultipartFormTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class,
                InputStream.class);
    }

    @Test
    void testJakartaMultipartForm() throws IOException, JSONException {
        test("params.multipart-form.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipartFormTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipartFormTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class,
                InputStream.class);
    }

    @Test
    void testJavaxEnumQueryParam() throws IOException, JSONException {
        test("params.enum-form-param.json",
                test.io.smallrye.openapi.runtime.scanner.EnumQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.EnumQueryParamTestResource.TestEnum.class,
                test.io.smallrye.openapi.runtime.scanner.EnumQueryParamTestResource.TestEnumWithSchema.class);
    }

    @Test
    void testJakartaEnumQueryParam() throws IOException, JSONException {
        test("params.enum-form-param.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.EnumQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.EnumQueryParamTestResource.TestEnum.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.EnumQueryParamTestResource.TestEnumWithSchema.class);
    }

    @Test
    void testJavaxUUIDQueryParam() throws IOException, JSONException {
        test("params.uuid-params-responses.json",
                test.io.smallrye.openapi.runtime.scanner.UUIDQueryParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.UUIDQueryParamTestResource.WrappedUUID.class);
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
                test.io.smallrye.openapi.runtime.scanner.RestEasyFieldsAndSettersTestResource.class);
    }

    @Test
    void testJakartaRestEasyFieldsAndSetters() throws IOException, JSONException {
        test("params.resteasy-fields-and-setters.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyFieldsAndSettersTestResource.class);
    }

    @Test
    void testJavaxCharSequenceArrayParam() throws IOException, JSONException {
        test("params.char-sequence-arrays.json",
                test.io.smallrye.openapi.runtime.scanner.CharSequenceArrayParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.CharSequenceArrayParamTestResource.EchoResult.class);
    }

    @Test
    void testJakartaCharSequenceArrayParam() throws IOException, JSONException {
        test("params.char-sequence-arrays.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.CharSequenceArrayParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.CharSequenceArrayParamTestResource.EchoResult.class);
    }

    @Test
    void testJavaxOptionalParam() throws IOException, JSONException {
        test("params.optional-types.json",
                test.io.smallrye.openapi.runtime.scanner.OptionalParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.OptionalParamTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.OptionalParamTestResource.NestedBean.class,
                test.io.smallrye.openapi.runtime.scanner.OptionalParamTestResource.OptionalWrapper.class,
                Optional.class,
                OptionalDouble.class,
                OptionalLong.class);
    }

    @Test
    void testJakartaOptionalParam() throws IOException, JSONException {
        test("params.optional-types.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.Bean.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.NestedBean.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OptionalParamTestResource.OptionalWrapper.class,
                Optional.class,
                OptionalDouble.class,
                OptionalLong.class);
    }

    @Test
    void testJavaxPathParamTemplateRegex() throws IOException, JSONException {
        test("params.path-param-templates.json",
                test.io.smallrye.openapi.runtime.scanner.PathParamTemplateRegexTestResource.class);
    }

    @Test
    void testJakartaPathParamTemplateRegex() throws IOException, JSONException {
        test("params.path-param-templates.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.PathParamTemplateRegexTestResource.class);
    }

    @Test
    void testJavaxPathSegmentMatrix() throws IOException, JSONException {
        test("params.path-segment-param.json",
                test.io.smallrye.openapi.runtime.scanner.PathSegmentMatrixTestResource.class);
    }

    @Test
    void testJakartaPathSegmentMatrix() throws IOException, JSONException {
        test("params.path-segment-param.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.PathSegmentMatrixTestResource.class);
    }

    @Test
    void testJavaxParamNameOverride() throws IOException, JSONException {
        test("params.param-name-override.json",
                test.io.smallrye.openapi.runtime.scanner.ParamNameOverrideTestResource.class);
    }

    @Test
    void testJakartaParamNameOverride() throws IOException, JSONException {
        test("params.param-name-override.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParamNameOverrideTestResource.class);
    }

    @Test
    void testJavaxCommonTargetMethodParameter() throws IOException, JSONException {
        test("params.common-annotation-target-method.json",
                test.io.smallrye.openapi.runtime.scanner.CommonTargetMethodParameterResource.class);
    }

    @Test
    void testJakartaCommonTargetMethodParameter() throws IOException, JSONException {
        test("params.common-annotation-target-method.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.CommonTargetMethodParameterResource.class);
    }

    @Test
    void testJavaxRestEasyReactivePathParamOmitted() throws IOException, JSONException {
        test("params.resteasy-reactive-missing-restpath.json",
                test.io.smallrye.openapi.runtime.scanner.RestEasyReactivePathParamOmittedTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJakartaRestEasyReactivePathParamOmitted() throws IOException, JSONException {
        test("params.resteasy-reactive-missing-restpath.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.RestEasyReactivePathParamOmittedTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.Widget.class);
    }

    @Test
    void testJavaxSerializedIndexParameterAnnotations() throws IOException, JSONException {
        Index i1 = indexOf(test.io.smallrye.openapi.runtime.scanner.GreetResource.class,
                test.io.smallrye.openapi.runtime.scanner.GreetResource.GreetingMessage.class);
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
        test("params.parameter-ref-property.json", test.io.smallrye.openapi.runtime.scanner.ParameterRefTestApplication.class,
                test.io.smallrye.openapi.runtime.scanner.ParameterRefTestResource.class);
    }

    @Test
    void testJakartaParameterRefOnly() throws IOException, JSONException {
        test("params.parameter-ref-property.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterRefTestApplication.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ParameterRefTestResource.class);
    }

    @Test
    void testJavaxDefaultEnumValue() throws IOException, JSONException {
        test("params.local-schema-attributes.json", test.io.smallrye.openapi.runtime.scanner.DefaultEnumTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.DefaultEnumTestResource.MyEnum.class);
    }

    @Test
    void testJakartaDefaultEnumValue() throws IOException, JSONException {
        test("params.local-schema-attributes.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DefaultEnumTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.DefaultEnumTestResource.MyEnum.class);
    }

    @Test
    void testJavaxGenericTypeVariableResource() throws IOException, JSONException {
        test("params.generic-type-variables.json", test.io.smallrye.openapi.runtime.scanner.BaseGenericResource.class,
                test.io.smallrye.openapi.runtime.scanner.BaseGenericResource.GenericBean.class,
                test.io.smallrye.openapi.runtime.scanner.IntegerStringUUIDResource.class);
    }

    @Test
    void testJakartaGenericTypeVariableResource() throws IOException, JSONException {
        test("params.generic-type-variables.json", test.io.smallrye.openapi.runtime.scanner.jakarta.BaseGenericResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BaseGenericResource.GenericBean.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.IntegerStringUUIDResource.class);
    }
}
