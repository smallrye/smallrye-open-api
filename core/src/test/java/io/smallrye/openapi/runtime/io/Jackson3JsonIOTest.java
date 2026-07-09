package io.smallrye.openapi.runtime.io;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIf;

import io.smallrye.openapi.api.OpenApiConfig;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@EnabledIf("jackson3Present")
class Jackson3JsonIOTest extends JacksonJsonIOTest<JsonNode, ArrayNode, ObjectNode, ArrayNode, ObjectNode> {
    static boolean jackson3Present() {
        try {
            Class.forName("tools.jackson.databind.ObjectMapper");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        super.setup(testInfo);
        super.target = new Jackson3JsonIO(
                OpenApiConfig.fromConfig(ConfigProvider.getConfig()),
                JsonMapper.builder()
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                        .build());
    }
}
