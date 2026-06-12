package io.smallrye.openapi.api.util;

import static org.eclipse.microprofile.openapi.OASFactory.createOpenAPI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.SmallRyeOASConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;

class UnusedTagFilterTest {

    private static final Config REMOVE_UNUSED_TAGS_CONFIG = new SmallRyeConfigBuilder()
            .withSources(new PropertiesConfigSource(
                    Map.of(SmallRyeOASConfig.SMALLRYE_REMOVE_UNUSED_TAGS, "true"),
                    "unit-test",
                    ConfigSource.DEFAULT_ORDINAL))
            .build();

    UnusedTagFilter target;

    @BeforeEach
    void setUp() {
        target = new UnusedTagFilter();
    }

    @Test
    void testEmptyNoFailureWithoutTags() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"));

        Assertions.assertDoesNotThrow(() -> {
            SmallRyeOpenAPI.builder()
                    .withInitialModel(initialModel)
                    .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                    .build()
                    .model();
        });
    }

    @Test
    void testAllUnusedTagsRemoved() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"))
                .addTag(OASFactory.createTag().name("unused-tag-1"))
                .addTag(OASFactory.createTag().name("unused-tag-2"))
                .addTag(OASFactory.createTag().name("unused-tag-3"))
                .paths(OASFactory.createPaths()
                        .addPathItem("/test", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .summary("Test operation"))));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                .build()
                .model();

        assertEquals(3, initialModel.getTags().size());
        assertTrue(filteredModel.getTags() == null || filteredModel.getTags().isEmpty());
    }

    @Test
    void testUsedTagsRetained() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"))
                .addTag(OASFactory.createTag().name("used-tag-1").description("Used tag 1"))
                .addTag(OASFactory.createTag().name("unused-tag").description("Unused tag"))
                .addTag(OASFactory.createTag().name("used-tag-2").description("Used tag 2"))
                .paths(OASFactory.createPaths()
                        .addPathItem("/test", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .summary("Test operation")
                                        .addTag("used-tag-1")
                                        .addTag("used-tag-2"))));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                .build()
                .model();

        assertEquals(3, initialModel.getTags().size());
        assertEquals(2, filteredModel.getTags().size());

        List<String> remainingTagNames = filteredModel.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        assertTrue(remainingTagNames.contains("used-tag-1"));
        assertTrue(remainingTagNames.contains("used-tag-2"));
    }

    @Test
    void testRetainedTagsNotRemoved() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"))
                .addTag(OASFactory.createTag()
                        .name("unused-but-retained")
                        .description("This should be kept")
                        .addExtension("x-smallrye-directives", List.of("retain")))
                .addTag(OASFactory.createTag().name("unused-tag"))
                .paths(OASFactory.createPaths()
                        .addPathItem("/test", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .summary("Test operation"))));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                .build()
                .model();

        assertEquals(2, initialModel.getTags().size());
        assertEquals(1, filteredModel.getTags().size());
        assertEquals("unused-but-retained", filteredModel.getTags().get(0).getName());
    }

    @Test
    void testMultipleOperationsReferenceSameTag() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"))
                .addTag(OASFactory.createTag().name("shared-tag"))
                .addTag(OASFactory.createTag().name("unused-tag"))
                .paths(OASFactory.createPaths()
                        .addPathItem("/test1", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .summary("Test operation 1")
                                        .addTag("shared-tag")))
                        .addPathItem("/test2", OASFactory.createPathItem()
                                .POST(OASFactory.createOperation()
                                        .summary("Test operation 2")
                                        .addTag("shared-tag"))));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                .build()
                .model();

        assertEquals(2, initialModel.getTags().size());
        assertEquals(1, filteredModel.getTags().size());
        assertEquals("shared-tag", filteredModel.getTags().get(0).getName());
    }

    @Test
    void testMixedUsedUnusedAndRetained() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"))
                .addTag(OASFactory.createTag().name("used-tag"))
                .addTag(OASFactory.createTag().name("unused-tag"))
                .addTag(OASFactory.createTag()
                        .name("retained-tag")
                        .addExtension("x-smallrye-directives", List.of("retain")))
                .addTag(OASFactory.createTag().name("another-unused-tag"))
                .paths(OASFactory.createPaths()
                        .addPathItem("/test", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .summary("Test operation")
                                        .addTag("used-tag"))));

        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                .build()
                .model();

        assertEquals(4, initialModel.getTags().size());
        assertEquals(2, filteredModel.getTags().size());

        List<String> remainingTagNames = filteredModel.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        assertTrue(remainingTagNames.contains("used-tag"));
        assertTrue(remainingTagNames.contains("retained-tag"));
    }

    @Test
    void testUnusedTagsRemoved() throws JSONException, IOException {
        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .withCustomStaticFile(() -> getClass().getResourceAsStream("UnusedTagFilter-before.json"))
                .withConfig(REMOVE_UNUSED_TAGS_CONFIG)
                .build();

        JSONAssert.assertEquals(
                new String(getClass().getResourceAsStream("UnusedTagFilter-after.json").readAllBytes()),
                result.toJSON(),
                JSONCompareMode.STRICT);
    }

    @Test
    void testDisabledByDefault() {
        OpenAPI initialModel = createOpenAPI()
                .info(OASFactory.createInfo()
                        .description("Test")
                        .version("1.0"))
                .addTag(OASFactory.createTag().name("unused-tag"))
                .paths(OASFactory.createPaths()
                        .addPathItem("/test", OASFactory.createPathItem()
                                .GET(OASFactory.createOperation()
                                        .summary("Test operation"))));

        // Build without the remove-unused-tags config
        OpenAPI filteredModel = SmallRyeOpenAPI.builder()
                .withInitialModel(initialModel)
                .build()
                .model();

        assertNotNull(filteredModel.getTags());
        assertEquals(1, filteredModel.getTags().size());
        assertEquals("unused-tag", filteredModel.getTags().get(0).getName());
    }
}
