package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class ResourceParameterTests extends JaxRsDataObjectScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #25.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/25
     *
     */
    @Test
    void testJavaxParameterResource() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.javax.ParameterResource.class);
        testParameterResource(i);
    }

    @Test
    void testJakartaParameterResource() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.resources.jakarta.ParameterResource.class);
        testParameterResource(i);
    }

    void testParameterResource(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()), i);
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
    void testJavaxPrimitiveArraySchema() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.PrimitiveArraySchemaTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.PrimitiveArraySchemaTestResource.PrimitiveArrayTestObject.class);
        testPrimitiveArraySchema(i);
    }

    @Test
    void testJakartaPrimitiveArraySchema() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.PrimitiveArraySchemaTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.PrimitiveArraySchemaTestResource.PrimitiveArrayTestObject.class);
        testPrimitiveArraySchema(i);
    }

    void testPrimitiveArraySchema(Index i) throws IOException, JSONException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-schema.json", result);
    }

    @Test
    void testJavaxPrimitiveArrayParameter() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.PrimitiveArrayParameterTestResource.class);
        testPrimitiveArrayParameter(i);
    }

    @Test
    void testJakartaPrimitiveArrayParameter() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.PrimitiveArrayParameterTestResource.class);
        testPrimitiveArrayParameter(i);
    }

    void testPrimitiveArrayParameter(Index i) throws IOException, JSONException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-param.json", result);
    }

    @Test
    void testJavaxPrimitiveArrayPolymorphism() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.PrimitiveArrayPolymorphismTestResource.class);
        testPrimitiveArrayPolymorphism(i);
    }

    @Test
    void testJakartaPrimitiveArrayPolymorphism() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.PrimitiveArrayPolymorphismTestResource.class);
        testPrimitiveArrayPolymorphism(i);
    }

    void testPrimitiveArrayPolymorphism(Index i) throws IOException, JSONException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.primitive-array-polymorphism.json", result);
    }

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #201.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/201
     *
     */
    @Test
    void testJavaxSchemaImplementationType() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.SchemaImplementationTypeResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.SchemaImplementationTypeResource.GreetingMessage.class,
                test.io.smallrye.openapi.runtime.scanner.javax.SchemaImplementationTypeResource.SimpleString.class);
        testSchemaImplementationType(i);
    }

    @Test
    void testJakartaSchemaImplementationType() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.SchemaImplementationTypeResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.SchemaImplementationTypeResource.GreetingMessage.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.SchemaImplementationTypeResource.SimpleString.class);
        testSchemaImplementationType(i);
    }

    void testSchemaImplementationType(Index i) throws IOException, JSONException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.string-implementation-wrapped.json", result);
    }

    /*
     * Test case derived for Smallrye OpenAPI issue #233.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/233
     *
     */
    @Test
    void testJavaxTimeResource() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.TimeTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.TimeTestResource.UTC.class, LocalTime.class, OffsetTime.class);
        testTimeResource(i);
    }

    @Test
    void testJakartaTimeResource() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.TimeTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.TimeTestResource.UTC.class, LocalTime.class, OffsetTime.class);
        testTimeResource(i);
    }

    void testTimeResource(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.time.json", result);
    }

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #237.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/237
     *
     */
    @Test
    void testJavaxTypeVariableResponse() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.TypeVariableResponseTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.TypeVariableResponseTestResource.Dto.class);
        testTypeVariableResponse(i);
    }

    @Test
    void testJakartaTypeVariableResponse() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.TypeVariableResponseTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.TypeVariableResponseTestResource.Dto.class);
        testTypeVariableResponse(i);
    }

    void testTypeVariableResponse(Index i) throws IOException, JSONException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(i, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.parameters.type-variable.json", result);
    }

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #248.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/248
     *
     */
    @Test
    void testJavaxResponseTypeUnindexed() throws IOException, JSONException {
        // Index is intentionally missing ResponseTypeUnindexedTestResource$ThirdPartyType
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.ResponseTypeUnindexedTestResource.class);
        testResponseTypeUnindexed(i);
    }

    @Test
    void testJakartaResponseTypeUnindexed() throws IOException, JSONException {
        // Index is intentionally missing ResponseTypeUnindexedTestResource$ThirdPartyType
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.ResponseTypeUnindexedTestResource.class);
        testResponseTypeUnindexed(i);
    }

    void testResponseTypeUnindexed(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.unknown-type.empty-schema.json", result);
    }

    /*
     * Test cases derived from original example in SmallRye OpenAPI issue #260.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/260
     *
     */
    @Test
    void testJavaxGenericSetResponseWithSetIndexed() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.FruitResource.class,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class, test.io.smallrye.openapi.runtime.scanner.Seed.class,
                Set.class);

        testGenericSetResponseWithSetIndexed(i);
    }

    @Test
    void testJakartaGenericSetResponseWithSetIndexed() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource.class,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class,
                Set.class);

        testGenericSetResponseWithSetIndexed(i);
    }

    void testGenericSetResponseWithSetIndexed(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.generic-collection.set-indexed.json", result);
    }

    @Test
    void testJavaxGenericSetResponseWithSetIndexedWithoutArrayRefs() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.FruitResource.class,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class,
                Set.class);
        testGenericSetResponseWithSetIndexedWithoutArrayRefs(i);
    }

    @Test
    void testJakartaGenericSetResponseWithSetIndexedWithoutArrayRefs() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource.class,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class,
                Set.class);
        testGenericSetResponseWithSetIndexedWithoutArrayRefs(i);
    }

    void testGenericSetResponseWithSetIndexedWithoutArrayRefs(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(
                dynamicConfig(SmallRyeOASConfig.SMALLRYE_ARRAY_REFERENCES_ENABLE,
                        Boolean.FALSE),
                i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.generic-collection.set-indexed-wo-array-refs.json", result);
    }

    @Test
    void testJavaxGenericSetResponseWithSetUnindexed() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.FruitResource.class,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class);

        testGenericSetResponseWithSetUnindexed(i);
    }

    @Test
    void testJakartaGenericSetResponseWithSetUnindexed() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.FruitResource.class,
                test.io.smallrye.openapi.runtime.scanner.Fruit.class,
                test.io.smallrye.openapi.runtime.scanner.Seed.class);

        testGenericSetResponseWithSetUnindexed(i);
    }

    void testGenericSetResponseWithSetUnindexed(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.generic-collection.set-unindexed.json", result);
    }

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #239.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/239
     *
     */
    @Test
    void testJavaxBeanParamMultipartFormInheritance() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.BeanParamMultipartFormInheritanceResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.MultipartFormVerify.class,
                test.io.smallrye.openapi.runtime.scanner.javax.MultipartFormUploadIconForm.class,
                test.io.smallrye.openapi.runtime.scanner.javax.BeanParamBase.class,
                test.io.smallrye.openapi.runtime.scanner.javax.BeanParamImpl.class,
                test.io.smallrye.openapi.runtime.scanner.javax.BeanParamAddon.class);
        testBeanParamMultipartFormInheritance(i);
    }

    @Test
    void testJakartaBeanParamMultipartFormInheritance() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.BeanParamMultipartFormInheritanceResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipartFormVerify.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultipartFormUploadIconForm.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BeanParamBase.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BeanParamImpl.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BeanParamAddon.class);
        testBeanParamMultipartFormInheritance(i);
    }

    void testBeanParamMultipartFormInheritance(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("params.beanparam-multipartform-inherited.json", result);
    }

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #330.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/330
     *
     */

    @Test
    void testJavaxMethodTargetParametersWithoutJAXRS() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.MethodTargetParametersResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.MethodTargetParametersResource.PagedResponse.class);
        testMethodTargetParametersWithoutJAXRS(i);
    }

    @Test
    void testJakartaMethodTargetParametersWithoutJAXRS() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.MethodTargetParametersResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MethodTargetParametersResource.PagedResponse.class);
        testMethodTargetParametersWithoutJAXRS(i);
    }

    void testMethodTargetParametersWithoutJAXRS(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("params.method-target-nojaxrs.json", result);
    }

    /*
     * Test case derived from original example in SmallRye OpenAPI issue #437.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/437
     *
     */
    @Test
    void testJavaxJsonbTransientOnSetterGeneratesReadOnly() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.javax.Policy437Resource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Policy437.class);
        testJsonbTransientOnSetterGeneratesReadOnly(i);
    }

    @Test
    void testJakartaJsonbTransientOnSetterGeneratesReadOnly() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.Policy437Resource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Policy437.class);
        testJsonbTransientOnSetterGeneratesReadOnly(i);
    }

    void testJsonbTransientOnSetterGeneratesReadOnly(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("responses.hidden-setter-readonly-props.json", result);
    }
}
