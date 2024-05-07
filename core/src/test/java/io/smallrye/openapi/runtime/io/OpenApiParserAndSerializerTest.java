package io.smallrye.openapi.runtime.io;

import static io.smallrye.openapi.runtime.scanner.IndexScannerTestBase.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.yaml.snakeyaml.error.YAMLException;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;

/**
 * @author eric.wittmann@gmail.com
 */
class OpenApiParserAndSerializerTest {

    static final int REPEAT_BODY_CONTENTS_ITERATIONS = 1536; // ~8MB?

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
     * @param original
     * @param roundTrip
     */
    private static void assertYamlEquals(String original, String roundTrip) {
        Assertions.assertEquals(normalizeYaml(original), normalizeYaml(roundTrip));
    }

    /**
     * Normalizes the YAML by removing any comments.
     *
     * @param yaml
     */
    private static String normalizeYaml(String yaml) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(yaml));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("--")) {
                    continue;
                }
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path generateBigStaticFile() throws IOException {
        //  let's build a big openapi file, start by its header
        String bigFileHeader = loadResource(OpenApiParserAndSerializerTest.class.getResource("openapi-fragment-header.yaml"));
        StringBuilder bigFileContents = new StringBuilder(bigFileHeader);
        // body
        final String bodyChunk = loadResource(OpenApiParserAndSerializerTest.class.getResource("openapi-fragment-body.yaml"));
        for (int i = 0; i < REPEAT_BODY_CONTENTS_ITERATIONS; i++) {
            //  n-chunk of body
            bigFileContents.append(bodyChunk.replaceAll("@@ID@@", String.valueOf(i)));
        }
        //  footer
        String bigFileFooter = loadResource(OpenApiParserAndSerializerTest.class.getResource("openapi-fragment-footer.yaml"));
        bigFileContents.append(bigFileFooter);

        Path tempFile = Files.createTempFile("sroap-big-file-test-", "-generated.yaml");
        Files.write(tempFile, bigFileContents.toString().getBytes(StandardCharsets.UTF_8));
        return tempFile;
    }

    /**
     * Performs a full round-trip parse+serialize test on a single resource.
     *
     * @param testResource
     * @param format
     * @throws IOException
     * @throws JSONException
     */
    private static void doTest(URL testResource, Format format, OpenAPI impl) throws IOException, JSONException {
        String original = loadResource(testResource);
        String roundTrip = OpenApiSerializer.serialize(impl, format);

        try {
            if (format == Format.JSON) {
                assertJsonEquals(original, roundTrip);
            } else {
                assertYamlEquals(original, roundTrip);
            }
        } catch (AssertionError e) {
            System.out.println("================");
            System.out.println(roundTrip);
            System.out.println("================");
            throw e;
        }
    }

    /**
     * Performs a full round-trip parse+serialize test on a single resource.
     *
     * @param resource
     * @param format
     * @throws IOException
     * @throws JSONException
     */
    private static void doTest(String resource, Format format) throws IOException, JSONException {
        URL testResource = OpenApiParserAndSerializerTest.class.getResource(resource);
        OpenAPI impl = OpenApiParser.parse(testResource);
        doTest(testResource, format, impl);
    }

    /**
     * Performs a full round-trip parse+serialize test on a single resource.
     *
     * @param testResource
     * @param format
     * @throws IOException
     * @throws JSONException
     */
    private static void doTest(URL testResource, Format format) throws IOException, JSONException {
        OpenAPI impl = OpenApiParser.parse(testResource);
        doTest(testResource, format, impl);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testParseSimplest() throws IOException, JSONException {
        doTest("simplest.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testParseSimplestYaml() throws IOException, JSONException {
        doTest("simplest.yaml", Format.YAML);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testParseInfo() throws IOException, JSONException {
        doTest("info.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testParseInfoYaml() throws IOException, JSONException {
        doTest("info.yaml", Format.YAML);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testExternalDocs() throws IOException, JSONException {
        doTest("externalDocs.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testExtensions() throws IOException, JSONException {
        doTest("extensions.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSecurity() throws IOException, JSONException {
        doTest("security.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testServers() throws IOException, JSONException {
        doTest("servers.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testServersYaml() throws IOException, JSONException {
        doTest("servers.yaml", Format.YAML);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testTags() throws IOException, JSONException {
        doTest("tags.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Callbacks() throws IOException, JSONException {
        doTest("components-callbacks.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Empty() throws IOException, JSONException {
        doTest("components-empty.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Examples() throws IOException, JSONException {
        doTest("components-examples.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Headers() throws IOException, JSONException {
        doTest("components-headers.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Links() throws IOException, JSONException {
        doTest("components-links.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Parameters() throws IOException, JSONException {
        doTest("components-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_RequestBodies() throws IOException, JSONException {
        doTest("components-requestBodies.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Responses() throws IOException, JSONException {
        doTest("components-responses.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_Schemas() throws IOException, JSONException {
        doTest("components-schemas.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testComponents_SecuritySchemes() throws IOException, JSONException {
        doTest("components-securitySchemes.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_AllOperations() throws IOException, JSONException {
        doTest("paths-all-operations.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_Empty() throws IOException, JSONException {
        doTest("paths-empty.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetCallbacks() throws IOException, JSONException {
        doTest("paths-get-callbacks.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetParameters() throws IOException, JSONException {
        doTest("paths-get-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetRequestBodyContent() throws IOException, JSONException {
        doTest("paths-get-requestBody-content.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetRequestBodyExample() throws IOException, JSONException {
        doTest("paths-get-requestBody-example.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetRequestBody() throws IOException, JSONException {
        doTest("paths-get-requestBody.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetResponseContent() throws IOException, JSONException {
        doTest("paths-get-response-content.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetResponseHeaders() throws IOException, JSONException {
        doTest("paths-get-response-headers.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetResponseLinks() throws IOException, JSONException {
        doTest("paths-get-response-links.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetResponses() throws IOException, JSONException {
        doTest("paths-get-responses.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetSecurity() throws IOException, JSONException {
        doTest("paths-get-security.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_GetServers() throws IOException, JSONException {
        doTest("paths-get-servers.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_Get() throws IOException, JSONException {
        doTest("paths-get.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_Parameters() throws IOException, JSONException {
        doTest("paths-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_Ref() throws IOException, JSONException {
        doTest("paths-ref.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_Servers() throws IOException, JSONException {
        doTest("paths-servers.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testPaths_Extensions() throws IOException, JSONException {
        doTest("paths-with-extensions.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_Discriminator() throws IOException, JSONException {
        doTest("schemas-discriminator.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_AdditionalProperties() throws IOException, JSONException {
        doTest("schemas-with-additionalProperties.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_AllOf() throws IOException, JSONException {
        doTest("schemas-with-allOf.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_Composition() throws IOException, JSONException {
        doTest("schemas-with-composition.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_Example() throws IOException, JSONException {
        doTest("schemas-with-example.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_ExternalDocs() throws IOException, JSONException {
        doTest("schemas-with-externalDocs.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_MetaData() throws IOException, JSONException {
        doTest("schemas-with-metaData.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testSchemas_XML() throws IOException, JSONException {
        doTest("schemas-with-xml.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testEverything() throws IOException, JSONException {
        doTest("_everything.json", Format.JSON);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    void testEverythingYaml() throws IOException, JSONException {
        doTest("_everything.yaml", Format.YAML);
    }

    /**
     * Test method for {@link OpenApiParser#parse(java.net.URL)}.
     *
     * Here we check that {@link OpenApiParser#parse(java.net.URL)} throws an exception
     * when a file bigger than the maximum allowed size is passed, while
     * {@link OpenApiParser#parse(InputStream, Format, Integer)} is successful when using a higher limit.
     */
    @Test
    void testParsingBigStaticYamlFile() throws IOException, JSONException {
        // let's check that parsing a file bigger than the default (3 MB) allowed by the underlying implementation
        // would fail
        Path tempFile = generateBigStaticFile();
        URL tempFileLoc = tempFile.toUri().toURL();
        Exception thrown = assertThrows(OpenApiRuntimeException.class, () -> doTest(tempFileLoc, Format.YAML));
        assertNotNull(thrown.getCause());
        Throwable rootCause = thrown.getCause().getCause();
        assertTrue(rootCause instanceof YAMLException);

        // now let's set a higher limit
        final Integer maximumFileSize = 8 * 1024 * 1024;
        // and finally, let's parse the file but pass the custom limit
        try {
            System.setProperty(SmallRyeOASConfig.MAXIMUM_STATIC_FILE_SIZE, maximumFileSize.toString());

            try (InputStream is = new FileInputStream(tempFile.toFile())) {
                doTest(tempFile.toUri().toURL(), Format.YAML, OpenApiParser.parse(is, Format.YAML, null));
            }
        } finally {
            System.clearProperty(SmallRyeOASConfig.MAXIMUM_STATIC_FILE_SIZE);
        }
    }

    @Test
    void testSerializeLongKeyPreserved() throws IOException {
        OpenAPI doc = OASFactory.createOpenAPI();
        String key = "x-" + new String(new char[1021]).replace('\0', 'x');
        assertEquals(1023, key.length());
        doc.addExtension(key, "OK");
        String yaml = OpenApiSerializer.serialize(doc, Format.YAML);
        assertTrue(yaml.indexOf('?') < 0);
    }

    @Test
    void testSerializeExtraLongKeyConvertsToExplicit() throws IOException {
        OpenAPI doc = OASFactory.createOpenAPI();
        String key = "x-" + new String(new char[1022]).replace('\0', 'x');
        assertEquals(1024, key.length());
        doc.addExtension(key, "OK");
        String yaml = OpenApiSerializer.serialize(doc, Format.YAML);
        assertTrue(yaml.indexOf('?') >= 0);
    }

    @Test
    void testJsonObjectWriter() throws Exception {
        OpenAPI doc = OASFactory.createOpenAPI();
        doc.addExtension("x-foo", "bar");
        String json = OpenApiSerializer.serialize(doc, Format.JSON);
        assertJsonEquals("{\"x-foo\":\"bar\"}", json);
    }

    @Test
    void testYamlObjectWriter() throws Exception {
        OpenAPI doc = OASFactory.createOpenAPI();
        doc.addExtension("x-foo", "bar");
        String yaml = OpenApiSerializer.serialize(doc, Format.YAML);
        assertYamlEquals("x-foo: bar", yaml);
    }
}
