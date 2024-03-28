package io.smallrye.openapi.runtime.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;

class StaticFileConfigTest {

    @Test
    void testSettingMaximumFileSize() {
        final Integer maximumFileSize = 8 * 1024 * 1024;
        System.setProperty(SmallRyeOASConfig.MAXIMUM_STATIC_FILE_SIZE, maximumFileSize.toString());

        try {
            Config config = ConfigProvider.getConfig();
            OpenApiConfig openApiConfig = OpenApiConfig.fromConfig(config);
            assertEquals(maximumFileSize, openApiConfig.getMaximumStaticFileSize());
        } finally {
            System.clearProperty(SmallRyeOASConfig.MAXIMUM_STATIC_FILE_SIZE);
        }
    }
}
