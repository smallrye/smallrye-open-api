package io.smallrye.openapi.runtime.io;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class JacksonJsonIOTest extends JsonIOTest<JsonNode, ArrayNode, ObjectNode, ArrayNode, ObjectNode> {

    private Properties originalSystemProperties;

    @BeforeEach
    void setup(TestInfo testInfo) {
        originalSystemProperties = System.getProperties();
        Properties testSystemProperties = new Properties();
        originalSystemProperties.forEach(testSystemProperties::put);
        System.setProperties(testSystemProperties);

        testInfo.getTags().stream()
                .filter(tag -> tag.startsWith("property:"))
                .map(tag -> tag.substring("property:".length()))
                .map(tag -> tag.split("="))
                .forEach(tag -> System.setProperty(tag[0], tag[1]));

        super.target = new JacksonJsonIO(new ObjectMapper()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

    @AfterEach
    void teardown() {
        System.setProperties(originalSystemProperties);
    }
}
