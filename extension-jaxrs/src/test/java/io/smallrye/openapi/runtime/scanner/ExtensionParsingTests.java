package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 * 
 */
class ExtensionParsingTests extends IndexScannerTestBase {

    @Test
    void testJavaxAllExpectedParseTypes() throws IOException, JSONException {
        assertJsonEquals("extensions.parsing.expected.json",
                test.io.smallrye.openapi.runtime.scanner.ExtensionParsingTestResource.class);
    }

    @Test
    void testJakartaAllExpectedParseTypes() throws IOException, JSONException {
        assertJsonEquals("extensions.parsing.expected.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExtensionParsingTestResource.class);
    }

    @Test
    void testJavaxSiblingExtensionAnnotations() throws IOException, JSONException {
        assertJsonEquals("extensions.scan-siblings.expected.json",
                test.io.smallrye.openapi.runtime.scanner.ExtensionPlacementTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.ExtensionPlacementTestResource.Model.class);
    }

    @Test
    void testJakartaSiblingExtensionAnnotations() throws IOException, JSONException {
        assertJsonEquals("extensions.scan-siblings.expected.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExtensionPlacementTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.ExtensionPlacementTestResource.Model.class);
    }
}
