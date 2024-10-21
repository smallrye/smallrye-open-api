package io.smallrye.openapi.api.util;

import static io.smallrye.openapi.runtime.scanner.IndexScannerTestBase.loadResource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.URL;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

/**
 * @author eric.wittmann@gmail.com
 */
class FilterUtilTest {

    static final Logger LOG = Logger.getLogger(FilterUtilTest.class);

    /**
     * Compares two JSON strings.
     *
     * @param expected
     * @param actual
     * @throws JSONException
     */
    private static void assertJsonEquals(String expected, String actual) throws JSONException {
        LOG.debug(actual);
        JSONAssert.assertEquals(expected, actual, true);
    }

    /**
     * Test method for
     * {@link FilterUtil#applyFilter(org.eclipse.microprofile.openapi.OASFilter, org.eclipse.microprofile.openapi.models.OpenAPI)}.
     *
     * @throws Exception
     */
    @Test
    void testApplyFilter() throws Exception {
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfig.fromConfig(config);

        URL beforeUrl = FilterUtilTest.class.getResource("filter-before.json");
        URL afterUrl = FilterUtilTest.class.getResource("filter-after.json");

        OpenAPI model = OpenApiParser.parse(beforeUrl);
        OpenApiDocument document = OpenApiDocument.INSTANCE;
        document.reset();
        document.config(openApiConfig);
        document.modelFromReader(model);

        OASFilter f1 = filter();
        OASFilter f2 = filter();
        OASFilter f3 = new OASFilter() {
            @Override
            public void filterOpenAPI(OpenAPI openAPI) {
                openAPI.addServer(OASFactory.createServer().url("urn:server3"));
            }
        };
        OASFilter f4 = new OASFilter() {
            @Override
            public void filterOpenAPI(OpenAPI openAPI) {
                openAPI.addServer(OASFactory.createServer().url("urn:server4"));
            }
        };

        document.filter(f1);
        document.filter(f2); // Add it twice to make sure we do not repeat
        document.filter(f3);
        document.filter(f4);
        document.initialize();

        model = document.get();

        String actual = OpenApiSerializer.serialize(model, Format.JSON);
        String expected = loadResource(afterUrl);

        assertJsonEquals(expected, actual);
    }

    @Test
    void testCyclicReferencesSafe() {
        Schema schema1 = OASFactory.createSchema();
        Schema schema2 = OASFactory.createSchema();
        Schema schema3 = OASFactory.createSchema();

        // Cycle in lists
        schema1.addAllOf(schema2);
        schema2.addAllOf(schema3);
        schema3.addAllOf(schema1);

        // Cycle in map
        Schema schema4 = OASFactory.createSchema();
        schema4.addProperty("prop1", schema1);
        schema4.addProperty("prop4", schema4);

        // Cycle in simple object reference
        Schema schema5 = OASFactory.createSchema();
        schema5.setNot(schema5);

        OpenAPI model = OASFactory.createOpenAPI()
                .info(OASFactory.createInfo())
                .components(OASFactory.createComponents()
                        .addSchema("Schema1", schema1)
                        .addSchema("Schema2", schema2)
                        .addSchema("Schema3", schema3)
                        .addSchema("Schema4", schema4)
                        .addSchema("Schema5", schema5));

        assertDoesNotThrow(() -> FilterUtil.applyFilter(filter(), model));
    }

    /**
     * Creates and returns the filter to use for the test.
     */
    private OASFilter filter() {
        return new OASFilter() {
            /**
             * @see org.eclipse.microprofile.openapi.OASFilter#filterOpenAPI(org.eclipse.microprofile.openapi.models.OpenAPI)
             */
            @Override
            public void filterOpenAPI(OpenAPI openAPI) {
                openAPI.getInfo().setLicense(null);
                openAPI.getInfo().setTitle("Updated API Title");
            }

            /**
             * @see org.eclipse.microprofile.openapi.OASFilter.filterTag(org.eclipse.microprofile.openapi.models.tags.Tag)
             */
            @Override
            public Tag filterTag(Tag tag) {
                if (tag.getName().equals("tag-1")) {
                    return null;
                }
                return tag;
            }

            /**
             * @see org.eclipse.microprofile.openapi.OASFilter#filterPathItem(org.eclipse.microprofile.openapi.models.PathItem)
             */
            @Override
            public PathItem filterPathItem(PathItem pathItem) {
                if (pathItem.getRef() != null) {
                    return null;
                } else {
                    return pathItem;
                }
            }

            /**
             * @see org.eclipse.microprofile.openapi.OASFilter#filterOperation(org.eclipse.microprofile.openapi.models.Operation)
             */
            @Override
            public Operation filterOperation(Operation operation) {
                if (operation.getTags() != null && operation.getTags().contains("tag-1")) {
                    operation.removeTag("tag-1");
                }
                return operation;
            }
        };
    }

}
