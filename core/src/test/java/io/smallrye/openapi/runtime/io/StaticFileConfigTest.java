package io.smallrye.openapi.runtime.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.constants.OpenApiConstants;

class StaticFileConfigTest {

    @Test
    void testSettingMaximumFileSize() throws IOException, JSONException {
        final Integer maximumFileSize = 8 * 1024 * 1024;
        System.setProperty(OpenApiConstants.MAXIMUM_STATIC_FILE_SIZE, maximumFileSize.toString());
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        assertEquals(maximumFileSize, openApiConfig.getMaximumStaticFileSize());
    }
}
