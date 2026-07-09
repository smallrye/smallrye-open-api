package io.smallrye.openapi.runtime.io;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIf;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.OpenApiConfig;

@EnabledIf("jackson2Present")
class Jackson2JsonIOTest extends JacksonJsonIOTest<JsonNode, ArrayNode, ObjectNode, ArrayNode, ObjectNode> {
    static boolean jackson2Present() {
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        super.setup(testInfo);
        super.target = new Jackson2JsonIO(
                OpenApiConfig.fromConfig(ConfigProvider.getConfig()),
                new ObjectMapper()
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }
}
