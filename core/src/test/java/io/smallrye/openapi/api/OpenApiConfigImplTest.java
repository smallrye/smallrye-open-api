package io.smallrye.openapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASConfig;
import org.junit.jupiter.api.Test;

class OpenApiConfigImplTest {

    private static final String TEST_PROPERTY = OASConfig.EXTENSIONS_PREFIX + "OpenApiConfigImplTest";

    @Test
    void testGetStringConfigValueMissingIsNull() {
        System.clearProperty(TEST_PROPERTY);
        Config config = ConfigProvider.getConfig();
        OpenApiConfigImpl oaiConfig = new OpenApiConfigImpl(config);
        assertNull(oaiConfig.getStringConfigValue(TEST_PROPERTY));
    }

    @Test
    void testGetStringConfigValueBlankIsNull() {
        System.setProperty(TEST_PROPERTY, "\t \n\r");

        try {
            Config config = ConfigProvider.getConfig();
            OpenApiConfigImpl oaiConfig = new OpenApiConfigImpl(config);
            // White-space only value is treated as absent value
            assertNull(oaiConfig.getStringConfigValue(TEST_PROPERTY));
        } finally {
            System.clearProperty(TEST_PROPERTY);
        }
    }

    @Test
    void testGetStringConfigValuePresent() {
        System.setProperty(TEST_PROPERTY, "  VALUE  \t");

        try {
            Config config = ConfigProvider.getConfig();
            OpenApiConfigImpl oaiConfig = new OpenApiConfigImpl(config);
            // Trim is only used to determine if the value is blank. Full value returned for app use
            assertEquals("  VALUE  \t", oaiConfig.getStringConfigValue(TEST_PROPERTY));
        } finally {
            System.clearProperty(TEST_PROPERTY);
        }
    }

}
