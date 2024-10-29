package io.smallrye.openapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASConfig;
import org.junit.jupiter.api.Test;

import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;

class OpenApiConfigImplTest {

    private static final String TEST_PROPERTY = OASConfig.EXTENSIONS_PREFIX + "OpenApiConfigImplTest";

    @Test
    void testGetStringConfigValueMissingIsNull() {
        System.clearProperty(TEST_PROPERTY);
        Config config = ConfigProvider.getConfig();
        OpenApiConfig oaiConfig = OpenApiConfig.fromConfig(config);
        assertNull(oaiConfig.getConfigValue(TEST_PROPERTY, String.class, () -> null));
    }

    @Test
    void testGetStringConfigValueBlankIsNull() {
        System.setProperty(TEST_PROPERTY, "\t \n\r");

        try {
            Config config = ConfigProvider.getConfig();
            OpenApiConfig oaiConfig = OpenApiConfig.fromConfig(config);
            // White-space only value is treated as absent value
            assertNull(oaiConfig.getConfigValue(TEST_PROPERTY, String.class, () -> null));
        } finally {
            System.clearProperty(TEST_PROPERTY);
        }
    }

    @Test
    void testGetStringConfigValuePresent() {
        System.setProperty(TEST_PROPERTY, "  VALUE  \t");

        try {
            Config config = ConfigProvider.getConfig();
            OpenApiConfig oaiConfig = OpenApiConfig.fromConfig(config);
            // Trim is only used to determine if the value is blank. Full value returned for app use
            assertEquals("  VALUE  \t", oaiConfig.getConfigValue(TEST_PROPERTY, String.class, () -> null));
        } finally {
            System.clearProperty(TEST_PROPERTY);
        }
    }

    @Test
    void testLoadPropertyYaml() throws Exception {
        var builder = new SmallRyeConfigBuilder();
        builder.withSources(new YamlConfigSource(getClass().getResource("config.yaml")));
        Config config = builder.build();

        OpenApiConfig oaiConfig = OpenApiConfig.fromConfig(config);
        assertEquals(Set.of("java.lang", "java.util"), oaiConfig.scanExcludePackages());
        assertEquals(Set.of("my.pkg.Test", "my.pkg.Another"), oaiConfig.scanExcludeClasses());
    }
}
