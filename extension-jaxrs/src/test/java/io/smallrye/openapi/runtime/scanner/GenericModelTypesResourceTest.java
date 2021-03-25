package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.constants.OpenApiConstants;

class GenericModelTypesResourceTest extends IndexScannerTestBase {

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #25.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/25
     *
     */
    @Test
    void testJavaxGenericsApplication() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.BaseModel.class,
                test.io.smallrye.openapi.runtime.scanner.BaseResource.class,
                test.io.smallrye.openapi.runtime.scanner.KingCrimson.class,
                test.io.smallrye.openapi.runtime.scanner.KingCrimsonResource.class,
                test.io.smallrye.openapi.runtime.scanner.Magma.class,
                test.io.smallrye.openapi.runtime.scanner.MagmaResource.class,
                test.io.smallrye.openapi.runtime.scanner.Message.class,
                test.io.smallrye.openapi.runtime.scanner.OpenAPIConfig.class,
                test.io.smallrye.openapi.runtime.scanner.Residents.class,
                test.io.smallrye.openapi.runtime.scanner.ResidentsResource.class,
                test.io.smallrye.openapi.runtime.scanner.Result.class,
                test.io.smallrye.openapi.runtime.scanner.ResultList.class,
                test.io.smallrye.openapi.runtime.scanner.POJO.class,
                List.class);

        testGenericsApplication(i);
    }

    @Test
    void testJakartaGenericsApplication() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.BaseModel.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BaseResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.KingCrimson.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.KingCrimsonResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Magma.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MagmaResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Message.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OpenAPIConfig.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Residents.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResidentsResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Result.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResultList.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.POJO.class,
                List.class);

        testGenericsApplication(i);
    }

    void testGenericsApplication(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.generic-model-types.json", result);
    }

    @Test
    void testJavaxGenericsApplicationWithoutArrayRefs() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.BaseModel.class,
                test.io.smallrye.openapi.runtime.scanner.BaseResource.class,
                test.io.smallrye.openapi.runtime.scanner.KingCrimson.class,
                test.io.smallrye.openapi.runtime.scanner.KingCrimsonResource.class,
                test.io.smallrye.openapi.runtime.scanner.Magma.class,
                test.io.smallrye.openapi.runtime.scanner.MagmaResource.class,
                test.io.smallrye.openapi.runtime.scanner.Message.class,
                test.io.smallrye.openapi.runtime.scanner.OpenAPIConfig.class,
                test.io.smallrye.openapi.runtime.scanner.Residents.class,
                test.io.smallrye.openapi.runtime.scanner.ResidentsResource.class,
                test.io.smallrye.openapi.runtime.scanner.Result.class,
                test.io.smallrye.openapi.runtime.scanner.ResultList.class,
                test.io.smallrye.openapi.runtime.scanner.POJO.class,
                List.class);

        testGenericsApplicationWithoutArrayRefs(i);
    }

    @Test
    void testJakartaGenericsApplicationWithoutArrayRefs() throws IOException, JSONException {
        Index i = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.BaseModel.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.BaseResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.KingCrimson.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.KingCrimsonResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Magma.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.MagmaResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Message.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.OpenAPIConfig.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Residents.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResidentsResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Result.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ResultList.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.POJO.class,
                List.class);

        testGenericsApplicationWithoutArrayRefs(i);
    }

    void testGenericsApplicationWithoutArrayRefs(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(
                dynamicConfig(OpenApiConstants.SMALLRYE_ARRAY_REFERENCES_ENABLE,
                        Boolean.FALSE),
                i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.generic-model-types-wo-array-refs.json", result);
    }
}
