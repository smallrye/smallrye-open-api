package io.smallrye.openapi.api.util;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

/**
 * @author eric.wittmann@gmail.com
 */
public class FilterUtilTest {

    /**
     * Loads a resource as a string (reads the content at the URL).
     * 
     * @param testResource
     * @throws IOException
     */
    private static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, "UTF-8");
    }

    /**
     * Compares two JSON strings.
     * 
     * @param expected
     * @param actual
     * @throws JSONException
     */
    private static void assertJsonEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, true);
    }

    /**
     * Test method for
     * {@link FilterUtil#applyFilter(org.eclipse.microprofile.openapi.OASFilter, org.eclipse.microprofile.openapi.models.OpenAPI)}.
     * 
     * @throws Exception
     */
    @Test
    public void testApplyFilter() throws Exception {
        URL beforeUrl = FilterUtilTest.class.getResource("filter-before.json");
        URL afterUrl = FilterUtilTest.class.getResource("filter-after.json");

        OpenAPI model = OpenApiParser.parse(beforeUrl);
        OASFilter filter = filter();

        model = FilterUtil.applyFilter(filter, model);

        String actual = OpenApiSerializer.serialize(model, Format.JSON);
        String expected = loadResource(afterUrl);

        assertJsonEquals(expected, actual);
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
