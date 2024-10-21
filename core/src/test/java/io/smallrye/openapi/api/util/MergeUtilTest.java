package io.smallrye.openapi.api.util;

import static io.smallrye.openapi.runtime.scanner.IndexScannerTestBase.loadResource;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

/**
 * @author eric.wittmann@gmail.com
 */
class MergeUtilTest {

    private static final Logger LOG = Logger.getLogger(MergeUtilTest.class);

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
     * Performs a single full merge test. Two documents are loaded (as resources) and then
     * merged. The expected merge result is then loaded and compared with the actual result.
     *
     * @param resource1
     * @param resource2
     * @param expected
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    private static void doTest(String resource1, String resource2, String expected)
            throws IOException, ParseException, JSONException {
        URL resource1Url = MergeUtilTest.class.getResource(resource1);
        URL resource2Url = MergeUtilTest.class.getResource(resource2);
        URL expectedUrl = MergeUtilTest.class.getResource(expected);

        String expectedContent = loadResource(expectedUrl);

        OpenAPI resource1Model = OpenApiParser.parse(resource1Url);
        OpenAPI resource2Model = OpenApiParser.parse(resource2Url);

        OpenAPI actualModel = MergeUtil.merge(resource1Model, resource2Model);

        String actual = OpenApiSerializer.serialize(actualModel, Format.JSON);

        assertJsonEquals(expectedContent, actual);
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_Info() throws IOException, ParseException, JSONException {
        doTest("_info/info1.json", "_info/info2.json", "_info/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_Extensions() throws IOException, ParseException, JSONException {
        doTest("_extensions/extensions1.json", "_extensions/extensions2.json", "_extensions/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_Tags() throws IOException, ParseException, JSONException {
        doTest("_tags/tags1.json", "_tags/tags2.json", "_tags/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_Servers() throws IOException, ParseException, JSONException {
        doTest("_servers/servers1.json", "_servers/servers2.json", "_servers/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_PathDocs() throws IOException, ParseException, JSONException {
        doTest("_pathDocs/path1.json", "_pathDocs/path2.json", "_pathDocs/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_PathDefault() throws IOException, ParseException, JSONException {
        doTest("_pathDefault/pathDefault1.json", "_pathDefault/pathDefault2.json", "_pathDefault/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_Callbacks() throws IOException, ParseException, JSONException {
        doTest("_callbacks/callbacks1.json", "_callbacks/callbacks2.json", "_callbacks/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_Security() throws IOException, ParseException, JSONException {
        doTest("_security/security1.json", "_security/security2.json", "_security/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_OperationTags() throws IOException, ParseException, JSONException {
        doTest("_opTags/opTags1.json", "_opTags/opTags2.json", "_opTags/merged.json");
    }

    /**
     * Test method for
     * {@link MergeUtil#merge(io.smallrye.openapi.api.models.OpenAPIImpl, io.smallrye.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    void testMerge_EmptyQueryParam() throws IOException, ParseException, JSONException {
        doTest("_pathEmpty/pathEmpty1.json", "_pathEmpty/pathEmpty2.json", "_pathEmpty/merged.json");
    }
}
